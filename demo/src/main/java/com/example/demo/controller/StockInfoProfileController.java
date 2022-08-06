package com.example.demo.controller;

import net.sf.json.JSONObject;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Service.StockInfoProfileService;
    

@RestController
public class StockInfoProfileController {

    @GetMapping("/twse/getCompanyProfile")
    public JSONObject getStockTradeInfo(@RequestBody JSONObject input,StockInfoProfileService stock){
        String stockUrl="";
    
        Integer stockid =input.getInt("id");
        stockUrl="https://tw.stock.yahoo.com/quote/"+stockid+"/profile";
        try{
            stock= new StockInfoProfileService(stockUrl);  
        }catch(IOException e){
            e.printStackTrace();
        }   
        return stock.getCompanyProfile();  
    }
}
