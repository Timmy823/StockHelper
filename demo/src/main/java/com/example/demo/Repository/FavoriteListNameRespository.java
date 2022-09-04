package com.example.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.Entity.FavoriteListNameModel;

public interface FavoriteListNameRespository extends JpaRepository<FavoriteListNameModel, Long> {
    @Query(nativeQuery = true, value = "select * from favorite_list_name where BINARY member_id = ?1")
    public List<FavoriteListNameModel> FindAllListByMember(String member_id);  
}
