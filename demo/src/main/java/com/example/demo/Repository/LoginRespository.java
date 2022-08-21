package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entity.LoginLogModel;

//JpaRepository 參數第一個是放連到的model，第二個是他的主鍵key格式
@Repository
public interface LoginRespository extends JpaRepository<LoginLogModel, Long> {
}
