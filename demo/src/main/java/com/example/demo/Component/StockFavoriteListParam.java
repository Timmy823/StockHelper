package com.example.demo.Component;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class StockFavoriteListParam {
    @NotEmpty(message = "member_account不可為空")
    @Email(message = "必須email格式")
    private String member_account;

    private String favorite_list_name;

    private String stock_id;

    private String stock_name;

    private String comments_string;

    public StockFavoriteListParam(String account, String list_name, String stock_id, String stock_name, String comments_string) {
        super();
        this.member_account = account;
        this.favorite_list_name = list_name;
        this.stock_id = stock_id;
        this.stock_name = stock_name;
        this.comments_string = comments_string;
    }
}
