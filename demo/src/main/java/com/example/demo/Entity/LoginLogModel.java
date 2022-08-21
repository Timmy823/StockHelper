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
@Table(name="login_log")
@EntityListeners(AuditingEntityListener.class)
public class LoginLogModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="sequence_id", unique = true, nullable = false, length = 20)
    private Long sequence_id;

    @ManyToOne(fetch = FetchType.LAZY)//多得一方延遲載入
    @JoinColumn(name="mid_fk")
    private MemberModel mid_fk;

    @Column(name="login_type", length = 20)
    private String login_type;
        
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="create_time", nullable = false, updatable = false)
    private Date create_time;
    
    @NonNull
    @Column(name="create_user", nullable = false, updatable = false, length = 10)
    private String create_user;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="update_time", nullable = false)
    private Date update_time;

    @NonNull
    @Column(name="update_user", nullable = false, length = 10)
    private String update_user;
}    
