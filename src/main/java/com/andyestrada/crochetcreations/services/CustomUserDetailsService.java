package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.UserInfoDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomUserDetailsService extends UserDetailsService {
    UserInfoDto getUserInfo(String username);
    Boolean isAdmin(String username);
}
