package com.example.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.Entity.FavoriteListDetailModel;

public interface FavoriteListDetailRespository extends JpaRepository<FavoriteListDetailModel, Long> {
    @Query(nativeQuery = true, value = "select * from favorite_list_detail where BINARY list_name_id = ?1")
    public List<FavoriteListDetailModel> FindDetailByListNameId(Long list_name_id);  
}
