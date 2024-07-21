package com.andyestrada.crochetcreations.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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

    @OneToOne
    @JoinColumn(name = "jumbotroncontent_id")
    private JumbotronContent jumbotronContent;

}
