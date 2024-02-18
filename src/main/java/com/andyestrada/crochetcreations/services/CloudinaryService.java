package com.andyestrada.crochetcreations.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CloudinaryService {
    public Map<String, String> uploadFile(MultipartFile file, String folderName);
    public String deleteFile(String publicId);
}
