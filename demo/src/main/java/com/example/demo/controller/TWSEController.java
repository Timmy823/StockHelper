package com.example.demo.controller;

import com.example.demo.Service.TWSEService;
import net.sf.json.JSONObject;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TWSEController {
    @GetMapping("/twse/getStockMarketIndex")
    public JSONObject getStockMarketIndex(@RequestBody JSONObject input, TWSEService twse){
        
        Integer specified_date =input.getInt("specified_date");
        String stockUrl = "https://www.twse.com.tw/exchangeReport/MI_INDEX?response=html&date="+specified_date+"&type=IND";

        try{
            twse= new TWSEService(stockUrl);
            return twse.getStockMarketIndex();
        }catch(IOException io){
            io.printStackTrace();
            return twse.responseError(io.toString());
        }
    }
}
