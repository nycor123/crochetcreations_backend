package com.andyestrada.crochetcreations.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemDto {
    private Long id;
    private CartDto cart;
    private Long productId; // used ONLY for requests
    private ProductDto product;
    private Integer quantity;
}
