package com.andyestrada.crochetcreations.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    @Autowired
    private final Cloudinary cloudinary;

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudinaryServiceImpl.class);

    @Override
    public Map<String, String> uploadFile(MultipartFile file, String folderName) {
        try {
            Map<Object, Object> options = new HashMap<>();
            options.put("folder", folderName);
            Map uploadedFile = cloudinary.uploader().upload(file.getBytes(), options);
            String publicId = (String) uploadedFile.get("public_id");
            String url = cloudinary.url().secure(true).generate(publicId);
            return new HashMap<String, String>() {{
                put("publicId", publicId);
                put("url", url);
            }};
        } catch (IOException e) {
            LOGGER.error("An error occurred while trying to upload a file.", e);
            return null;
        }
    }

    @Override
    public String deleteFile(String publicId) {
        String result = "";
        try {
            result = (String) cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap()).get("result");
        } catch (Exception e) {
            LOGGER.error("CloudinaryServiceImpl::deleteFile | An exception occurred while trying to delete resource.", e);
        }
        return result;
    }

}
