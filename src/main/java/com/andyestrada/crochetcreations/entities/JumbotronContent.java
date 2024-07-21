package com.andyestrada.crochetcreations.entities;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "JUMBOTRON_CONTENTS")
public class JumbotronContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "jumbotronContent", cascade = CascadeType.ALL)
    @ToString.Exclude
    private JumbotronImage image;

    private String url;
    private Integer priority;
}
