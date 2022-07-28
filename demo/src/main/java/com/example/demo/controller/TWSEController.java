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
        String type = input.getString("type");
        String stockUrl ="";

        if(type.equals("1"))
            stockUrl= "https://www.twse.com.tw/exchangeReport/MI_INDEX?response=html&date="+specified_date+"&type=IND";
        else if(type.equals("2")){
            specified_date -= 19110000;
            stockUrl="https://www.tpex.org.tw/web/stock/aftertrading/index_summary/summary_result.php?l=zh-tw&d="+
                String.valueOf(specified_date).substring(0,3)+
                "/"+String.valueOf(specified_date).substring(3,5)+
                "/"+String.valueOf(specified_date).substring(5,7)+"&s=0,asc,0&o=htm";
        }
  
        try{
            twse= new TWSEService(stockUrl);
            return twse.getStockMarketIndex(type);
        }catch(IOException io){
            io.printStackTrace();
            return twse.responseError(io.toString());
        }
    }
}
