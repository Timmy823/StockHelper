package com.example.demo.Component;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

import net.sf.json.JSONObject;

@Data
public class MemberUpdateParam {
    @NotEmpty(message = "member_account不可為空")
    private String account;

    @SpecificValidator(objectKeys = {}, message = "update_dat參數錯誤")
    private JSONObject update_data;
    
    private String password;
    private String name;
    private String telephone;
    private String verification;

    public MemberUpdateParam(String member_account, JSONObject update_data) {
        super();
        this.account = member_account;
        this.update_data=update_data;
        this.password = update_data.getString("password");
        this.name = update_data.getString("member_name");
        this.telephone = update_data.getString("telephone");
        this.verification = update_data.getString("verification");
    }
}
