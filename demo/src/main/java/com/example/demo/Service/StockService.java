package com.example.demo.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

        if (II_stock_trade_string != null) {
            return ResponseService.responseJSONArraySuccess(JSONArray.fromObject(II_stock_trade_string));
        }

        ArrayList<String> url = new ArrayList<String>();
        //get QFII share numbers of transaction  
        url.add(this.stockUrl + "TWT38U" + "?response=json&date=" + specific_date);
        //get QDII share numbers of transaction 
        url.add(this.stockUrl + "TWT44U" + "?response=json&date=" + specific_date);
        //get dealer share numbers of transaction 
        url.add(this.stockUrl + "TWT43U" + "?response=json&date=" + specific_date);

        return getShareNumbersOfIIListedStockTransaction(0, url, new JSONObject(), stock_id, redis_key);
    }

    private JSONObject getShareNumbersOfIIListedStockTransaction (int url_index, ArrayList<String> url, JSONObject II_info, String stock_id, String redis_key) {
        try {
            int redis_ttl = 86400; // redis存活一天

            //put II_info object into investor trade array
            JSONArray investor_trade_array = new JSONArray();
            //put url info into the investor trade info
            JSONObject investor_trade_info = new JSONObject();
            //put different institutional investor into II info object 
            String[] institutional_investors_string = {"foreign_investors", "investment_trust", "dealer"};
            //put share numbers of transaction into investor trade info object
            String[] trade_info_string = {"buy", "sell", "sum"};
            //get stock id position of url stock info
            Integer[] stock_id_index = {1,1,0};
            
            if (url_index == url.size()) {
                investor_trade_array.add(II_info); 

                //put result into redis
                this.stringRedisTemplate.opsForValue().setIfAbsent(redis_key,
                        investor_trade_array.toString(), redis_ttl, TimeUnit.SECONDS);
                
                return ResponseService.responseJSONArraySuccess(investor_trade_array);
            }
            //https connection 
            HttpsService open_url = new HttpsService();
            InputStream URLstream = open_url.openURL(url.get(url_index));
            
            //get url info
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "UTF-8"));
            String line = null;
            String alllines = "";
            while ((line = buffer.readLine()) != null) {
                alllines += line;
            }

            JSONArray stock_array = JSONObject.fromObject(alllines).getJSONArray("data");
            JSONArray stock_item;

            for (int i=0; i<stock_array.size(); i++) {
                stock_item = stock_array.getJSONArray(i);
                //get param stock_id info
                if (!stock_item.get(stock_id_index[url_index]).toString().trim().equals(stock_id)) 
                    continue;

                //QFII
                if(url_index == 0) {
                    investor_trade_info.put(trade_info_string[0], stock_item.get(9));
                    investor_trade_info.put(trade_info_string[1], stock_item.get(10));
                    investor_trade_info.put(trade_info_string[2], stock_item.get(11));
                    break;
                }
                //QDII
                if(url_index == 1) {
                    investor_trade_info.put(trade_info_string[0], stock_item.get(3));
                    investor_trade_info.put(trade_info_string[1], stock_item.get(4));
                    investor_trade_info.put(trade_info_string[2], stock_item.get(5));
                    break;
                }
                //dealer
                if(url_index == 2) {
                    investor_trade_info.put(trade_info_string[0], stock_item.get(8));
                    investor_trade_info.put(trade_info_string[1], stock_item.get(9));
                    investor_trade_info.put(trade_info_string[2], stock_item.get(10));
                    break;
                }
            }

            II_info.put(institutional_investors_string[url_index], investor_trade_info);
            return getShareNumbersOfIIListedStockTransaction(++url_index, url, II_info, stock_id, redis_key);
        } catch (IOException io) {
            return ResponseService.responseError("error", io.toString());
        }
    }
}
