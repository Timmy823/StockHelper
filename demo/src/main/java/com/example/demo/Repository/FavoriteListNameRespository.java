package com.example.demo.Repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.Entity.FavoriteListNameModel;

public interface FavoriteListNameRespository extends JpaRepository<FavoriteListNameModel, Long> {
    @Query(nativeQuery = true, value = "select * from favorite_list_name where BINARY member_id = ?1")
    public ArrayList<FavoriteListNameModel> FindListByMember(String member_id);  

    @Query(nativeQuery = true, value = "select * from favorite_list_name where BINARY member_id = ?1 and BINARY list_name = ?2")
    public ArrayList<FavoriteListNameModel> FindByListName(String member_id, String list_name);  
}