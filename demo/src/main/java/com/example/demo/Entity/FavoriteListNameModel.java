package com.example.demo.Entity;

import lombok.Data;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;

@Component
@Entity
@Data
@Table(name = "favorite_list_name")
@EntityListeners(AuditingEntityListener.class)
public class FavoriteListNameModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "list_name_id", unique = true)
    private Long list_name_id;

    @Column(name = "member_id", nullable = false, columnDefinition = "BINARY(36)")
    private String member_id;

    @Column(name = "list_name",nullable = false, length = 30)
    private String favorite_list_name;

    @Column(name = "status",nullable = false, length = 1)
    private String status;
    
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time", nullable = false, updatable = false)
    private Date create_time;

    @NotEmpty
    @NotBlank
    @Column(name = "create_user", nullable = false, updatable = false, length = 10)
    private String create_user;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time", nullable = false, updatable = true)
    private Date update_time;

    @NotEmpty
    @NotBlank
    @Column(name = "update_user", nullable = false, updatable = true, length = 10)
    private String update_user;
}
