package com.example.demo.Entity;

import lombok.*;

import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Data
@Embeddable
public class FavoriteListId implements Serializable {
    @Column(name="mid", nullable = false, columnDefinition = "BINARY(36)")
    private String mid;

    @NonNull
    @NotBlank
    @Column(name="favorite_list_name", nullable = false, length = 30)
    private String favorite_list_name;

    @Column(name="stock_id", length = 15)
    private String stock_id;

    public FavoriteListId(String mid, String favorite_list_name, String stock_id) {
        this.mid = mid;
        this.favorite_list_name = favorite_list_name;
        this.stock_id = stock_id;
    }

    public FavoriteListId(){
    }
}
