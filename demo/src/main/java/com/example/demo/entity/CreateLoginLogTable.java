package com.example.demo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity //將這個class 認定為table User
@Data
@RequiredArgsConstructor //final 修飾變量為特定參數
@Table(name="stock_login_log")
public class CreateLoginLogTable {
    /*
    * sequence number: seqId.format=YYYYMMDDSSNNNN
    * mid
    * login type:
    *
    */   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="login_seqId", unique = true, nullable = false, length = 20)
    private Long seqId;

    @ManyToOne(fetch = FetchType.LAZY)//多得一方延遲載入
    @JoinColumn(name="mid_fk")
    private CreateMemberTable mid;

    @Column(name="login_type", nullable = true, length = 20)
    private String type;
        
    @Column(name="login_createTime", nullable = false, length = 10) //yyyymmddss
    private Integer createTime;
    
    @Column(name="login_createUser", nullable = false, length = 10)
    private String createUser;

    @Column(name="login_updateTime", nullable = true, length = 10)
    private Integer updateTime;

    @Column(name="login_updateUser", nullable = true, length = 10)
    private String updateUser;
}
