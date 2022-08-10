package com.example.demo.Controller;

import net.sf.json.JSONObject;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Service.CompanyInfoProfileService;
    
@RestController
public class CompanyInfoProfileController {

    @GetMapping("/twse/getCompanyProfile")
    public JSONObject getCompanyTradeInfo(@RequestBody JSONObject input,CompanyInfoProfileService stock){
        String stockUrl="";
    
        Integer stockid =input.getInt("id");
        stockUrl="https://tw.stock.yahoo.com/quote/"+stockid+"/profile";
        try{
            stock= new CompanyInfoProfileService(stockUrl);  
        }catch(IOException e){
            e.printStackTrace();
        }   
        return stock.getCompanyProfile();  
    }
}
