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
    String[] stock_index_items = {"period","dividend","right",
    "dividend_date","right_date","payment_date","right_payment_date","makeup_days"};

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
    
    public JSONObject  getCompanyDividendPolicy() {
        try{
            InputStream URLStream = openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLStream,"UTF-8"));

            String line=null;
            String all_lines="";
            while((line=buffer.readLine())!= null){
                all_lines+=line;
            }

            return CompanyDividendPolicyDataParsing(all_lines);  
        }catch(IOException io){
            return responseError(io.toString());
        }
    }

    public JSONObject CompanyDividendPolicyDataParsing(String all_lines){
        try{
            HashMap<String, ArrayList<String>> stock_map = new HashMap<String, ArrayList<String>>();
            for(int i=0; i<stock_index_items.length; i++){
                stock_map.put(stock_index_items[i], new  ArrayList<String>());
            }

            //只需要取得股利政策的table底下的<li class="List(n)">
            Document doc =  Jsoup.parse(new String(all_lines.getBytes("UTF-8"), "UTF-8"));
            Elements li_lists=doc.select("div.table-body-wrapper").get(0).select("li");

            //html div element format ={股利所屬期間,現金股利,股票股利,除息日,除權日,現金股利發放日,股票股利發放,填息天數,股利合計}
            for(int i=0; i<li_lists.size(); i++){
                //要取得的八個div element外面包了一層div、第一個element "股利所屬期間" 另外又包了一層div，故加起來有10個div。
                Elements tds = li_lists.get(i).select("div").get(0).select("div");
                
                if(tds.size()!=10)
                    continue;

                for(int j=2; j<tds.size(); j++){
                    stock_map.get(stock_index_items[j-2]).add(tds.get(j).text());
                }
            }   
            return responseCompanyDividendPolicySuccess(stock_map);
        }catch(IOException io){
            return responseError(io.toString());
        }
    }

    public JSONObject responseCompanyDividendPolicySuccess(HashMap<String,ArrayList<String>> stock_map){
        JSONArray allstockArray= new JSONArray();
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();
        //make up dividend days 指的是填息天數。
        String[] request_key = {"dividend_period","cash_dividend(dollors)","stock_dividend(shares)"
        ,"EX-dividend_date","EX-right_date","dividend_payment_date","right_payment_date","make_up_dividend_days"};

        for (int i=0; i<stock_map.get(stock_index_items[0]).size(); i++){
            JSONObject tempstock= new JSONObject();
            for(int j=0; j<request_key.length; j++){
                tempstock.element(request_key[j],stock_map.get(stock_index_items[j]).get(i));
            }
            allstockArray.add(tempstock);
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
