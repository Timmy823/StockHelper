package com.example.demo.Entity;

import lombok.*;

import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Data
@Embeddable
public class StockFavoriteListsId implements Serializable {
    @Column(name="mid", columnDefinition = "BINARY(36)")
    private String mid;

    @NonNull
    @NotBlank
    @Column(name="favorite_list_name", nullable = false, length = 30)
    private String favorite_list_name;

    // public StockFavoriteListsId(String mid, String favorite_list_name) {
    //     this.mid = mid;
    //     this.favorite_list_name = favorite_list_name;
    // }
    public StockFavoriteListsId(){
    }
}
