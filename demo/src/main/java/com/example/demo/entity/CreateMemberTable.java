package com.example.demo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Entity //將這個class 認定為table User
@Data
@RequiredArgsConstructor //final 修飾變量為特定參數
@Table(name="stock_members")
public class CreateMemberTable {
    /*
     * member id: mId .format xxYYYYMMDDNNNN
     * member account: mAccount
     * 會員密碼 mPasswd
     * 會員名稱 mName
     * telephone 
     * 是否已驗證成功 
     * 
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="mid", unique = true, nullable = false, length = 20)
    private Long mid;

    @NonNull
    @NotBlank
    @Column(name="mAccount", nullable = false, length = 350)
    private String mAccount;
    
    @NonNull
    @NotBlank
    @Column(name="mPasswd", nullable = false, length = 16)
    private String mPasswd;

    @NonNull
    @NotBlank
    @Column(name="mName", nullable = false, length = 50)
    private String mName;


    @Column(name="mTelephone", nullable = true, length = 11)
    private Integer mTelephone;

    @NonNull
    @NotBlank
    @Column(name="mIsValid", nullable = false, length = 2)
    private String mIsValid;

    @Column(name="createTime", nullable = false, length = 10) //yyyymmddss
    private Integer createTime;
    
    @Column(name="createUser", nullable = false, length = 10)
    private String createUser;

    @Column(name="updateTime", nullable = true, length = 10)
    private Integer updateTime;

    @Column(name="updateUser", nullable = true, length = 10)
    private String updateUser;
}
