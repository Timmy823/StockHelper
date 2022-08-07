package com.example.demo.controller;

import com.example.demo.Service.TWSEService;

import net.sf.json.JSONObject;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TWSEController {
    //company monthly revenue for recent five years
    @GetMapping("/twse/getCompanyMonthlyRevenue")
    public JSONObject getCompanyMonthlyRevenue(@RequestBody JSONObject input, TWSEService twse){
        String id =input.getString("id");
        String stockUrl= "https://tw.stock.yahoo.com/quote/"+id+"/revenue";
        
        try{
            twse= new TWSEService(stockUrl);
            return twse.getCompanyMonthlyRevenue();
        }catch(IOException io){
            io.printStackTrace();
            return twse.responseError(io.toString());
        }
    }
}
