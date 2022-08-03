package com.example.demo.controller;

import com.example.demo.Service.TWSEService;

import net.sf.json.JSONObject;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TWSEController {
    @GetMapping("/twse/getCompanyDividendPolicy")
    public JSONObject getStockMarketIndex(@RequestBody JSONObject input, TWSEService twse){
        String id =input.getString("id");
        String stockUrl= "https://stockinfo.tw/dividends/?stockSearch="+id;

        try{
            twse= new TWSEService(stockUrl);
            return twse.getCompanyDividendPolicy();
        }catch(IOException io){
            io.printStackTrace();
            return twse.responseError(io.toString());
        }
    }
}