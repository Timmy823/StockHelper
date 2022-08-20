package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.Entity.MemberModel;

//JpaRepository 參數第一個是放連到的model，第二個是他的主鍵key格式
@Repository
public interface MemberRespository extends JpaRepository<MemberModel, String> {
    @Query(nativeQuery = true, value = "select count(*) from stock_members where mid = ?1")
    public long existByUUID(String uuid);

    @Query(nativeQuery = true, value = "select count(*) from stock_members where member_account = ?1 ;")
    public long existByAccount(String account);

    @Query(nativeQuery = true, value = "select * from stock_members where member_account = ?1")
    public MemberModel FindByAccount(String account);

    @Query(nativeQuery = true, value = "select * from stock_members where member_account = ?1 and member_passwd = ?2")
    public MemberModel FindByAccountAndPassword(String account, String password);
}
