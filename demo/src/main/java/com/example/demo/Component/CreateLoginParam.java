package com.example.demo.Component;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class CreateLoginParam {
    @NotEmpty(message = "member_account不可為空")
    private String account;

    @NotNull(message = "password不可為空")
    @Size(min=9, max=16, message = "password長度需為9~16個半形字元")
    private String password;

    public CreateLoginParam(String member_account, String password){
        super();
        this.account = member_account;
        this.password = password;
    }
}
