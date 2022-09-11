package com.example.demo.Component.FavoriteListComponent;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class FavoriteListStockDeleteParam {
    @NotNull(message = "member_account不可為null")
    @NotEmpty(message = "member_account不可為空")
    @Email(message = "必須email格式")
    private String account;

    @NotNull(message = "favorite_list_name不可為null")
    @NotEmpty(message = "favorite_list_name不可為空")
    @Size(max = 50, message="favorite_list_name最長50")
    private String list_name;

    @NotNull(message = "stock_id不可為null")
    @NotEmpty(message = "stock_id不可為空")
    private String stock_id;

    public FavoriteListStockDeleteParam(String member_account, String favorite_list_name, String stock_id) {
        super();
        this.account = member_account;
        this.list_name = favorite_list_name;
        this.stock_id = stock_id;
    }
}
