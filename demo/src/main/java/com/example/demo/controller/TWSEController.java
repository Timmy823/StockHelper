package com.example.demo.Controller;
import java.io.IOException;

import javax.validation.constraints.*;

import com.example.demo.Service.TWSEService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.sf.json.JSONObject;

@RestController
@Validated
public class TWSEController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/twse/getCompanyMonthlyRevenue")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject getCompanyMonthlyRevenue(TWSEService twse,
    @RequestParam("stock_id")
    @NotEmpty(message = "it can not be empty.")
    String stock_id) {
        String stockUrl= "https://tw.stock.yahoo.com/_td-stock/api/resource/StockServices.revenues;period=month;symbol=" + stock_id;
        
        try{
            twse = new TWSEService(stockUrl, stringRedisTemplate);
            return twse.getCompanyMonthlyRevenue(stock_id);
        }catch(IOException io){
            io.printStackTrace();
            return twse.responseError(io.toString());
        }
    }
}
