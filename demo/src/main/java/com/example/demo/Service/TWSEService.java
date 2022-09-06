package com.example.demo.Service;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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

    public JSONObject getCompanyInfoProfile() {
        String [] stock_items={"main_business","created_date","telephone","listed_date","fax","website","chairman"
                                  ,"email","president","share_capital","share_number","address","market_value","share_hoding_radio"};
        String split_string [];
        //紀錄stock_items位置
        int count=0;
        JSONObject stock= new JSONObject();
        //initailize items_position = {4,5,6,7,9,10,11,12,14,16,17,18,20}; 
        ArrayList<Integer>items_position = new ArrayList<>(); 
        items_position.add(4);
        items_position.add(5);
        items_position.add(6);
        items_position.add(7);
        items_position.add(9);
        items_position.add(11);
        items_position.add(12);
        items_position.add(14);
        items_position.add(16);
        items_position.add(17);
        items_position.add(18);
        items_position.add(20);

        try {
            InputStream URLstream = openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream,"UTF-8"));

            String line;
            String all_lines = ""; 
            while ((line=buffer.readLine()) != null) {
                all_lines+=line;
            }

            Document doc= Jsoup.parse(new String(all_lines.getBytes("UTF-8"),"UTF-8"));
            //div.class="D(f) Fx(a) Mb($m-module)"
            //div#id=main-2-QuoteProfile-Proxy > div.class="grid-item item-span-6 break-mobile"
            Elements divseElements = doc.select("div#main-2-QuoteProfile-Proxy");
            Elements div= divseElements.get(0).select("div.grid-item.item-span-6.break-mobile");

            //主要業務內容
            Elements divs2= doc.select("section");
            divs2.get(0).text().split(" ");
            split_string=divs2.get(0).text().split(" ");
            stock.element(stock_items[count++], split_string[split_string.length-1].trim());

            for(int i=0; i<div.size(); i++) {
                split_string=div.get(i).text().split(" ");
                if (split_string[0].equals("股利所屬期間"))
                    break;
                if(items_position.contains(i))
                    stock.element(stock_items[count++], split_string[split_string.length-1].trim());
            }
            return responseSuccess(stock);
        }catch (IOException io){
            return responseError(io.toString());
        }
    }

    private JSONObject responseSuccess(JSONObject data){
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();
        
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
