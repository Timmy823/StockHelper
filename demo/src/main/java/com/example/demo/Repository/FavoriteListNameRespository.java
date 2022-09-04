package com.example.demo.Repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.Entity.FavoriteListNameModel;

public interface FavoriteListNameRespository extends JpaRepository<FavoriteListNameModel, Long> {
    @Query(nativeQuery = true, value = "select * from favorite_list_name where BINARY member_id = ?1 and BINARY list_name = ?2")
    public ArrayList<FavoriteListNameModel> FindListByListName(String member_id, String list_name);
    
    @Query(nativeQuery = true, value = "select * from favorite_list_name where BINARY member_id = ?1")
    public ArrayList<FavoriteListNameModel> FindListByMemberId(String member_id);
}
