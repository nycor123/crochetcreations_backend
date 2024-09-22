package com.andyestrada.crochetcreations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String pictureUrl;
}
