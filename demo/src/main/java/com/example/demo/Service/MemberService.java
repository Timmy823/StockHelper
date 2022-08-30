package com.example.demo.Service;

import com.example.demo.Component.GetMemberInfoParam;
import com.example.demo.Entity.LoginModel;
import com.example.demo.Entity.MemberModel;
import com.example.demo.Repository.LoginRespository;
import com.example.demo.Repository.MemberRespository;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

@Service
public class MemberService {
    String [] member_valuesStrings = {"UUID", "account", "name", "telephone", "verification", "timestamp"};
    
    @Autowired
    private MemberRespository MemberRepo;
    @Autowired
    private LoginRespository LoginRepo;
    
    public MemberService() {
    }
    
    public JSONObject getMemberInfo(GetMemberInfoParam data) {
        HashMap<String, String> member_map = new HashMap<String, String>();
        for(int i=0; i<member_valuesStrings.length; i++){
            member_map.put(member_valuesStrings[i], new String());
        }
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

        //add response values to map
        member_map.put("UUID", member.getMid().toString());
        member_map.put("account", member.getMember_account());
        member_map.put("name", member.getName());
        member_map.put("telephone", member.getTelephone());
        member_map.put("verification", member.getIsValid().equals("99") ? "N" : "Y");
        member_map.put("timestamp", member.getCreate_time().toString());

        return responseGetMemberInfoSuccess(member_map);
    }

    private JSONObject responseGetMemberInfoSuccess(HashMap<String, String> member_map){
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();
        String [] object_keyString = {"member_UUID", "member_account", "name", "telephone"
        , "member_account_verification(Y/N)", "member_account_create_timestamp"};
        
        for(int i=0; i<object_keyString.length; i++) {
            data.put(object_keyString[i], member_map.get(member_valuesStrings[i]));
        }
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
