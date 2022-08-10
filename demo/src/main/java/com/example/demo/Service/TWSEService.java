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
    String[] stock_info_items = {"date","number","amount","openning","highest","lowest","closing","tradeVolume","average","turnoverRate"};

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
    
    public JSONObject getStockTradeInfo(String type, Integer specified_date) {
        try{
            InputStream URLStream = openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLStream,"UTF-8"));

            String line=null;
            String all_lines="";
            
            while((line=buffer.readLine())!= null){
                all_lines+=line;
            }
            
            if(type.equals("1")){
                return StockTradeInfoDaily(all_lines,specified_date);    
            }else if(type.equals("2")){
                return StockTradeInfoMonthly(all_lines,specified_date);
            }else if(type.equals("3")){
                return StockTradeInfoYearly(all_lines,specified_date);
            }else{
                return responseError("get stock trade info error.");
            }
        }catch(IOException io){
            return responseError(io.toString());
        }
    }

    private JSONObject StockTradeInfoDaily(String all_lines, Integer specified_date) {
        try{
            HashMap<String, ArrayList<String>> stock_map = new HashMap<String, ArrayList<String>>();
            for(int i=0; i<stock_info_items.length; i++){
                stock_map.put(stock_info_items[i], new ArrayList<String>());
            }

            Document doc =  Jsoup.parse(new String(all_lines.getBytes("UTF-8"), "UTF-8"));
            Elements trs = doc.select("tr");

            String temp[];
            int temp_ymd=0;
            boolean flag_ymd=false;
            
            //{日期,成交股數,成交金額,開盤價,最高價,最低價,收盤價,漲跌價差,成交筆數} trs資料第一行是中文標題欄位，故從i=2開始取得內文資料做處理。
            for(int i=2; i<trs.size();i++){
                flag_ymd=false;
            
                Elements tds = trs.get(i).select("td");
            
                //<td>111/07/01</td> 先處理日期字串中可能有異常空白問題"111 /07/01"，再以"/"分割年月日後後重新計算成西元年月日"20220701"。
                if(tds.size()== 9){
                    temp_ymd=19110000;
                    temp= tds.get(0).text().replaceAll(" ", "").split("/");
                    temp_ymd= temp_ymd+Integer.parseInt(temp[0])*10000+Integer.parseInt(temp[1])*100+Integer.parseInt(temp[2]); 
                    
                    flag_ymd=(temp_ymd==specified_date);
                }
                
                if(!flag_ymd)
                    continue;

                stock_map.get("date").add(String.valueOf(temp_ymd));
                stock_map.get("number").add(tds.get(1).text());
                stock_map.get("amount").add(tds.get(2).text());
                stock_map.get("openning").add(tds.get(3).text());
                stock_map.get("highest").add(tds.get(4).text());
                stock_map.get("lowest").add(tds.get(5).text());
                stock_map.get("closing").add(tds.get(6).text());
                stock_map.get("tradeVolume").add(tds.get(8).text());
                stock_map.get("turnoverRate").add("");
                stock_map.get("average").add("");
                return responseSuccess(stock_map);
            }
            return responseError("查無符合資料");
        }catch(IOException io){
            return responseError(io.toString());
        }
    }

    private JSONObject StockTradeInfoMonthly(String all_lines,Integer specified_month) {
        try{
            HashMap<String, ArrayList<String>> stock_map = new HashMap<String, ArrayList<String>>();
            for(int i=0; i<stock_info_items.length; i++){
                stock_map.put(stock_info_items[i], new ArrayList<String>());
            }

            Document doc =  Jsoup.parse(new String(all_lines.getBytes("UTF-8"), "UTF-8"));
            Elements trs = doc.select("tr");

            int temp_ymd=0;
            int specified_yyyymm=Integer.parseInt(specified_month.toString().substring(0,6));
            
            boolean flag_ymd=false;

            //{年度,月份,最高價,最低價,加權(A/B)平均價,成交筆數,成交金額(A),成交股數(B),週轉率(%)}
            for(int i=trs.size()-1; i>1;i--){
                flag_ymd=false;
                
                Elements tds = trs.get(i).select("td");

                if(tds.size()== 9){
                    temp_ymd=(1911+Integer.parseInt(tds.get(0).text().trim()))*100+Integer.parseInt(tds.get(1).text().trim());
                    flag_ymd=(temp_ymd==specified_yyyymm);
                }
                
                if(!flag_ymd)
                    continue;

                stock_map.get("date").add(String.valueOf(temp_ymd));
                stock_map.get("highest").add(tds.get(2).text());
                stock_map.get("lowest").add(tds.get(3).text());
                stock_map.get("average").add(tds.get(4).text());
                stock_map.get("tradeVolume").add(tds.get(5).text());
                stock_map.get("amount").add(tds.get(6).text());
                stock_map.get("number").add(tds.get(7).text());
                stock_map.get("turnoverRate").add(tds.get(8).text());
                stock_map.get("closing").add("");
                stock_map.get("openning").add("");
                return responseSuccess(stock_map);
            }

            return responseError("查無符合資料");
        }catch(IOException io){
            return responseError(io.toString());
        }
    }

    private JSONObject StockTradeInfoYearly(String all_lines, Integer specified_year) {
        try{
            HashMap<String, ArrayList<String>> stock_map = new HashMap<String, ArrayList<String>>();
            for(int i=0; i<stock_info_items.length; i++){
                stock_map.put(stock_info_items[i], new ArrayList<String>());
            }

            Document doc =  Jsoup.parse(new String(all_lines.getBytes("UTF-8"), "UTF-8"));
            Elements trs = doc.select("tr");
            
            int temp_ymd=0;
            int specified_yyyy=Integer.parseInt(specified_year.toString().substring(0,4));
            
            boolean flag_ymd=false;
            
            //{年度,成交股數,成交金額,成交筆數,最高價,日期,最低價,日期,收盤平均價}
            for(int i=trs.size()-1; i>1;i--){
                flag_ymd=false;
                
                Elements tds = trs.get(i).select("td");
                
                if(tds.size()== 9){
                    temp_ymd=1911+Integer.parseInt(tds.get(0).text().trim());
                    flag_ymd=(temp_ymd==specified_yyyy);
                }
                
                if(!flag_ymd)
                    continue;

                stock_map.get("date").add(String.valueOf(temp_ymd));
                stock_map.get("number").add(tds.get(1).text());
                stock_map.get("amount").add(tds.get(2).text());
                stock_map.get("openning").add("");
                stock_map.get("highest").add(tds.get(4).text());
                stock_map.get("lowest").add(tds.get(6).text());
                stock_map.get("closing").add("");
                stock_map.get("tradeVolume").add(tds.get(3).text());
                stock_map.get("turnoverRate").add("");
                stock_map.get("average").add(tds.get(8).text());
                return responseSuccess(stock_map);
            }
            return responseError("查無符合資料");
        }catch(IOException io){
            return responseError(io.toString());
        }
    }

    private JSONObject responseSuccess(HashMap<String,ArrayList<String>> stock_map){
        JSONArray allstockArray= new JSONArray();
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();

        for (int i=0; i<stock_map.get("date").size(); i++){
            JSONObject tempstock= new JSONObject();
            tempstock.element("share_number(B)",stock_map.get("number").get(i));
            tempstock.element("share_amount(A)",stock_map.get("amount").get(i));
            tempstock.element("trade_volume",stock_map.get("tradeVolume").get(i));
            tempstock.element("openning_price",stock_map.get("openning").get(i));
            tempstock.element("hightest_price",stock_map.get("highest").get(i));
            tempstock.element("lowest_price",stock_map.get("lowest").get(i));
            tempstock.element("closing_price(average)",stock_map.get("closing").get(i));
            tempstock.element("the_average_of_ShareAmount(A)_and_ShareNumber(B)",stock_map.get("average").get(i));
            tempstock.element("turnover_rate(%)",stock_map.get("turnoverRate").get(i));
            
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
