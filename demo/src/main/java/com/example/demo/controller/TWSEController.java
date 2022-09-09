package com.example.demo.Controller;

import java.io.IOException;

import javax.validation.constraints.*;

import com.example.demo.Component.SpecificValidator;
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
    public JSONObject getCompanyList(TWSEService company,
            @RequestParam("type")
            @SpecificValidator(intValues = {1,2}, message = "type just can be 1 or 2. 1 is listed company. 2 is OTC company.")
            Integer type) {
        String twseUrl = "https://isin.twse.com.tw/isin/C_public.jsp?strMode=" + String.valueOf(type * 2);

        try {
            company = new TWSEService(twseUrl, stringRedisTemplate);
            return company.getCompanyList(type);
        } catch (IOException e) {
            e.printStackTrace();
            return company.responseError(e.toString());
        }
    }

    @GetMapping("/twse/getCompanyProfile")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject getCompanyInfoProfile(TWSEService company, 
            @RequestParam("stock_id")
            @NotEmpty(message = "it can not be empty.")
            String stock_id) {
        String stockUrl = "https://tw.stock.yahoo.com/quote/" + stock_id + "/profile";
        try {
            company = new TWSEService(stockUrl, stringRedisTemplate);
            return company.getCompanyInfoProfile();
        } catch (IOException e) {
            return company.responseError(e.toString());
        }
    }

    @GetMapping("/twse/getCompanyDividendPolicy")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject getCompanyDividendPolicy(TWSEService company, 
            @RequestParam("stock_id")
            @NotEmpty(message = "it can not be empty.")
            String stock_id){
        String stockUrl = "https://tw.stock.yahoo.com/quote/" + stock_id + "/dividend";
        try {
            company = new TWSEService(stockUrl, stringRedisTemplate);
            return company.getCompanyDividendPolicy();
        } catch (IOException io) {
            io.printStackTrace();
            return company.responseError(io.toString());
        }
    }

    @GetMapping("/twse/getStockTradeInfo")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject getStockTradeInfo(TWSEService stock, 
            @RequestParam("stock_id")
            @NotEmpty(message = "it can not be empty.")
            String stock_id,
            
            @RequestParam("type")
            @NotEmpty(message = "it can not be empty.")
            @SpecificValidator(strValues={"1","2","3"}, message="type必須為指定\"1\"或\"2\"或\"3\"")
            String type,

            @RequestParam("specific_date")
            @Pattern(regexp = "^(((?:19|20)[0-9]{2})(0?[1-9]|1[012])(0?[1-9]|[12][0-9]|3[01]))$" , message = "格式錯誤")
            String specific_date) {

        String stockUrl = "";
        if (type.equals("1"))
            stockUrl = "https://www.twse.com.tw/exchangeReport/STOCK_DAY?response=html&date=" + specific_date
                    + "&stockNo=" + stock_id;

        if (type.equals("2"))
            stockUrl = "https://www.twse.com.tw/exchangeReport/FMSRFK?response=html&date=" + specific_date + "&stockNo="
                    + stock_id;

        if (type.equals("3"))
            stockUrl = "https://www.twse.com.tw/exchangeReport/FMNPTK?response=html&stockNo=" + stock_id;

        try {
            stock = new TWSEService(stockUrl, stringRedisTemplate);
            return stock.getStockTradeInfo(type, Integer.parseInt(specific_date));
        } catch (IOException io) {
            io.printStackTrace();
            return stock.responseError(io.toString());
        }
    }
}
