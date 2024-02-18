package com.andyestrada.crochetcreations.repositories;

import com.andyestrada.crochetcreations.entities.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {

}
