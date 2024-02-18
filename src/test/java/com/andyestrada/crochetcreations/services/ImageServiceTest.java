package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.request.ImageDto;
import com.andyestrada.crochetcreations.entities.Image;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Test
    public void canUploadValidImage() throws Exception {
        //given
        File file = new File("src/test/resources/sample_image.png");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(
                "SampleImage",
                file.getName(),
                "image/png",
                IOUtils.toByteArray(input));
        ImageDto imageDto = ImageDto.builder()
                .name("test_image")
                .file(multipartFile)
                .build();
        //when
        Image image = imageService.uploadImage(imageDto).get();
        //then
        assertNotNull(image.getId());
        assertNotNull(image.getName());
        assertNotNull(image.getUrl());
        //cleanup
        imageService.deleteImageFromRemote(image);
    }

    @Test
    public void cannotUploadImageWithoutName() {
        //given
        ImageDto imageDto = ImageDto.builder()
                .file(null)
                .build();
        String expectedMessage = "Image name is required.";
        //when
        Exception exception = assertThrows(Exception.class, () -> imageService.uploadImage(imageDto));
        //then
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void cannotUploadImageWithoutFile() {
        //given
        ImageDto imageDto = ImageDto.builder()
                .name("test_image")
                .build();
        String expectedMessage = "File is required.";
        //when
        Exception exception = assertThrows(Exception.class, () -> imageService.uploadImage(imageDto));
        //then
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void canDeleteImage() throws Exception {
        //given
        File file = new File("src/test/resources/sample_image.png");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(
                "SampleImage",
                file.getName(),
                "image/png",
                IOUtils.toByteArray(input));
        ImageDto imageDto = ImageDto.builder()
                .name("test_image")
                .file(multipartFile)
                .build();
        Image image = imageService.uploadImage(imageDto).get();
        //when
        Boolean isDeleted = imageService.deleteImage(image.getId());
        //then
        assertTrue(isDeleted);
    }

}
