package com.example.demo.Component;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class FavoriteListParam {
    @NotEmpty(message = "member_account不可為空")
    @Email(message = "必須email格式")
    private String account;

    @NotEmpty(message = "favorite_list_name不可為空")
    @Size(max = 50, message="favorite_list_name最長50")
    private String list_name;

    private String new_list_name;

    private String stock_id;

    private String stock_name;

    private String comments_string;

    public FavoriteListParam(String member_account, String favorite_list_name, String new_favorite_list_name, String stock_id, String stock_name, String comments_string) {
        super();
        this.account = member_account;
        this.list_name = favorite_list_name;
        this.new_list_name= new_favorite_list_name;
        this.stock_id = stock_id;
        this.stock_name = stock_name;
        this.comments_string = comments_string;
    }
}
