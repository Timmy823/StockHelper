package com.example.demo.Service;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;


import org.jsoup.nodes.Document;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TWSEService {
    String stockUrl;
    String[] stock_index_items = {"name","index","sign","change","percent"};

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
    
    public JSONObject  getStockMarketIndex() {
        try{
            InputStream URLStream = openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLStream,"UTF-8"));
            
            HashMap<String, ArrayList<String>> stock_map = new HashMap<String, ArrayList<String>>();
            for(int i=0; i<stock_index_items.length; i++){
                stock_map.put(stock_index_items[i], new ArrayList<String>());
            }

            String line=null;
            String all_lines="";
            while((line=buffer.readLine())!= null){
                all_lines+=line;
            }
            return StockMaketIndexDataParsing(stock_map, all_lines);    
        }catch(IOException io){
            return responseError(io.toString());
        }
    }

    public JSONObject StockMaketIndexDataParsing(HashMap<String, ArrayList<String>> stock_map, String all_lines){
        try{
            Document doc =  Jsoup.parse(new String(all_lines.getBytes("UTF-8"), "UTF-8"));
            Elements tables = doc.select("table");

            //html tables format ={價格指數(臺灣證券交易所),價格指數(跨市場),價格指數(臺灣指數公司),報酬指數(臺灣證券交易所),報酬指數(跨市場),報酬指數(臺灣指數公司)}
            for(int i=0; i<tables.size(); i++){
                Elements trs = tables.get(i).select("tr");

                if(trs.get(0).text().contains("報酬"))
                    break;
                
                //html tds format ={指數,收盤指數,漲跌(+/-),漲跌點數,漲跌百分比(%),特殊處理註記}
                for(int j=2; j<trs.size(); j++){
                    Elements tds = trs.get(j).select("td");
                    
                    if(tds.size()!=6)
                        continue;

                    for(int k=0; k<tds.size()-1; k++){
                        stock_map.get(stock_index_items[k]).add(tds.get(k).text());
                    }
                }   
            }
            return responseSuccess(stock_map);
        }catch(IOException io){
            return responseError(io.toString());
        }
    }

    public JSONObject responseSuccess(HashMap<String,ArrayList<String>> stock_map){
        JSONArray allstockArray= new JSONArray();
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();
        
        for (int i=0; i<stock_map.get(stock_index_items[0]).size(); i++){
            JSONObject tmpstock= new JSONObject();
            tmpstock.element("index_name",stock_map.get(stock_index_items[0]).get(i));
            tmpstock.element("closing_index",stock_map.get(stock_index_items[1]).get(i));
            tmpstock.element("change_sign",stock_map.get(stock_index_items[2]).get(i));
            tmpstock.element("price_change",stock_map.get(stock_index_items[3]).get(i));
            tmpstock.element("price_fluctuation(%)",stock_map.get(stock_index_items[4]).get(i));
            
            allstockArray.add(tmpstock);
        }
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
