package com.andyestrada.crochetcreations.dto.request;

import com.andyestrada.crochetcreations.entities.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean listedForSale;
    private List<Long> imageIds;
}
