package com.example.demo.Service;

import com.example.demo.Component.MemberRegisterParam;
import com.example.demo.Component.StockFavoriteListParam;
import com.example.demo.Entity.StockFavoriteListsModel;
import com.example.demo.Entity.StockFavoriteListsId;
import com.example.demo.Entity.MemberModel;
import com.example.demo.Repository.MemberRespository;
import com.example.demo.Repository.StockFavoriteListRespository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Data;
import net.sf.json.JSONObject;

@Data
@Service
public class MemberService {
    @Autowired
    private MemberRespository MemberRepo;
    private StockFavoriteListRespository FavoriteRepo;
    public MemberService() {
    }
    
    public JSONObject updateStockFavoriteList(StockFavoriteListParam data) {
        MemberModel memberModel = new MemberModel();
        //檢核會員帳號是否存在
        if((memberModel = MemberRepo.FindByAccount(data.getMember_account())) == null) {
            return responseError("會員帳號尚未建立");
        }

        if(FavoriteRepo.FindByListName(data.getFavorite_list_name()) == null) {
            StockFavoriteListsModel favoriteModel = new StockFavoriteListsModel();
            StockFavoriteListsId favoriteId = new StockFavoriteListsId();
            favoriteId.setMid(memberModel.getMid());
            favoriteId.setFavorite_list_name(data.getFavorite_list_name());
            favoriteModel.setFavoriteListsId(favoriteId);
            favoriteModel.setStock_id(data.getStock_id());
            favoriteModel.setStock_name(data.getStock_name());
            favoriteModel.setComments_string(data.getComments_string());
            favoriteModel.setCreate_user("system");
            favoriteModel.setUpdate_user("system");
            FavoriteRepo.save(favoriteModel);
        }else{

        }
        return responseCreateMemberSuccess();
    }
    
    public JSONObject createMember(MemberRegisterParam data) {
        //檢核會員帳號是否存在
        if((MemberRepo.FindByAccount(data.getAccount())) != null) {
            return responseError("會員帳號已創建");
        }

        //add member data
        MemberModel memberModel = new MemberModel();
        memberModel.setMember_account(data.getAccount());
        memberModel.setName(data.getName());
        memberModel.setMember_passwd(data.getPassword());
        memberModel.setTelephone(data.getTelephone());
        memberModel.setIsValid("99");
        memberModel.setCreate_user("system");
        memberModel.setUpdate_user("system");
        MemberRepo.save(memberModel);

        return responseCreateMemberSuccess();
    }

    private JSONObject responseCreateMemberSuccess() {
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();

        status_code.put("status", "success");
        status_code.put("desc", "");

        result.put("metadata", status_code);
        result.put("data", data);
        return result;
    }

    public JSONObject responseError(String error_msg) {
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();
    
        data.put("data","");
        
        status_code.put("status", "error");
        status_code.put("desc", error_msg);
    
        result.put("metadata", status_code);
        result.put("data", data);
        return result;
    }
}
