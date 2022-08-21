package com.example.demo.Entity;

import lombok.*;

import com.example.demo.Entity.FavoriteListsId;

import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;

@Entity
@Data
@RequiredArgsConstructor //final 修飾變量為特定參數
@Table(name="stock_favorite")
@EntityListeners(AuditingEntityListener.class)
public class FavoriteListsModel {

    @EmbeddedId
    private FavoriteListsId favoriteListsId;

    @NonNull
    @NotBlank
    @Column(name="favorite_lists_name", nullable = false, length = 30)
    private String favorite_lists_name;

    @Column(name="stock_id", nullable = false, length = 4)
    private String stock_id;

    @Column(name="stock_name", nullable = false, length = 50)
    private String stock_name;

    @Column(name="comments", nullable = true, length = 200)
    private String comments_string;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="create_time", updatable = false)
    private Date create_time;
    
    @Column(name="create_user", updatable = false, nullable = true, length = 10)
    private String create_user;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="update_time")
    private Date update_time;

    @Column(name="update_user", updatable=true, nullable = true, length = 10)
    private String update_user;
}
