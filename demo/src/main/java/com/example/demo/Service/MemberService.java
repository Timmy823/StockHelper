package com.example.demo.Service;

import com.example.demo.Component.GetMemberInfoParam;
import com.example.demo.Entity.LoginModel;
import com.example.demo.Entity.MemberModel;
import com.example.demo.Repository.LoginRespository;
import com.example.demo.Repository.MemberRespository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

@Service
public class MemberService {
    @Autowired
    private MemberRespository MemberRepo;
    @Autowired
    private LoginRespository LoginRepo;
    
    public MemberService() {
    }
    
    public JSONObject getMemberInfo(GetMemberInfoParam data) {
        JSONObject result = new JSONObject();
        
        //檢核會員帳號是否存在
        MemberModel member = MemberRepo.FindByAccountAndPassword(data.getAccount(),data.getPassword());
        if(member == null) {
            return responseError("會員帳號或密碼錯誤");
        }

        //add member login log data
        LoginModel loginModel = new LoginModel();
        loginModel.setMid_fk(member);
        loginModel.setCreate_user("system");
        loginModel.setUpdate_user("system");
        LoginRepo.save(loginModel);

        result.put("member_account", member.getMember_account());
        result.put("name", member.getName());
        result.put("telephone", member.getTelephone());
        result.put("member_account_verification(Y/N)", member.getIsValid().equals("99") ? "N" : "Y");
        result.put("member_account_create_timestamp", member.getCreate_time().toString());

        return responseGetMemberInfoSuccess(result);
    }

    private JSONObject responseGetMemberInfoSuccess(JSONObject data){
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
