package com.andyestrada.crochetcreations.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DiscriminatorValue("Product")
public class ProductImage extends Image {

    public ProductImage(Image image) {
        this.setRemotePublicId(image.getRemotePublicId());
        this.setUrl(image.getUrl());
    }

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    private Integer priority;
}
