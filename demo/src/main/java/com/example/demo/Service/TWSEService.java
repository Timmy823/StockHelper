package com.example.demo.Service;
import java.io.*;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jsoup.nodes.Document;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TWSEService {
    String stockUrl;

    public TWSEService(String stockUrl) throws IOException{
        this.stockUrl = stockUrl;
    }
    
    private InputStream openURL(String urlPath) throws IOException{
        URL url = new URL(urlPath);

        // open a url connection.
        HttpsURLConnection url_connection =  (HttpsURLConnection) url.openConnection();
        url_connection.setDoInput(true);
        url_connection.setDoOutput(true);

        //set request method
        url_connection.setRequestMethod("GET");
        url_connection.setConnectTimeout(15000);
        url_connection.setReadTimeout(15000);
        //set request header 
        url_connection.setRequestProperty("User-Agent", " Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
        url_connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        url_connection.setRequestProperty("Content-Length", Integer.toString(1000));
        url_connection.setRequestProperty("connection", "Keep-Alive");
        
        System.out.println("ready to connect!");
        
        url_connection.connect();
        
        //the method is used to access the header filed after the connection 
        if(url_connection.getResponseCode() != 200){
            System.out.print("\nConnection Fail:"+url_connection.getResponseCode());
        }
        return url_connection.getInputStream();            
    }
    
    public JSONObject  getStockRealtimeOTCTradeInfo() {
        try{
            InputStream URLStream = openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLStream,"UTF-8"));

            String line=null;
            String all_lines="";
            while((line=buffer.readLine())!= null){
                all_lines+=line;
            }

            return StockRealtimeOTCTradeInfoDataParsing(all_lines);  
        }catch(IOException io){
            return responseError(io.toString());
        }
    }

    public JSONObject StockRealtimeOTCTradeInfoDataParsing(String all_lines){
        String [] web_info_key ={"v","o","h","l","u","w","t","z","tv"};
        String[] request_key = {"accumulation_volume","openning_price","highest_price","lowest_price","limit_up","limit_down"
        ,"trade_time","price","trade_volume"};

        try{
            //取得html內文 body資料
            Document doc =  Jsoup.parse(new String(all_lines.getBytes("UTF-8"), "UTF-8"));
            String body = doc.select("body").text();
            
            //jackson解析json/array node
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(body);
            //個股即時詳細資訊放在 "msgArray":[{}]
            JsonNode realtime_info= node.get("msgArray").get(0);

            if(realtime_info == null)
                return responseError("網頁解析錯誤");

            //取得指定資料並放入json object
            JSONArray allstockArray= new JSONArray();
            JSONObject object = new JSONObject();
            for(int i=0; i<web_info_key.length; i++){
                object.element(request_key[i], realtime_info.get(web_info_key[i]).toString().replaceAll("\"",""));
            }
            allstockArray.add(object);
            
            return responseStockRealtimeOTCTradeInfoSuccess(allstockArray);
        }catch(IOException io){
            return responseError(io.toString());
        }
    }

    public JSONObject responseStockRealtimeOTCTradeInfoSuccess(JSONArray allstockArray){
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();

        data.put("stockdata",allstockArray);
        
        status_code.put("status", "success");
        status_code.put("desc", "");

        result.put("metadata", status_code);
        result.put("data", data);
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
