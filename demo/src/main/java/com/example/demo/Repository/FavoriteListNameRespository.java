package com.example.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.Entity.FavoriteListNameModel;

public interface FavoriteListNameRespository extends JpaRepository<FavoriteListNameModel, Long> {
    @Query(nativeQuery = true, value = "select * from favorite_list_name where member_id = ?1")
    public List<FavoriteListNameModel> FindListByMember(String member_id);  
}
