package com.andyestrada.crochetcreations.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ORDERS")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonManagedReference
    @ToString.Exclude
    @OneToMany(mappedBy = "order")
    private List<Item> items;

    @ToString.Exclude
    @OneToMany(mappedBy = "order")
    private List<CartItem> cartItems;
}
