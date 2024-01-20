package com.andyestrada.crochetcreations.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/resource")
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<String> welcome() {
        return ResponseEntity.ok("Successfully accessed resource.");
    }
}
