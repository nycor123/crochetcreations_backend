package com.andyestrada.crochetcreations.services.google;

import com.andyestrada.crochetcreations.dto.google.GoogleSigninRequestDto;
import com.andyestrada.crochetcreations.dto.response.JwtAuthenticationResponseDto;
import com.andyestrada.crochetcreations.entities.Cart;
import com.andyestrada.crochetcreations.entities.Role;
import com.andyestrada.crochetcreations.entities.User;
import com.andyestrada.crochetcreations.repositories.CartRepository;
import com.andyestrada.crochetcreations.repositories.UserRepository;
import com.andyestrada.crochetcreations.services.authentication.JwtService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleOAuthServiceImpl implements GoogleOAuthService {

    @Value("${google.oauth.clientid}")
    private String clientID;

    @Value("${google.oauth.secret}")
    private String clientSecret;

    @Value("${google.oauth.scope}")
    private String[] scope;

    @Value("${google.oauth.accesstoken.url}")
    private String accessTokenUrl;

    @Value("${google.oauth.userinfo.url}")
    private String userInfoUrl;

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final JwtService jwtService;

    @Autowired
    public GoogleOAuthServiceImpl(UserRepository userRepository,
                                  CartRepository cartRepository,
                                  JwtService jwtService) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.jwtService = jwtService;
    }

    @Override
    public JwtAuthenticationResponseDto signin(GoogleSigninRequestDto signinRequestDto) {
        JsonObject profileDetails = retrieveUserDetails(signinRequestDto);
        String id = profileDetails.get("id").getAsString();
        String email = profileDetails.get("email").getAsString();
        String isEmailVerified = profileDetails.get("verified_email").getAsString();
        String name = profileDetails.get("name").getAsString();
        String firstName = profileDetails.get("given_name").getAsString();
        String lastName = profileDetails.get("family_name").getAsString();
        String pictureUrl = profileDetails.get("picture").getAsString();
        User user;
        if (userRepository.findByEmail(email).isPresent()) {
            // Get existing user.
            user = userRepository.findByEmail(email).orElseThrow();
        } else {
            // Create new user.
            User transientUser = User.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .pictureUrl(pictureUrl)
                    .role(Role.USER)
                    .build();
            user = userRepository.save(transientUser);
            // Initialize cart for user.
            Cart cart = Cart.builder().user(user).build();
            user.setCart(cartRepository.save(cart));
            userRepository.save(user);
        }
        // Sign in user.
        String jwt = jwtService.generateToken(user);
        return JwtAuthenticationResponseDto.builder().token(jwt).build();
    }

    private JsonObject retrieveUserDetails(GoogleSigninRequestDto signinRequestDto) {
        String accessToken = getOauthAccessTokenGoogle(signinRequestDto);
        JsonObject jsonObject = getProfileDetailsGoogle(accessToken);
        return jsonObject;
    }

    private String getOauthAccessTokenGoogle(GoogleSigninRequestDto signinRequestDto) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // Set parameters.
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", signinRequestDto.getGrantCode());
        params.add("redirect_uri", signinRequestDto.getRedirectUri());
        params.add("client_id", this.clientID);
        params.add("client_secret", this.clientSecret);
        for (String s : scope) {
            s = s.replace(":", "%3A").replace("/", "%2F");
            params.add("scope", s);
        }
        params.add("grant_type", "authorization_code");
        // Execute POST request.
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, httpHeaders);
        String response = restTemplate.postForObject(this.accessTokenUrl, requestEntity, String.class);
        JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);
        return jsonObject.get("access_token").getAsString();
    }

    private JsonObject getProfileDetailsGoogle(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(this.userInfoUrl, HttpMethod.GET, requestEntity, String.class);
        return new Gson().fromJson(response.getBody(), JsonObject.class);
    }

}
