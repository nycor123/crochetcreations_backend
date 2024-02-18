package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.request.ImageDto;
import com.andyestrada.crochetcreations.entities.Image;
import com.andyestrada.crochetcreations.repositories.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final CloudinaryService cloudinaryService;
    private final ImageRepository imageRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageServiceImpl.class);

    @Override
    public Optional<Image> uploadImage(ImageDto imageDto) {
        this.validateImageDto(imageDto);
        try {
            Map<String, String> fileProperties = cloudinaryService.uploadFile(imageDto.getFile(), "crochetcreations");
            Image image = Image.builder()
                    .name(imageDto.getName())
                    .remotePublicId(fileProperties.get("publicId"))
                    .url(fileProperties.get("url"))
                    .build();
            imageRepository.save(image);
            return Optional.of(image);
        } catch (Exception e) {
            LOGGER.error("An exception occurred while trying to upload image.", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Image> findById(Long id) {
        return imageRepository.findById(id);
    }

    @Override
    public Boolean deleteImage(Long id) {
        try {
            Image image = imageRepository.findById(id).orElseThrow();
            this.deleteImageFromRemote(image);
            imageRepository.delete(image);
            return true;
        } catch (Exception e) {
            LOGGER.error("ImageServiceImpl::deleteImage | An exception occurred while trying to delete image.", e);
            return false;
        }
    }

    @Override
    public String deleteImageFromRemote(Image image) {
        return cloudinaryService.deleteFile(image.getRemotePublicId());
    }

    private void validateImageDto(ImageDto imageDto) {
        if (imageDto.getName() == null || imageDto.getName().isEmpty()) {
            throw new IllegalStateException("Image name is required.");
        }
        if (imageDto.getFile() == null || imageDto.getFile().isEmpty()) {
            throw new IllegalStateException("File is required.");
        }
    }

}
