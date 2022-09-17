package com.example.demo.Component.FavoriteListComponent;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class UpdateFavoriteListNameParam {
    @NotEmpty(message = "member_account不可為空")
    @Email(message = "必須email格式")
    private String account;

    @NotNull(message = "favorite_list_name不可為null")
    @NotEmpty(message = "favorite_list_name不可為空")
    @Size(max = 50, message = "favorite_list_name最長50")
    private String list_name;

    @NotNull(message = "new_favorite_list_name不可為null")
    @NotEmpty(message = "new_favorite_list_name不可為空")
    @Size(max = 50, message = "new_favorite_list_name最長50")
    private String new_list_name;

    public UpdateFavoriteListNameParam(String member_account, String favorite_list_name,
            String new_favorite_list_name) {
        super();
        this.account = member_account;
        this.list_name = favorite_list_name;
        this.new_list_name = new_favorite_list_name;
    }
}
