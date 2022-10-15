package com.example.demo.Component;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class SendEmailParam {
    @NotEmpty(message = "member_account不可為空")
    @Email(message = "member_account必須email格式")
    private String account;

    @NotEmpty(message = "certification_code不可為空")
    @NotNull(message = "certification_code為必填")
    private String certification;

    public SendEmailParam(String member_account, String certification_code) {
        super();
        this.account = member_account;
        this.certification = certification_code;
    }
}
