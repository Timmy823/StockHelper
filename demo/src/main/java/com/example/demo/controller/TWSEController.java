package com.example.demo.Controller;

import java.io.IOException;

import javax.validation.constraints.*;

import com.example.demo.Component.SpecificValidator;
import com.example.demo.Service.ResponseService;
import com.example.demo.Service.TWSEService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
            return twse.getCompanyDividendPolicy();
        } catch (IOException io) {
            io.printStackTrace();
            return twse.responseError(io.toString());
        }
    }

    @GetMapping("/twse/getListedStockTradeInfo")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject getListedStockTradeInfo(TWSEService stock,
            @RequestParam("stock_id") @NotEmpty(message = "it can not be empty.") 
            String stock_id,
            @RequestParam("type") @NotEmpty(message = "it can not be empty.") @SpecificValidator(strValues = { "1", "2",
                    "3" }, message = "type必須為指定\"1\"或\"2\"或\"3\"") 
            String type,
            @RequestParam("specific_date") @Pattern(regexp = "^(((?:19|20)[0-9]{2})(0?[1-9]|1[012])(0?[1-9]|[12][0-9]|3[01]))$", message = "格式錯誤") 
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
            return stock.getListedStockTradeInfo(type, Integer.parseInt(specific_date), stock_id);
        } catch (IOException io) {
            io.printStackTrace();
            return ResponseService.responseError("error", io.toString());
        }
    }
}
