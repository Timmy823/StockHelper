package com.example.demo.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class StockService {
    private StringRedisTemplate stringRedisTemplate;
    private String stockUrl;
    
    public StockService(String stockUrl, StringRedisTemplate stringRedisTemplate) throws IOException {
        this.stockUrl = stockUrl;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public JSONObject getInstitutionalInvestorsListedStockTradeInfo (String stock_id, String specific_date) {

        //get redis info        
        String redis_key = "institutional_investors_listed_stock_trade_info_" + specific_date + ":" + stock_id;
        String II_stock_trade_string = this.stringRedisTemplate.opsForValue().get(redis_key);

        int redis_ttl = 86400; // redis存活一天

        if (II_stock_trade_string != null) {
            return ResponseService.responseSuccess(JSONObject.fromObject(II_stock_trade_string));
        }

        ArrayList<String> url = new ArrayList<String>();
        //get QFII share numbers of transaction  
        url.add(this.stockUrl + "TWT38U" + "?response=json&date=" + specific_date);
        //get QDII share numbers of transaction 
        url.add(this.stockUrl + "TWT44U" + "?response=json&date=" + specific_date);
        //get dealer share numbers of transaction 
        url.add(this.stockUrl + "TWT43U" + "?response=json&date=" + specific_date);

        JSONObject II_info = new JSONObject();

        try{
            HttpsService open_url = new HttpsService();
            String alllines = open_url.getURLBufferString(url.get(0));
            JSONArray stock_array = JSONObject.fromObject(alllines).getJSONArray("data");
            JSONObject investor_trade_info = getForeignInvestorsListedStockTradeInfo(stock_array, stock_id);
            II_info.put("foreign_investors", investor_trade_info);

            //investment_trust
            open_url = new HttpsService();
            alllines = open_url.getURLBufferString(url.get(1));
            stock_array = JSONObject.fromObject(alllines).getJSONArray("data");
            investor_trade_info = getInvestmentTrustListedStockTradeInfo(stock_array, stock_id);
            II_info.put("investment_trust", investor_trade_info);

            //dealer
            open_url = new HttpsService();
            alllines = open_url.getURLBufferString(url.get(2));
            stock_array = JSONObject.fromObject(alllines).getJSONArray("data");
            investor_trade_info = getDealerListedStockTradeInfo(stock_array, stock_id);
            II_info.put("dealer", investor_trade_info);

            //put result into redis
            this.stringRedisTemplate.opsForValue().setIfAbsent(redis_key,
                II_info.toString(), redis_ttl, TimeUnit.SECONDS);

            return ResponseService.responseSuccess(II_info);
        } catch (IOException io) {
            return ResponseService.responseError("error", io.toString());
        } catch (Exception io) {
            return ResponseService.responseError("error", io.toString());
        }
    }

    private JSONObject getForeignInvestorsListedStockTradeInfo (JSONArray stock_array, String stock_id) throws Exception{
        JSONObject investor_trade_info = new JSONObject();
        JSONArray stock_item;
        String[] trade_info_string = {"buy", "sell", "sum"};

        for (int i=0; i<stock_array.size(); i++) {
            stock_item = stock_array.getJSONArray(i);
            //get param stock_id info
            if (!stock_id.equals(stock_item.get(1).toString().trim())) 
                continue;

            investor_trade_info.put(trade_info_string[0], stock_item.get(9));
            investor_trade_info.put(trade_info_string[1], stock_item.get(10));
            investor_trade_info.put(trade_info_string[2], stock_item.get(11));
            break;
        }
        if(investor_trade_info.size() == 0) {
            for(int i= 0; i< trade_info_string.length; i++) {
                investor_trade_info.put(trade_info_string[i], "0");
            }
        }
        return investor_trade_info;
    }

    private JSONObject getInvestmentTrustListedStockTradeInfo (JSONArray stock_array, String stock_id) throws Exception{
        JSONObject investor_trade_info = new JSONObject();
        JSONArray stock_item;
        String[] trade_info_string = {"buy", "sell", "sum"};

        for (int i=0; i<stock_array.size(); i++) {
            stock_item = stock_array.getJSONArray(i);

            //get param stock_id info
            if (!stock_id.equals(stock_item.get(1).toString().trim())) 
                continue;

            investor_trade_info.put(trade_info_string[0], stock_item.get(3));
            investor_trade_info.put(trade_info_string[1], stock_item.get(4));
            investor_trade_info.put(trade_info_string[2], stock_item.get(5));
            break;
        }
        if(investor_trade_info.size() == 0) {
            for(int i= 0; i< trade_info_string.length; i++) {
                investor_trade_info.put(trade_info_string[i], "0");
            }
        }
        return investor_trade_info;
    }

    private JSONObject getDealerListedStockTradeInfo (JSONArray stock_array, String stock_id) {
        JSONObject investor_trade_info = new JSONObject();
        JSONArray stock_item;
        String[] trade_info_string = {"buy", "sell", "sum"};

        for (int i=0; i<stock_array.size(); i++) {
            stock_item = stock_array.getJSONArray(i);

            //get param stock_id info
            if (!stock_id.equals(stock_item.get(0).toString().trim())) 
                continue;

            investor_trade_info.put(trade_info_string[0], stock_item.get(8));
            investor_trade_info.put(trade_info_string[1], stock_item.get(9));
            investor_trade_info.put(trade_info_string[2], stock_item.get(10));
            break;
        }
        if(investor_trade_info.size() == 0) {
            for(int i= 0; i< trade_info_string.length; i++) {
                investor_trade_info.put(trade_info_string[i], "0");
            }
        }
        return investor_trade_info;
    }
}
