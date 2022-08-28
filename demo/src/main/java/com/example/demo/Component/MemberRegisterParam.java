package com.example.demo.Component;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class MemberRegisterParam {
    @NotEmpty(message = "member_account不可為空")
    @Email(message = "必須email格式")
    private String account;

    private String login_type;

    @NotEmpty(message = "password不可為空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[\\w]{9,16}$", message = "密碼必須為長度9~16位碼大小寫英文加數字")
    private String password;

    @NotEmpty(message = "name不可為空")
    @Size(max=50, message = "name長度最多50")
    private String name;

    @Pattern(regexp = "(09)+[\\d]{8}", message = "telephone為09開頭+8長數字")
    private String telephone;

    public MemberRegisterParam(String member_account, String login_type, String password, String member_name, String telephone) {
        super();
        this.account = member_account;
        this.login_type = login_type;
        this.password = password;
        this.name = member_name;
        this.telephone = telephone;
    }
}
