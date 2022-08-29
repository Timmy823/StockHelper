package com.example.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Entity.FavoriteListModel;
import com.example.demo.Entity.FavoriteListId;

@Repository
public interface FavoriteListRespository extends JpaRepository<FavoriteListModel, FavoriteListId> {
    @Query(nativeQuery = true, value = "select * from favorite_list where mid = ?1 and favorite_list_name = ?2")
    public List<FavoriteListModel> FindByListName(String mid, String list_name);  
    
    @Query(nativeQuery = true, value = "select * from favorite_list where mid = ?1 and favorite_list_name = ?2 and stock_id = ?3")
    public FavoriteListModel FindByListNameAndStockId(String mid, String list_name, String stock_id);  
    
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "UPDATE favorite_list SET favorite_list_name = ?4 WHERE mid = ?1 and favorite_list_name = ?2 and stock_id = ?3")
    public void updateNewNameByCompositePK(String mid, String list_name, String stock_id, String new_list_name);  
    
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "UPDATE favorite_list SET stock_id = ?4 WHERE mid = ?1 and favorite_list_name = ?2 and stock_id = ?3")
    public void updateStockId(String mid, String list_name, String stock_id, String new_id);  
}
