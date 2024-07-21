package com.andyestrada.crochetcreations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JumbotronContentDto {
    private Long id;
    private Long imageId;
    private String url;
    private Integer priority;
}
