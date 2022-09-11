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

    public JSONObject getInstitutionalInvestorsOTCStockTradeInfo (String stock_id, String specific_date) {

        //get redis info        
        String redis_key = "institutional_investors_OTC_stock_trade_info_" + specific_date + ":" + stock_id;
        String II_stock_trade_string = this.stringRedisTemplate.opsForValue().get(redis_key);

        if (II_stock_trade_string != null) {
            return ResponseService.responseJSONArraySuccess(JSONArray.fromObject(II_stock_trade_string));
        }

        String[] url_II = {"qfii_trading/forgtr_result", "sitc_trading/sitctr_result", "dealer_trading/dealtr_hedge_result"};
        String[] url_type = {"&type=buy", "&type=sell"};

        //yyyyMMdd -> yyy/MM/dd
        String url_date = Integer.parseInt(specific_date.substring(0,4))-1911 +
                 "/"+specific_date.substring(4,6)+
                 "/"+specific_date.substring(6);

        //url = { QFII buy, QFII sell, QDII buy, QDII sell, dealer buy, dealer sell }
        ArrayList<String> url = new ArrayList<String>();
        for(int i=0; i<url_II.length; i++) {
            for(int j=0; j<url_type.length; j++) {
                url.add(this.stockUrl + url_II[i] + ".php?l=zh-tw&t=D&d=" + url_date + url_type[j]);
            }
        }
        return getShareNumbersOfIIOTCStockTransaction(0, url, new JSONObject(), stock_id, redis_key);
    }

    private JSONObject getShareNumbersOfIIOTCStockTransaction (int url_index, ArrayList<String> url, JSONObject II_info, String stock_id, String redis_key) {
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
                
            
            if (url_index >= url.size()) {
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

            JSONArray stock_array = JSONObject.fromObject(alllines).getJSONArray("aaData");
            JSONArray stock_item;

            for (int i=0; i<stock_array.size(); i++) {
                stock_item = stock_array.getJSONArray(i);

                //get param stock_id info
                if (!stock_item.get(1).toString().trim().equals(stock_id)) 
                    continue;

                //QFII
                if(url_index/2 == 0) {
                    investor_trade_info.put(trade_info_string[0], stock_item.get(9));
                    investor_trade_info.put(trade_info_string[1], stock_item.get(10));
                    investor_trade_info.put(trade_info_string[2], stock_item.get(11));
                    break;
                }
                //QDII
                if(url_index/2 == 1) {
                    investor_trade_info.put(trade_info_string[0], stock_item.get(3));
                    investor_trade_info.put(trade_info_string[1], stock_item.get(4));
                    investor_trade_info.put(trade_info_string[2], stock_item.get(5));
                    break;
                }
                //dealer
                if(url_index/2 == 2) {
                    investor_trade_info.put(trade_info_string[0], stock_item.get(7));
                    investor_trade_info.put(trade_info_string[1], stock_item.get(8));
                    investor_trade_info.put(trade_info_string[2], stock_item.get(9));
                    break;
                }
            }

            if(investor_trade_info.size() == 0) {
                //if buy and sell are empty. add "" into  investor trade info 
                if(url_index%2 == 1) {
                    for(int i= 0; i< institutional_investors_string.length; i++) {
                        investor_trade_info.put(institutional_investors_string[i], "0");
                        II_info.put(institutional_investors_string[url_index/2], investor_trade_info);
                    }
                }
                return getShareNumbersOfIIOTCStockTransaction(url_index + 1, url, II_info, stock_id, redis_key);
            }

            II_info.put(institutional_investors_string[url_index/2], investor_trade_info);
            return getShareNumbersOfIIOTCStockTransaction((url_index%2 == 1)? url_index+1 : url_index+2, url, II_info, stock_id, redis_key);
        } catch (IOException io) {
            return ResponseService.responseError("error", io.toString());
        }
    }
}
