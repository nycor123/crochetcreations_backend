package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.dto.request.ImageDto;
import com.andyestrada.crochetcreations.entities.Image;
import com.andyestrada.crochetcreations.services.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<Image> upload(ImageDto imageDto) {
        try {
            Optional<Image> imageOptional = imageService.uploadImage(imageDto);
            return imageOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Image> getImage(@PathVariable long id) {
        Optional<Image> imageOptional = imageService.findById(id);
        return imageOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable long id) {
        Boolean isDeleted = imageService.deleteImage(id);
        return isDeleted ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

}
