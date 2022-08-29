package com.example.demo.Component;

 import javax.validation.constraints.Email;
 import javax.validation.constraints.NotEmpty;

 import lombok.Data;

 @Data
 public class GetFavoriteListParam {
     @NotEmpty(message = "member_account不可為空")
     @Email(message = "必須email格式")
     private String account;

     private String list_name;

     public GetFavoriteListParam(String member_account, String favorite_list_name) {
         super();
         this.account = member_account;
         this.list_name = favorite_list_name;
     }
 }
