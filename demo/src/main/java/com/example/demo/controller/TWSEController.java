package com.example.demo.controller;

import com.example.demo.Service.TWSEService;

import net.sf.json.JSONObject;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TWSEController {
    //每五秒揭示最近一次撮合結果
    @GetMapping("/twse/getStockRealtimeInfo")
    public JSONObject getStockRealtimeTradeInfo(@RequestBody JSONObject input, TWSEService twse){
        String id =input.getString("id");
        String stockUrl= "https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_"+id+".tw";
        try{
            twse= new TWSEService(stockUrl);
            return twse.getStockRealtimeTradeInfo();
        }catch(IOException io){
            io.printStackTrace();
            return twse.responseError(io.toString());
        }
    }
}
