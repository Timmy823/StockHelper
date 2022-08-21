package com.example.demo.Entity;

import lombok.*;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;

@Component
@Entity
@Data
@RequiredArgsConstructor //final 修飾變量為特定參數
@Table(name="member")
@EntityListeners(AuditingEntityListener.class)
public class MemberModel {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name="mid", unique = true, columnDefinition = "BINARY(36)")
    private String mid;

    @NonNull
    @NotBlank
    @Column(name="member_account", nullable = false, length = 350)
    private String member_account;
    
    @NonNull
    @NotBlank
    @Column(name="member_passwd", nullable = false, length = 16)
    private String member_passwd;

    @NonNull
    @NotBlank
    @Column(name="name", nullable = false, length = 50)
    private String name;

    @Column(name="telephone", nullable = true, length = 11)
    private String telephone;

    @NonNull
    @NotBlank
    @Column(name="is_valid", nullable = false, length = 2)
    private String IsValid;

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

    @Column(name="update_user", updatable = true, nullable = true, length = 10)
    private String update_user;

    public MemberModel() {
    }
}
