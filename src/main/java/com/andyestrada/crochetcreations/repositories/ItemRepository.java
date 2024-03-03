package com.andyestrada.crochetcreations.repositories;

import com.andyestrada.crochetcreations.entities.Item;
import com.andyestrada.crochetcreations.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

}
