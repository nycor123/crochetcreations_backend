package com.andyestrada.crochetcreations.services.authentication;

import com.andyestrada.crochetcreations.dto.UserInfoDto;
import com.andyestrada.crochetcreations.entities.Role;
import com.andyestrada.crochetcreations.entities.User;
import com.andyestrada.crochetcreations.repositories.UserRepository;
import com.andyestrada.crochetcreations.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements CustomUserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public UserInfoDto getUserInfo(String username) {
        User user = (User) loadUserByUsername(username);
        return UserInfoDto.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().toString())
                .pictureUrl(user.getPictureUrl())
                .build();
    }

    @Override
    public Boolean isAdmin(String username) {
        UserDetails user = this.loadUserByUsername(username);
        return user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList()
                .contains(Role.ADMIN.toString());
    }

}
