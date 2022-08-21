package com.example.demo.Service;

import com.example.demo.Component.MemberUpdateParam;
import com.example.demo.Entity.MemberModel;
import com.example.demo.Repository.MemberRespository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

@Service
public class MemberService {
    @Autowired
    private MemberRespository MemberRepo;
    
    public MemberService() {
    }
    
    public JSONObject updateMember(MemberUpdateParam data) {
         //檢核會員帳號是否存在
        MemberModel member = MemberRepo.FindByAccount(data.getAccount());
        if(member == null) {
            return responseError("會員帳號或密碼錯誤");
        }

        //if input field not null ,and update member field
        if(data.getPassword().length() != 0)
            member.setMember_passwd(data.getPassword());
        if(data.getName().length() != 0)
            member.setName(data.getName());
        if(data.getTelephone().length() != 0)
            member.setTelephone(data.getTelephone());
        if(data.getVerification().equals("Y"))
            member.setIsValid("00");
        member.setUpdate_user("system");
        MemberRepo.save(member);

        return responseUpdateMemberSuccess();
    }

    private JSONObject responseUpdateMemberSuccess(){
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
