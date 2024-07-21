package com.andyestrada.crochetcreations.services;

import com.andyestrada.crochetcreations.dto.JumbotronContentDto;
import com.andyestrada.crochetcreations.entities.JumbotronContent;

import java.util.List;
import java.util.Optional;

public interface JumbotronContentService {
    Optional<List<JumbotronContent>> getAll();
    JumbotronContent save(JumbotronContentDto jContentDto);
}
