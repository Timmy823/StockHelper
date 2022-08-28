package com.example.demo.Entity;

import lombok.*;

import java.util.Date;
import javax.persistence.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Data
@Table(name="stock_favorite_list")
@EntityListeners(AuditingEntityListener.class)
public class StockFavoriteListsModel {

    @EmbeddedId
    private StockFavoriteListsId favoriteListsId;

    @Column(name="stock_id", nullable = false, length = 15)
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
