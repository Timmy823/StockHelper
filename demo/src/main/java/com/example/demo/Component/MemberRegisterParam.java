package com.example.demo.Component;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class MemberRegisterParam {
    @NotEmpty(message = "member_account不可為空")
    private String account;

    private String login_type;

    @NotNull(message = "password不可為空")
    @Size(min=9, max=16, message = "password長度需為9~16個半形字元")
    private String password;

    @NotNull(message = "password不可為空")
    @Size(max=50, message = "name長度最多50")
    private String name;

    @Size(max=11, message = "telephone長度最多11")
    private String telephone;

    public MemberRegisterParam(String member_account,String login_type, String password, String member_name, String telephone){
        super();
        this.account = member_account;
        this.login_type = login_type;
        this.password = password;
        this.name = member_name;
        this.telephone = telephone;
    }
}
