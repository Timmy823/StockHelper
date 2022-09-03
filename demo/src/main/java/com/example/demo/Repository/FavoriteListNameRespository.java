package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.Entity.FavoriteListNameModel;

public interface FavoriteListNameRespository extends JpaRepository<FavoriteListNameModel, Long> {
    @Query(nativeQuery = true, value = "select * from favorite_list_name where member_id = ?1 and BINARY list_name = ?2")
    public FavoriteListNameModel FindByListName(String member_id, String list_name);  

    // @Modifying
    // @Transactional
    // @Query(nativeQuery = true, value = "UPDATE favorite_list_name SET status = ?3 WHERE member_id = ?1 and BINARY list_name = ?2")
    // public void updateListStatusByListName(String mid, String list_name, String status);  
}