package com.example.demo.Component;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.example.demo.Controller.TWSEController;

public class CompanyDividendPolicyParam extends TWSEController{
    private  String id;
    
    @NotEmpty(message = "id不可為空")
    @Size(min=4,max =4, message = "id格式錯誤")
    public String get_id(){
        return id;
    }

    public CompanyDividendPolicyParam(String id){
        super();
        System.out.print(id);
        this.id=id;
    }
}
