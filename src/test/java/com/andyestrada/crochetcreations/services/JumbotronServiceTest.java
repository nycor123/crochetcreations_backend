package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.JumbotronContentDto;
import com.andyestrada.crochetcreations.entities.Image;
import com.andyestrada.crochetcreations.entities.JumbotronContent;
import com.andyestrada.crochetcreations.repositories.ImageRepository;
import com.andyestrada.crochetcreations.repositories.JumbotronContentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class JumbotronServiceTest {

    @Autowired
    private JumbotronContentService jumbotronContentService;

    @Autowired
    private JumbotronContentRepository jumbotronContentRepository;

    @Autowired
    private ImageRepository imageRepository;

    @BeforeEach
    public void reset() {
        jumbotronContentRepository.deleteAll();
    }

    @Test
    public void canGetJumbotronContents() {
        //given
        List<JumbotronContent> jContents = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Image image = createImage();
            JumbotronContentDto jContentDto = JumbotronContentDto.builder()
                    .imageId(image.getId())
                    .url("test@url.com")
                    .priority(i)
                    .build();
            jContents.add(jumbotronContentService.save(jContentDto));
        }
        //when
        List<JumbotronContent> fetchedJContents = jumbotronContentService.getAll().orElseThrow();
        //then
        assertEquals(jContents.size(), fetchedJContents.size());
    }

    @Test
    public void canCreateJumbotronContent() {
        //given
        Image image = createImage();
        //when
        JumbotronContentDto jContentDto = JumbotronContentDto.builder()
                .imageId(image.getId())
                .url("test@url.com")
                .priority(1)
                .build();
        JumbotronContent jContent = jumbotronContentService.save(jContentDto);
        //then
        Optional<JumbotronContent> jContentOptional = jumbotronContentRepository.findById(jContent.getId());
        assertTrue(jContentOptional.isPresent());
    }

    @Test
    public void canDeleteJumbotronContent() {
        //TODO
    }

    private Image createImage() {
        Image image = Image.builder()
                .remotePublicId("public_id")
                .url("test_url")
                .build();
        return imageRepository.save(image);
    }

}
