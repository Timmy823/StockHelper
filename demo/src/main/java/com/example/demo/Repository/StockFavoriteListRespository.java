package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.Entity.StockFavoriteListsModel;
import com.example.demo.Entity.StockFavoriteListsId;

@Repository
public interface StockFavoriteListRespository extends JpaRepository<StockFavoriteListsModel, StockFavoriteListsId> {
    @Query(nativeQuery = true, value = "select * from member where favorite_lists_name = ?1")
    public StockFavoriteListsModel FindByListName(String list_name);  
      
}
