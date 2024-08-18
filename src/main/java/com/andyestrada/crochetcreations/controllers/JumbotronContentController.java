package com.andyestrada.crochetcreations.controllers;

import com.andyestrada.crochetcreations.dto.JumbotronContentDto;
import com.andyestrada.crochetcreations.entities.JumbotronContent;
import com.andyestrada.crochetcreations.services.JumbotronContentService;
import org.apache.coyote.Response;
import org.apache.http.client.HttpResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/jumbotron/contents")
public class JumbotronContentController {

    private final JumbotronContentService jumbotronContentService;

    @Autowired
    public JumbotronContentController(JumbotronContentService jumbotronContentService) {
        this.jumbotronContentService = jumbotronContentService;
    }

    @GetMapping
    public ResponseEntity<List<JumbotronContent>> getJumbotronContents() {
        Optional<List<JumbotronContent>> jContentsOptional = jumbotronContentService.getAll();
        return jContentsOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.ok(new ArrayList<>()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JumbotronContent> getJumbotronContent(@PathVariable long id) {
        Optional<JumbotronContent> jContentOptional = jumbotronContentService.findById(id);
        return jContentOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<JumbotronContent> saveJumbotronContent(@RequestBody JumbotronContentDto jContentDto) {
        try {
            JumbotronContent savedJContent = jumbotronContentService.save(jContentDto);
            return ResponseEntity.ok(savedJContent);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PatchMapping
    public ResponseEntity<List<JumbotronContent>> updateJumbotronContents(@RequestBody List<JumbotronContentDto> jContents) {
        try {
            List<JumbotronContent> jumbotronContents = jumbotronContentService.updateAll(jContents).orElseThrow();
            return ResponseEntity.ok(jumbotronContents);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<JumbotronContent> updateJumbotronContent(@PathVariable long id,
                                                                   @RequestBody JumbotronContentDto jContentDto) {
        try {
            JumbotronContent savedJContent = jumbotronContentService.update(id, jContentDto);
            return ResponseEntity.ok(savedJContent);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteJumbotronContent(@PathVariable long id) {
        Boolean isJContentDeleted = jumbotronContentService.delete(id);
        return ResponseEntity.ok(isJContentDeleted);
    }

}
