package com.andyestrada.crochetcreations.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PRODUCTS")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "listed_for_sale", nullable = false)
    private Boolean listedForSale;

    @JsonManagedReference
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductPrice> prices;

    private String description;
    private String[] images;

    public Optional<ProductPrice> getEffectivePrice() {
        if (prices != null && prices.size() > 0) {
            return prices.stream().filter(p -> p.getUntil() == null).findFirst();
        }
        return Optional.empty();
    }

}
