package com.example.demo.controller;
import java.io.IOException;

import javax.validation.Valid;

import com.example.demo.Component.StockComponent.StockIdParam;
import com.example.demo.Service.TWSEService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import net.sf.json.JSONObject;

@RestController
public class TWSEController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/twse/getCompanyYearlyRevenue")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject getCompanyYearlyRevenue(@Valid @RequestBody StockIdParam input, TWSEService twse){
        String stockUrl= "https://tw.stock.yahoo.com/_td-stock/api/resource/StockServices.revenues;period=year;symbol="+ input.getStock_id();
        try{
            twse= new TWSEService(stockUrl, stringRedisTemplate);
            return twse.getCompanyYearlyRevenue(input);
        }catch(IOException io){
            io.printStackTrace();
            return twse.responseError(io.toString());
        }
    }
}
