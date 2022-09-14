package com.example.demo.Controller;
import java.io.IOException;

import javax.validation.constraints.NotEmpty;

import com.example.demo.Service.ResponseService;
import com.example.demo.Service.TWSEService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.sf.json.JSONObject;

@RestController
public class TWSEController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/twse/getCompanyYearlyRevenue")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject getCompanyYearlyRevenue(TWSEService twse,
        @RequestParam("stock_id")
        @NotEmpty(message = "it can not be empty.")
        String stock_id) {
        String stockUrl= "https://tw.stock.yahoo.com/_td-stock/api/resource/StockServices.revenues;period=year;symbol=" + stock_id;
        try{
            twse= new TWSEService(stockUrl, stringRedisTemplate);
            return twse.getCompanyYearlyRevenue(stock_id);
        }catch(IOException io){
            io.printStackTrace();
            return ResponseService.responseError("99999", io.toString());
        }
    }
}
