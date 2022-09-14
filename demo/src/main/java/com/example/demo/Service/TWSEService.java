package com.example.demo.Service;
import java.io.*;

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
    
    public JSONObject  getStockRealtimeOTCTradeInfo() {
        try{

            //取得html內文 body資料
            HttpsService open_url = new HttpsService();
            InputStream URLstream = open_url.openURL(this.stockUrl);

            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "UTF-8"));

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
