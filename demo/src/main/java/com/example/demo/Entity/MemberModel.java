package com.example.demo.Entity;

import lombok.Data;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;

@Component
@Entity
@Data
@Table(name="member")
@EntityListeners(AuditingEntityListener.class)
public class MemberModel {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name="mid", unique = true, columnDefinition = "BINARY(36)")
    private String mid;

    @NotEmpty
    @Column(name="member_account", nullable = false, length = 350)
    private String member_account;
    
    @NotEmpty
    @Column(name="member_passwd", nullable = false, length = 16)
    private String member_passwd;

    @NotEmpty
    @Column(name="name", nullable = false, length = 50)
    private String name;

    @Column(name="telephone", length = 11)
    private String telephone;

    @NotEmpty
    @Column(name="is_valid", nullable = false, length = 2)
    private String IsValid;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="create_time", nullable = false, updatable = false)
    private Date create_time;
    
    @NotEmpty
    @Column(name="create_user", nullable = false, updatable = false, length = 10)
    private String create_user;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="update_time", nullable = false, updatable = true)
    private Date update_time;

    @NotEmpty
    @Column(name="update_user", nullable = false, updatable = true, length = 10)
    private String update_user;

    public MemberModel() {
    }
}
