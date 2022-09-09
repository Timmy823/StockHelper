package com.example.demo.Service;
import java.io.*;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.example.demo.Component.StockComponent.StockIdParam;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class TWSEService {
    private StringRedisTemplate stringRedisTemplate;
    private String stockUrl;

    public TWSEService(String stockUrl, StringRedisTemplate stringRedisTemplate) throws IOException {
        this.stockUrl = stockUrl;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private InputStream openURL(String urlPath) throws IOException {
        URL url = new URL(urlPath);
        createTrustManager(url);

        // open a url connection.
        HttpsURLConnection url_connection = (HttpsURLConnection) url.openConnection();
        url_connection.setDoInput(true);
        url_connection.setDoOutput(true);

        // set request method
        url_connection.setRequestMethod("GET");
        url_connection.setConnectTimeout(15000);
        url_connection.setReadTimeout(15000);
        // set request header
        url_connection.setRequestProperty("User-Agent",
                " Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
        url_connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        url_connection.setRequestProperty("Content-Length", Integer.toString(1000));
        url_connection.setRequestProperty("connection", "Keep-Alive");

        System.out.println("ready to connect!");

        url_connection.connect();

        // the method is used to access the header filed after the connection
        if (url_connection.getResponseCode() != 200) {
            System.out.print("\nConnection Fail:" + url_connection.getResponseCode());
        }
        return url_connection.getInputStream();
    }
    
    public JSONObject getCompanyYearlyRevenue(StockIdParam data) {
        try{
            //check redis
            String yearly_revenue_redis_key = "yearly_revenue : " + data.getStock_id();
            int redis_ttl = 86400 * 3; // redis存活3天

            String yearly_revenue_string = this.stringRedisTemplate.opsForValue().get(yearly_revenue_redis_key);
            if (yearly_revenue_string != null) {
                return responseSuccess(JSONArray.fromObject(yearly_revenue_string));
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

            InputStream URLstream = openURL(this.stockUrl);
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

            return responseSuccess(revenue_array);
        }catch(Exception io){
            return responseError(io.toString());
        }
    }

    private TrustManager createTrustManager(URL urlObj) {
        System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
        System.setProperty("javax.net.ssl.trustStore", "keystore");
        TrustManager trust = new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {
            }
        };

        SSLContext sslcontext;
        try {
            sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[] { trust }, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trust;
    }

    private JSONObject responseSuccess(JSONArray json_array_data) {
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();

        status_code.put("status", "success");
        status_code.put("desc", "");

        result.put("metadata", status_code);
        result.put("data", json_array_data);

        return result;
    }

    public JSONObject responseError(String error_msg) {
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();
    
        data.put("data","");
        
        status_code.put("status", "error");
        status_code.put("desc", error_msg);
    
        result.put("metadata", status_code);
        result.put("data", data);
        return result;
    }
}
