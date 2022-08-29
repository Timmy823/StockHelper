package com.example.demo.Service;

import com.example.demo.Component.MemberRegisterParam;
import com.example.demo.Component.FavoriteListParam;
import com.example.demo.Entity.FavoriteListModel;
import com.example.demo.Entity.FavoriteListId;
import com.example.demo.Entity.MemberModel;
import com.example.demo.Repository.MemberRespository;
import com.example.demo.Repository.FavoriteListRespository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Data;
import net.sf.json.JSONObject;

@Data
@Service
public class MemberService {
    @Autowired
    private MemberRespository MemberRepo;
    @Autowired
    private FavoriteListRespository FavoriteRepo;
    public MemberService() {
    }
    
    public JSONObject updateFavoriteList(FavoriteListParam data) {
        String search_list_name = data.getNew_list_name();
        //是否有指定id，無指定status為99，反之00
        boolean isStockId = !data.getStock_id().equals("");
        String isValid = isStockId ? "00" : "99";
        MemberModel memberModel = new MemberModel();
        FavoriteListModel favoriteModel = new FavoriteListModel();
        //StockFavoriteListsId = mid + list name + stock id 
        FavoriteListId favoriteId = new FavoriteListId();
        //search each old list name data
        List<FavoriteListModel> favoriteModel_list = new ArrayList<FavoriteListModel>();

        //Check member exists ,and get mid.
        if((memberModel = MemberRepo.FindByAccount(data.getAccount())) == null) {
            return responseError("會員帳號尚未建立");
        }
        //initail update data composite primary key by input param
        favoriteId.setFavorite_list_name(data.getList_name());
        favoriteId.setMid(memberModel.getMid());
        favoriteId.setStock_id(data.getStock_id());

        //如果會員沒有任何相同list名稱，並確認無相同資料mid+listname+stockid 才可以insert a new data
        favoriteModel_list = FavoriteRepo.FindByListName(memberModel.getMid(), data.getList_name());
        favoriteModel = FavoriteRepo.FindByListNameAndStockId(memberModel.getMid(), data.getList_name(), data.getStock_id());
        if(favoriteModel == null) {
            if(isStockId || (!isStockId && favoriteModel_list.size() == 0)) {  
                //已有空白id的list，異動至new stock id
                favoriteModel = FavoriteRepo.FindByListNameAndStockId(memberModel.getMid(), data.getList_name(), "");    
                if(favoriteModel != null) {
                    FavoriteRepo.updateStockId(favoriteModel.getFavoriteListsId().getMid(), favoriteModel.getFavoriteListsId().getFavorite_list_name(), "", data.getStock_id());
                    favoriteModel.getFavoriteListsId().setStock_id(data.getStock_id());
                    updateFavoriteListField(favoriteModel, data, isValid);
                    return responseSuccess();
                }
                //若尚無建立list，新建一筆
                favoriteId.setStock_id(data.getStock_id());
                addFavoriteList(favoriteId, data, isValid);
                return responseSuccess();
            }
        }

        //前面已經建立stock_id為空的新list_name 或者 favoriteModel!=null，且無指定id、新list名稱，就無需update data
        if(search_list_name.equals("")) {
            if(!isStockId) {
                return responseError("無指定stock id，且favorite list name : \""+data.getList_name()+"\" 重複，無異動");
            }
            //資料已存在，沒有更新新名稱，且各欄位資料相同，則無異動
            if(isStockId && favoriteModel.getList_status().equals(isValid) && favoriteModel.getStock_name().equals(data.getStock_name()) && favoriteModel.getComments_string().equals(data.getComments_string())) {
                return responseError("與資料庫欄位皆相同，無異動");
            }
        }
    
        //update each old list name data into new list name.
        if(!search_list_name.equals("")) {
            //if new list name exist, return false
            if(FavoriteRepo.FindByListName(memberModel.getMid(), search_list_name).size() != 0) {
                return responseError("new favorite list name \""+ search_list_name + "\" 已創建，無異動");
            }
            //iterate all old name list, and update into new name
            search_list_name = data.getList_name();
            favoriteModel_list = FavoriteRepo.FindByListName(memberModel.getMid(), search_list_name);
            for(FavoriteListModel subList : favoriteModel_list) {
                FavoriteRepo.updateNewNameByCompositePK(subList.getFavoriteListsId().getMid(), subList.getFavoriteListsId().getFavorite_list_name(), subList.getFavoriteListsId().getStock_id(), data.getNew_list_name());
            }
        }

        //有指定stock id才可以update data each elements{stock name, comments string}
        if(isStockId) {
            search_list_name = data.getNew_list_name().equals("") ? data.getList_name() : data.getNew_list_name();
            favoriteModel = FavoriteRepo.FindByListNameAndStockId(memberModel.getMid(), search_list_name, data.getStock_id());
            updateFavoriteListField(favoriteModel, data, isValid);
        }

        return responseSuccess();
    }

    private void updateFavoriteListField(FavoriteListModel favoriteModel, FavoriteListParam data, String status) {
        favoriteModel.setList_status(status);
        favoriteModel.setStock_name(data.getStock_name());
        favoriteModel.setComments_string(data.getComments_string());
        FavoriteRepo.save(favoriteModel);
    }

    private void addFavoriteList(FavoriteListId favoriteId, FavoriteListParam data, String status) {
        // add a new favorite list.
        FavoriteListModel favorite_result_model = new FavoriteListModel();
        favorite_result_model.setFavoriteListsId(favoriteId);

        if(!data.getStock_id().equals("")){
            favorite_result_model.setStock_name(data.getStock_name());
            favorite_result_model.setComments_string(data.getComments_string());
        }
        favorite_result_model.setList_status(status);
        favorite_result_model.setCreate_user("system");
        favorite_result_model.setUpdate_user("system");
        FavoriteRepo.save(favorite_result_model);
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
