package com.example.demo.Component.MemberComponent;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class FavoriteListNameParam {
    @NotEmpty(message = "member_account不可為空")
    @Email(message = "必須email格式")
    private String account;

    @NotEmpty(message = "favorite_list_name不可為空")
    @NotNull(message = "favorite_list_name不可為null")
    @Size(max = 50, message="favorite_list_name最長50")
    private String list_name;
    
    public FavoriteListNameParam(String member_account, String favorite_list_name) {
        super();
        this.account = member_account;
        this.list_name = favorite_list_name;
    }
}
