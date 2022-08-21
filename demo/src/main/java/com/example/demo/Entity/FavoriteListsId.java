package com.example.demo.Entity;

import lombok.Data;

import java.io.Serializable;

import javax.persistence.*;

@Data
@Embeddable
public class FavoriteListsId implements Serializable {
    @Column(name="mid", columnDefinition = "BINARY(36)")
    private String mid;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="sequence_id", nullable = false, length = 20)
    private Long sequence_id;

    public FavoriteListsId(String mid, Long seq_id) {
        this.mid = mid;
        this.sequence_id = seq_id;
    }
}
