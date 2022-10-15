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

    public JSONObject getInstitutionalInvestorsOTCStockTradeInfo (String stock_id, String specific_date) {
        int redis_ttl = 86400; // redis存活一天

        //get redis info        
        String redis_key = "institutional_investors_OTC_stock_trade_info_" + specific_date + ":" + stock_id;
        String II_stock_trade_string = this.stringRedisTemplate.opsForValue().get(redis_key);

        if (II_stock_trade_string != null) {
            return ResponseService.responseSuccess(JSONObject.fromObject(II_stock_trade_string));
        }
        //set Investors buy and sell URL
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

        JSONObject II_info = new JSONObject();
        HttpsService open_url;
        String alllines = "";
        JSONArray stock_array;   
        try{
            //foreign_investors : buy/sell
            JSONObject investor_trade_info = new JSONObject();
            for (int i=0 ; i<=1 ; i++) {
                open_url = new HttpsService();
                alllines = open_url.getURLBufferString(url.get(i));
                stock_array = JSONObject.fromObject(alllines).getJSONArray("aaData");
                investor_trade_info = getForeignInvestorsOTCStockTradeInfo(stock_array, investor_trade_info, stock_id);
                
                if (investor_trade_info.size() != 0) 
                    break;
            }
            if (investor_trade_info.size() == 0) {
                investor_trade_info.put("buy", "0");
                investor_trade_info.put("sell", "0");
                investor_trade_info.put("sum", "0");
            }
            II_info.put("foreign_investors", investor_trade_info);

            // investment_trust : buy/sell
            investor_trade_info = new JSONObject();
            for (int i=2 ; i<=3 ; i++) {
                open_url = new HttpsService();
                alllines = open_url.getURLBufferString(url.get(i));
                stock_array = JSONObject.fromObject(alllines).getJSONArray("aaData");
                investor_trade_info = getInvestmentTrustOTCStockTradeInfo(stock_array, investor_trade_info, stock_id);
                
                if (investor_trade_info.size() != 0) 
                    break;
            }
            if (investor_trade_info.size() == 0) {
                investor_trade_info.put("buy", "0");
                investor_trade_info.put("sell", "0");
                investor_trade_info.put("sum", "0");
            }
            II_info.put("investment_trust", investor_trade_info);

            //dealer : buy/sell
            investor_trade_info = new JSONObject();
            for (int i=4 ; i<=5 ; i++) {
                open_url = new HttpsService();
                alllines = open_url.getURLBufferString(url.get(i));
                stock_array = JSONObject.fromObject(alllines).getJSONArray("aaData");
                investor_trade_info = getDealerOTCStockTradeInfo(stock_array, investor_trade_info, stock_id);
                
                if (investor_trade_info.size() != 0) 
                    break;
            }
            if (investor_trade_info.size() == 0) {
                investor_trade_info.put("buy", "0");
                investor_trade_info.put("sell", "0");
                investor_trade_info.put("sum", "0");
            }
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

    private JSONObject getForeignInvestorsOTCStockTradeInfo (JSONArray stock_array, JSONObject investor_trade_info,  String stock_id) throws Exception {
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
        return investor_trade_info;
    }

    private JSONObject getInvestmentTrustOTCStockTradeInfo (JSONArray stock_array, JSONObject investor_trade_info,  String stock_id) throws Exception {
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
        return investor_trade_info;
    }

    private JSONObject getDealerOTCStockTradeInfo (JSONArray stock_array, JSONObject investor_trade_info,  String stock_id) throws Exception {
        JSONArray stock_item;
        String[] trade_info_string = {"buy", "sell", "sum"};

        for (int i=0; i<stock_array.size(); i++) {
            stock_item = stock_array.getJSONArray(i);
            //get param stock_id info
            if (!stock_id.equals(stock_item.get(1).toString().trim())) 
                continue;

                investor_trade_info.put(trade_info_string[0], stock_item.get(7));
                investor_trade_info.put(trade_info_string[1], stock_item.get(8));
                investor_trade_info.put(trade_info_string[2], stock_item.get(9));
                break;
        }
        return investor_trade_info;
    }
}
