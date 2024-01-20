package com.andyestrada.crochetcreations.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
