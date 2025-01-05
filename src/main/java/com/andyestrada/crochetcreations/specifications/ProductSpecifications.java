package com.andyestrada.crochetcreations.specifications;

import com.andyestrada.crochetcreations.entities.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecifications {

    public static Specification<Product> nameContains(String searchString) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")),
                "%" + searchString.toLowerCase() + "%"
        );
    }

    public static Specification<Product> listedForSaleEq(boolean value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                root.get("listedForSale"),
                value
        );
    }

}
