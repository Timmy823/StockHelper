package com.example.demo.Service;


import com.example.demo.Component.MemberRegisterParam;
import com.example.demo.Component.GetMemberInfoParam;

import com.example.demo.Entity.MemberModel;
import com.example.demo.Entity.LoginLogModel;

import com.example.demo.Repository.MemberRespository;
import com.example.demo.Repository.LoginLogRespository;

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
    private LoginLogRespository LoginLogRepo;
    
    public MemberService() {
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
    
    public JSONObject getMemberInfo(GetMemberInfoParam data) {
        JSONObject response_data = new JSONObject();
        
        //檢核會員帳號是否存在
        MemberModel member = MemberRepo.FindByAccountAndPassword(data.getAccount(),data.getPassword());
        if(member == null) {
            return responseError("會員帳號或密碼錯誤");
        }

        //add member login log data
        LoginLogModel loginlogModel = new LoginLogModel();
        loginlogModel.setMid_fk(member);
        loginlogModel.setCreate_user("system");
        loginlogModel.setUpdate_user("system");
        LoginLogRepo.save(loginlogModel);

        response_data.put("member_account", member.getMember_account());
        response_data.put("name", member.getName());
        response_data.put("telephone", member.getTelephone());
        response_data.put("member_account_verification(Y/N)", member.getIsValid().equals("99") ? "N" : "Y");
        response_data.put("member_account_create_timestamp", member.getCreate_time().toString());

        return responseGetMemberInfoSuccess(response_data);
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
