package com.example.demo.Entity;

import lombok.*;
import java.util.Date;

import javax.persistence.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.stereotype.Component;

@Component
@Entity //將這個class 認定為table User
@Data
@Table(name="login")
@EntityListeners(AuditingEntityListener.class)
public class LoginModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="sequence_id", unique = true, nullable = false, length = 20)
    private Long sequence_id;

    @ManyToOne(fetch = FetchType.LAZY)//多得一方延遲載入
    @JoinColumn(name="mid_fk")
    private MemberModel mid_fk;

    @Column(name="login_type", nullable = true, length = 20)
    private String type;
        
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="create_time", updatable = false)
    private Date create_time;
    
    @Column(name="create_user", updatable = false, nullable = true, length = 10)
    private String create_user;

    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    @Column(name="update_time")
    private Date update_time;

    @Column(name="update_user", nullable = true, length = 10, updatable=true)
    private String update_user;
}    
