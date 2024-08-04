package com.andyestrada.crochetcreations.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DiscriminatorValue("Jumbotron")
public class JumbotronImage extends Image {

    public JumbotronImage(Image image) {
        this.setRemotePublicId(image.getRemotePublicId());
        this.setUrl(image.getUrl());
    }

    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "jumbotroncontent_id")
    private JumbotronContent jumbotronContent;
}
