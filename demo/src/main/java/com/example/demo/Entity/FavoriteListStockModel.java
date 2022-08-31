package com.example.demo.Entity;

import lombok.Data;
import java.util.Date;
import javax.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;

@Component
@Entity
@Data
@Table(name = "favorite_list_detail")
@EntityListeners(AuditingEntityListener.class)
public class FavoriteListStockModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "list_detail_id", unique = true)
    private Long list_detail_id;

    @Column(name = "list_identify", nullable = false, columnDefinition = "BINARY(36)")
    private String list_identify;

    @Column(name = "stock_id", nullable = false, length = 15)
    private String stock_id;

    @Column(name = "stock_name", nullable = false, length = 50)
    private String stock_name;

    @Column(name = "comment", length = 200)
    private String comment;

    @Column(name = "status", nullable = false, length = 1)
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
