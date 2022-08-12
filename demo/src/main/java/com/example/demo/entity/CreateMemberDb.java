package com.example.demo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
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
public class CreateMemberDb {
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
    @Column(name="mid", unique = true, nullable = false, length = 20)
    private String mid;

    @NonNull
    @NotBlank
    @Column(name="maccount", nullable = false, length = 350)
    private String mAccount;
    
    @NonNull
    @NotBlank
    @Column(name="mpasswd", nullable = false, length = 16)
    private String mPasswd;

    @NonNull
    @NotBlank
    @Column(name="mname", nullable = false, length = 50)
    private String mName;


    @Column(name="mtelephone", nullable = true, length = 11)
    private Integer mTelephone;

    @NonNull
    @NotBlank
    @Column(name="misvalid", nullable = false, length = 2)
    private String mIsValid;

    @Column(name="createtime", nullable = false, length = 10) //yyyymmddss
    private Integer createTime;
    
    @Column(name="createuser", nullable = false, length = 10)
    private String createUser;

    @Column(name="updateTime", nullable = false, length = 10)
    private Integer updateTime;

    @Column(name="updateuser", nullable = false, length = 10)
    private String updateUser;
}
