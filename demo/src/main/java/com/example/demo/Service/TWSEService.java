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
    String[] stock_index_items = {"date","before_price","ex_dividend","dividend","ex_yield","yield","total_yield","pe_ratio","eps","makeup_days","makeup_date"};

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
                stock_map.put(stock_index_items[i], new ArrayList<String>());
            }

            Document doc =  Jsoup.parse(new String(all_lines.getBytes("UTF-8"), "UTF-8"));
            Elements trs = doc.select("tr");

           //html tables format ={除權息日,除權息前股價,配息(元/股),配股(元/百股),現金殖利率,股票殖利率,合計殖利率,本益比price-earnings ratio,EPS,填息天數,填息日期}
            for(int i=1; i<trs.size(); i++){
                Elements tds = trs.get(i).select("td");
                if(tds.size()!=11)
                    continue;
                for(int j=0; j<tds.size(); j++){
                    stock_map.get(stock_index_items[j]).add(tds.get(j).text());
                }
            }   
            return responseStockMarketIndexSuccess(stock_map);
        }catch(IOException io){
            return responseError(io.toString());
        }
    }

    public JSONObject responseStockMarketIndexSuccess(HashMap<String,ArrayList<String>> stock_map){
        JSONArray allstockArray= new JSONArray();
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();
        //make up dividend days 指的是填息天數；make up dividend date 填息日期
        String[] request_key = {"(EX)dividend_date","dividend_price_before","EX-dividend(dollor/share)","dividend(dollor/hundredShare)",
        "cash_dividend_yeild","dividend_yeild","total_yeil","price-earnings_ratio","EPS","makeup_dividend_days","makeup_dividendt_date"};
        
        for (int i=0; i<stock_map.get(stock_index_items[0]).size(); i++){
            JSONObject tmpstock= new JSONObject();
            for(int j=0; j<request_key.length; j++){
                tmpstock.element(request_key[j],stock_map.get(stock_index_items[j]).get(i));
            }
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