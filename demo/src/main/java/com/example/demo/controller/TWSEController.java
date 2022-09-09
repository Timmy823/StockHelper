package com.example.demo.Controller;

import java.io.IOException;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.example.demo.Component.SpecificValidator;
import com.example.demo.Component.StockTradeInfoParam;
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

    @GetMapping("/twse/getAllCompanyList")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject getCompanyList(TWSEService twse,
            @RequestParam("type")
            @SpecificValidator(intValues = {1,2}, message = "type just can be 1 or 2. 1 is listed company. 2 is OTC company.")
            Integer type) {
        String twseUrl = "https://isin.twse.com.tw/isin/C_public.jsp?strMode=" + String.valueOf(type * 2);

        try {
            twse = new TWSEService(twseUrl, stringRedisTemplate);
            return twse.getCompanyList(type);
        } catch (IOException e) {
            e.printStackTrace();
            return twse.responseError(e.toString());
        }
    }

    @GetMapping("/twse/getCompanyProfile")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject getCompanyInfoProfile(@RequestParam JSONObject input, TWSEService stock) {
        Integer stockid = input.getInt("id");
        String stockUrl = "https://tw.stock.yahoo.com/quote/" + stockid + "/profile";
        try {
            stock = new TWSEService(stockUrl, stringRedisTemplate);
            return stock.getCompanyInfoProfile();
        } catch (IOException e) {
            return stock.responseError(e.toString());
        }
    }

    @GetMapping("/twse/getCompanyDividendPolicy")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject getCompanyDividendPolicy(@RequestParam JSONObject input, TWSEService twse) {
        String id = input.getString("id");
        String stockUrl = "https://tw.stock.yahoo.com/quote/" + id + "/dividend";

        try {
            twse = new TWSEService(stockUrl, stringRedisTemplate);
            return twse.getCompanyDividendPolicy();
        } catch (IOException io) {
            io.printStackTrace();
            return twse.responseError(io.toString());
        }
    }

    @GetMapping("/twse/getStockTradeInfo")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject getStockTradeInfo(@Valid @RequestParam StockTradeInfoParam input, TWSEService twse) {
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
