package com.example.demo.Component;
import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class GetMemberInfoParam {
    @NotEmpty(message = "member_account不可為空")
    private String account;

    @NotEmpty(message = "password不可為空")
    private String password;

    public GetMemberInfoParam(String member_account, String password){
        super();
        this.account = member_account;
        this.password = password;
    }
}
