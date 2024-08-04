package com.andyestrada.crochetcreations.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @JsonManagedReference
    @OneToOne(mappedBy = "jumbotronContent", cascade = CascadeType.ALL)
    @ToString.Exclude
    private JumbotronImage image;

    @Column
    private String url;

    @Column
    private Integer priority;
}
