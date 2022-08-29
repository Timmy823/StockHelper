package com.example.demo.Service;

import com.example.demo.Component.GetFavoriteListParam;
import com.example.demo.Component.MemberRegisterParam;
import com.example.demo.Entity.FavoriteListModel;
import com.example.demo.Entity.MemberModel;
import com.example.demo.Repository.FavoriteListRespository;
import com.example.demo.Repository.MemberRespository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Data;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Data
@Service
public class MemberService {
    @Autowired
    private MemberRespository MemberRepo;
    @Autowired
    private FavoriteListRespository favoriteListRespository;
    public MemberService() {
    }
    
    public JSONObject getFavoriteList(GetFavoriteListParam data) {
        MemberModel memberModel = new MemberModel();
        List<FavoriteListModel> favoriteModel_list = new ArrayList<FavoriteListModel>();
        JSONArray result_array= new JSONArray();

        //Check member exists ,and get mid.
        if((memberModel = MemberRepo.FindByAccount(data.getAccount())) == null) {
            return responseError("會員帳號尚未建立");
        }

        favoriteModel_list = data.getList_name().equals("") 
            ? favoriteListRespository.FindListByMid(memberModel.getMid()) 
            : favoriteListRespository.FindByListName(memberModel.getMid(), data.getList_name());
            
        if(favoriteModel_list.size() == 0) {
            return responseError("查無資料");
        }

        for (FavoriteListModel items : favoriteModel_list) {
            JSONObject object = new JSONObject();
            object.element("list_name", items.getFavoriteListsId().getFavorite_list_name());
            object.element("stock_id", items.getFavoriteListsId().getStock_id());
            object.element("stock_name", items.getStock_name());
            object.element("comments", items.getComments_string());
            
            result_array.add(object);
        }
        return responseJSONArraySuccess(result_array);
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

        return responseSuccess();
    }
    
    private JSONObject responseJSONArraySuccess(JSONArray data) {
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();

        status_code.put("status", "success");
        status_code.put("desc", "");

        result.put("metadata", status_code);
        result.put("data", data);
        return result;
    }

    private JSONObject responseSuccess() {
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
