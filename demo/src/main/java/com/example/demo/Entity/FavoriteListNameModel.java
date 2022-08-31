package com.example.demo.Entity;

import lombok.Data;
import java.util.Date;
import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
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
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "list_identify", unique = true, columnDefinition = "BINARY(36)")
    private String list_identify;

    @Column(name = "member_id", nullable = false, columnDefinition = "BINARY(36)")
    private String member_id;

    @Column(name = "list_name",nullable = false, length = 30)
    private String favorite_list_name;

    @Column(name = "status",nullable = false, length = 1)
    private String status;
    
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
