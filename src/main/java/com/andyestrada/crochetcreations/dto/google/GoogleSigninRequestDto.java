package com.andyestrada.crochetcreations.dto.google;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleSigninRequestDto {
    private String grantCode;
    private String redirectUri;
}
