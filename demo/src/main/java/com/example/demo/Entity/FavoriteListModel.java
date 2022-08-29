package com.example.demo.Entity;

import lombok.*;

import java.util.Date;
import javax.persistence.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;

@Component
@Entity
@Data
@Table(name="favorite_list")
@EntityListeners(AuditingEntityListener.class)
public class FavoriteListModel {

    @EmbeddedId
    private FavoriteListId favoriteListsId;

    @Column(name="stock_name", length = 50)
    private String stock_name;

    @Column(name="list_status", length = 2)
    private String list_status;

    @Column(name="comments", length = 200)
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
