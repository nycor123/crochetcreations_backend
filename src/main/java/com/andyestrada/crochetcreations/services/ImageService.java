package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.request.ImageDto;
import com.andyestrada.crochetcreations.entities.Image;

import java.util.Optional;

public interface ImageService {
    public Optional<Image> uploadImage(ImageDto imageDto);
    public Optional<Image> findById(Long id);
    public Boolean deleteImage(Long id, Boolean deleteFromRemote);
    public String deleteImageFromRemote(Image image);
}
