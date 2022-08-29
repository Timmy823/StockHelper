package com.example.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import com.example.demo.Entity.FavoriteListModel;
import com.example.demo.Entity.FavoriteListId;

@Repository
public interface FavoriteListRespository extends JpaRepository<FavoriteListModel, FavoriteListId> {
    @Query(nativeQuery = true, value = "select * from favorite_list where mid = ?1 and favorite_list_name = ?2")
    public List<FavoriteListModel> FindByListName(String mid, String list_name);  
    
    @Query(nativeQuery = true, value = "select * from favorite_list where mid = ?1")
    public List<FavoriteListModel> FindListByMid(String mid);  
}
