package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.CrochetCreationsApplication;
import com.andyestrada.crochetcreations.dto.request.ImageDto;
import com.andyestrada.crochetcreations.entities.Image;
import com.andyestrada.crochetcreations.services.ImageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = CrochetCreationsApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ImageService imageService;

    private List<Long> imageIds = new ArrayList<>();

    @AfterAll
    public void cleanup() {
        for (Long imageId : imageIds) {
            imageService.deleteImage(imageId, true);
        }
    }

    @Test
    public void shouldUploadValidImage() throws Exception {
        //given
        File file = new File("src/test/resources/sample_image.png");
        FileInputStream input = new FileInputStream(file);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "SampleImage",
                file.getName(),
                "image/png",
                IOUtils.toByteArray(input));
        //when
        ResultActions result = mockMvc.perform(multipart("/api/v1/images/upload")
                .file("file", multipartFile.getBytes()));
        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remotePublicId").isNotEmpty())
                .andExpect(jsonPath("$.url").isNotEmpty());
        //cleanup
        Integer id = JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
        imageIds.add(Long.valueOf(id));
    }

    @Test
    public void shouldNotUploadImageWithoutFile() throws Exception {
        //when
        ResultActions result = mockMvc.perform(multipart("/api/v1/images/upload").file("file", null));
        //then
        result.andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldGetImageById() throws Exception {
        //given
        Image image = this.uploadImage();
        //when
        ResultActions result = mockMvc.perform(get("/api/v1/images/" + image.getId()));
        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remotePublicId").isNotEmpty())
                .andExpect(jsonPath("$.url").isNotEmpty());
        //cleanup
        Integer id = JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
        imageIds.add(Long.valueOf(id));
    }

    @Test
    public void shouldDeleteImageById() throws Exception {
        //given
        Image image = this.uploadImage();
        //when
        ResultActions result = mockMvc.perform(delete("/api/v1/images/" + image.getId()));
        //then
        result.andExpect(status().isOk());
    }

    @Test
    public void shouldNotUploadFileWithInvalidType() throws Exception {
        // given
        File file = new File("src/test/resources/application.properties");
        FileInputStream input = new FileInputStream(file);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "SampleImage",
                file.getName(),
                "text/plain",
                IOUtils.toByteArray(input));
        // when
        ResultActions result = mockMvc.perform(multipart("/api/v1/images/upload")
                .file("file", multipartFile.getBytes()));
        // then
        result.andExpect(status().isBadRequest());
    }

    private Image uploadImage() throws Exception {
        File file = new File("src/test/resources/sample_image.png");
        FileInputStream input = new FileInputStream(file);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "SampleImage",
                file.getName(),
                "image/png",
                IOUtils.toByteArray(input));
        ImageDto imageDto = ImageDto.builder().file(multipartFile).build();
        return imageService.uploadImage(imageDto).get();
    }

}
