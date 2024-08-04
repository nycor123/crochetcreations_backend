package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.JumbotronContentDto;
import com.andyestrada.crochetcreations.entities.Image;
import com.andyestrada.crochetcreations.entities.JumbotronContent;
import com.andyestrada.crochetcreations.entities.JumbotronImage;
import com.andyestrada.crochetcreations.repositories.ImageRepository;
import com.andyestrada.crochetcreations.repositories.JumbotronContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JumbotronContentServiceImpl implements JumbotronContentService {

    private final JumbotronContentRepository jumbotronContentRepository;
    private final ImageService imageService;

    private static final Logger _logger = LoggerFactory.getLogger(JumbotronContentServiceImpl.class);

    @Autowired
    public JumbotronContentServiceImpl(
            JumbotronContentRepository jumbotronContentRepository,
            ImageService imageService) {

        this.jumbotronContentRepository = jumbotronContentRepository;
        this.imageService = imageService;
    }

    @Override
    public Optional<List<JumbotronContent>> getAll() {
        return Optional.of(jumbotronContentRepository.findAll());
    }

    @Override
    public Optional<JumbotronContent> findById(Long jumbotronContentId) {
        return Optional.of(jumbotronContentRepository.findById(jumbotronContentId)).orElseThrow();
    }

    @Override
    public JumbotronContent save(JumbotronContentDto jContentDto) {
        //TODO: validateJumbotronContentDto(jContentDto);
        JumbotronContent jContent = JumbotronContent.builder()
                .url(jContentDto.getUrl())
                .priority(jContentDto.getPriority())
                .build();
        Image image = imageService.findById(jContentDto.getImageId()).orElseThrow(() -> new RuntimeException("Image id not found."));
        JumbotronImage jumbotronImage = convertImageToJumbotronImage(image);
        jumbotronImage.setJumbotronContent(jContent);
        jContent.setImage(jumbotronImage);
        return jumbotronContentRepository.save(jContent);
    }

    @Override
    public Boolean delete(Long jumbotronContentId) {
        JumbotronContent jumbotronContent = findById(jumbotronContentId).orElseThrow();
        jumbotronContentRepository.delete(jumbotronContent);
        return true;
    }

    @Override
    public JumbotronContent update(Long id, JumbotronContentDto jContentDto) {
        JumbotronContent jContent = findById(id).orElseThrow();
        if (jContentDto.getImageId() != null) {
            // remove existing JumbotronContent-Image relationship
            JumbotronImage jImage = (JumbotronImage) imageService.findById(jContent.getImage().getId()).orElseThrow();
            jContent.setImage(null);
            jImage.setJumbotronContent(null);
            jumbotronContentRepository.save(jContent);
            imageService.deleteImage(jImage.getId(), true);
            // create new JumbotronContent-Image relationship
            jContent = findById(id).orElseThrow();
            Image image = imageService.findById(jContentDto.getImageId()).orElseThrow();
            JumbotronImage newImage = convertImageToJumbotronImage(image);
            jContent.setImage(newImage);
            newImage.setJumbotronContent(jContent);
        }
        if (jContentDto.getUrl() != null) {
            jContent.setUrl(jContentDto.getUrl());
        }
        if (jContentDto.getPriority() != null) {
            jContent.setPriority(jContentDto.getPriority());
        }
        return jumbotronContentRepository.save(jContent);
    }

    private JumbotronImage convertImageToJumbotronImage(Image image) {
        JumbotronImage jumbotronImage = new JumbotronImage(image);
        imageService.deleteImage(image.getId(), false);
        return jumbotronImage;
    }

    private void validateJumbotronContentDto(JumbotronContentDto jContentDto) {
        //TODO
    }

}
