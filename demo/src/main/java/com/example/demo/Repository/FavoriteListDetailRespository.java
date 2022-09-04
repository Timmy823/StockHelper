package com.example.demo.Repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.Entity.FavoriteListDetailModel;

public interface FavoriteListDetailRespository extends JpaRepository<FavoriteListDetailModel, Long> {
    @Query(nativeQuery = true, value = "select * from favorite_list_detail where BINARY list_name_id = ?1")
    public ArrayList<FavoriteListDetailModel> FindDetailByListNameId(Long list_name_id);  

    @Query(nativeQuery = true, value = "select * from favorite_list_detail where BINARY list_name_id = ?1 AND stock_id = ?2")
    public ArrayList<FavoriteListDetailModel> FindDetailByListStock(Long list_name_id, String stock_id);  
}