package com.example.demo.Controller;

import com.example.demo.Service.TWSEService;
import com.example.demo.Component.StockTradeInfoParam;

import net.sf.json.JSONObject;

import java.io.IOException;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

@RestController
public class TWSEController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/twse/getAllCompanyList")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject getCompanyList(@RequestBody JSONObject input, TWSEService twse) {
        int list_level = input.getInt("type");
        String twseUrl = "https://isin.twse.com.tw/isin/C_public.jsp?strMode=" + String.valueOf(list_level * 2);

        try {
            twse = new TWSEService(twseUrl, stringRedisTemplate);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return twse.getCompanyList(list_level);
    }

    @GetMapping("/twse/getCompanyDividendPolicy")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject getCompanyDividendPolicy(@RequestBody JSONObject input, TWSEService twse) {
        String id = input.getString("id");
        String stockUrl = "https://tw.stock.yahoo.com/quote/" + id + "/dividend";

        try {
            twse = new TWSEService(stockUrl, stringRedisTemplate);
            return twse.getCompanyDividendPolicy(id);
        } catch (IOException io) {
            io.printStackTrace();
            return twse.responseError(io.toString());
        }
    }

    @GetMapping("/twse/getStockTradeInfo")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject getStockTradeInfo(@Valid @RequestBody StockTradeInfoParam input, TWSEService twse) {
        Integer specific_date = input.get_date();
        String input_type = input.get_type();
        String input_id = input.get_stockID();
        String stockUrl = "";

        if (input_type.equals("1"))
            stockUrl = "https://www.twse.com.tw/exchangeReport/STOCK_DAY?response=html&date=" + specific_date
                    + "&stockNo=" + input_id;

        if (input_type.equals("2"))
            stockUrl = "https://www.twse.com.tw/exchangeReport/FMSRFK?response=html&date=" + specific_date + "&stockNo="
                    + input_id;

        if (input_type.equals("3"))
            stockUrl = "https://www.twse.com.tw/exchangeReport/FMNPTK?response=html&stockNo=" + input_id;

        try {
            twse = new TWSEService(stockUrl, stringRedisTemplate);
            return twse.getStockTradeInfo(input_type, specific_date);
        } catch (IOException io) {
            io.printStackTrace();
            return twse.responseError(io.toString());
        }
    }
}
