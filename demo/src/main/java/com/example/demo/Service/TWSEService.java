package com.example.demo.Service;
import java.io.*;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class TWSEService {
    private StringRedisTemplate stringRedisTemplate;
    private String stockUrl;

    public TWSEService(String stockUrl, StringRedisTemplate stringRedisTemplate) throws IOException {
        this.stockUrl = stockUrl;
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    public JSONObject getCompanyYearlyRevenue(String stock_id) {
        try{
            //check redis
            String yearly_revenue_redis_key = "yearly_revenue : " + stock_id;
            int redis_ttl = 86400 * 3; // redis存活3天

            String yearly_revenue_string = this.stringRedisTemplate.opsForValue().get(yearly_revenue_redis_key);
            if (yearly_revenue_string != null) {
                return ResponseService.responseJSONArraySuccess(JSONArray.fromObject(yearly_revenue_string));
            }

            //get every revenue data frome URL's revenues array.
            JSONObject yearly_revenue_item = new JSONObject();
            //it is response array.
            JSONArray revenue_array = new JSONArray();
            //response info put into revenue_array
            JSONObject revenue_info = new JSONObject();  
            //revene data belong date "2022-08-01T00:00:00+08:00" 
            String[] belong_date; 
            //revene_info key string 
            String[] revenue_string = {"year", "month", "revenue", "cumulative_revenue",
                    "MoM", "YoY", "cumulative_YoY" ,
                    "revenue_in_same_yearly_last_year", "cumulative_revenue_last_year"};

            HttpsService open_url = new HttpsService();
            InputStream URLstream = open_url.openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "UTF-8"));
            String line = null;
            String alllines = "";
            while ((line = buffer.readLine()) != null) {
                alllines += line;
            }
            JSONArray revenues = JSONObject.fromObject(alllines)
                    .getJSONObject("data").getJSONObject("result").getJSONArray("revenues");

            for(int i = 0; i< revenues.size(); i++) {
                yearly_revenue_item = revenues.getJSONObject(i);
                revenue_info = new JSONObject();   
                //get revenue belong year and month
                belong_date = yearly_revenue_item.getString("date").split("-");

                revenue_info.put(revenue_string[0], belong_date[0]);     
                revenue_info.put(revenue_string[1], "");     
                revenue_info.put(revenue_string[2], yearly_revenue_item.getString("revenue"));    
                revenue_info.put(revenue_string[3], "");  
                revenue_info.put(revenue_string[4], "");     
                revenue_info.put(revenue_string[5], yearly_revenue_item.getString("revenueYoY"));     
                revenue_info.put(revenue_string[6], "");     
                revenue_info.put(revenue_string[7], yearly_revenue_item.getJSONObject("lastYear").getString("revenue"));     
                revenue_info.put(revenue_string[8], ""); 
                revenue_array.add(revenue_info);
            }
            
            this.stringRedisTemplate.opsForValue().setIfAbsent(yearly_revenue_redis_key,
                    revenue_array.toString(), redis_ttl, TimeUnit.SECONDS);

            return ResponseService.responseJSONArraySuccess(revenue_array);
        }catch(Exception io){
            return ResponseService.responseError("error", io.toString());
        }
    }
}
