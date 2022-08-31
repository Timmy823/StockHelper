package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.Entity.FavoriteListDetailModel;

public interface FavoriteListDetailRespository extends JpaRepository<FavoriteListDetailModel, Long> {
    @Query(nativeQuery = true, value = "select * from list_name_id where BINARY list_name = ?1 AND stock_id = ?2")
    public FavoriteListDetailModel FindByListNameId(Long list_name_id, String stock_id);  
}
