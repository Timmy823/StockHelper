package com.example.demo.Component.FavoriteListComponent;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class FavoriteListStockCommentParam {
    @NotEmpty(message = "member_account不可為空")
    @Email(message = "必須email格式")
    private String account;

    @NotEmpty(message = "stock_id不可為空")
    private String stock_id;

    @NotNull(message = "comment不可為null")
    private String stock_comment;

    public FavoriteListStockCommentParam(String member_account, String stock_id, String comment) {
        super();
        this.account = member_account;
        this.stock_id = stock_id;
        this.stock_comment = comment;
    }
}
