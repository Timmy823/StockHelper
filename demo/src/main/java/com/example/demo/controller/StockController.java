package com.example.demo.Controller;

import java.io.IOException;

import javax.validation.constraints.*;

import com.example.demo.Service.ResponseService;
import com.example.demo.Service.StockService;

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
public class StockController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/stock/getIIListedStockTradeInfo")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject getInstitutionalInvestorsListedStockTradeInfo(StockService stock, 
            @RequestParam("stock_id")
            @NotEmpty(message = "it can not be empty.")
            String stock_id,

            @RequestParam("specific_date")
            @Pattern(regexp = "^(((?:19|20)[0-9]{2})(0?[1-9]|1[012])(0?[1-9]|[12][0-9]|3[01]))$" , message = "格式錯誤")
            String specific_date) {
            
        String stockUrl = "https://www.twse.com.tw/fund/";
        try {
            stock = new StockService(stockUrl, stringRedisTemplate);
            return stock.getInstitutionalInvestorsListedStockTradeInfo(stock_id, specific_date);
        } catch (IOException io) {
            return ResponseService.responseError("99999", io.toString());
        }
    }
}
