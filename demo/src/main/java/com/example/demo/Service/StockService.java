package com.example.demo.Service;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class StockService {
    String stockUrl;
    ArrayList<Integer> stock_list_date = new ArrayList<>();
    ArrayList<String> stock_list_number = new ArrayList<>();
    ArrayList<String> stock_list_amount = new ArrayList<>();
    ArrayList<String> stock_list_openningPrice = new ArrayList<>();
    ArrayList<String> stock_list_highestPrice= new ArrayList<>();
    ArrayList<String> stock_list_lowestPrice= new ArrayList<>();
    ArrayList<String> stock_list_closingPrice= new ArrayList<>();
    ArrayList<String> stock_list_tradeVolume= new ArrayList<>();
    ArrayList<String> stock_list_turnoverRate= new ArrayList<>();
    ArrayList<String> stock_list_average= new ArrayList<>();

    JSONObject status_code = new JSONObject();
    JSONObject data = new JSONObject();
    JSONObject result = new JSONObject();

   
    public StockService(String stockUrl) throws IOException{
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


    public JSONObject getStockTradeInfoDaily(String type, Integer specified_date) {
        try{
            InputStream URLStream = openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLStream,"UTF-8"));
            String line=null;
            String all_lines="";
            
            while((line=buffer.readLine())!= null){
                all_lines+=line;
            }
            
            Document doc =  Jsoup.parse(new String(all_lines.getBytes("UTF-8"), "UTF-8"));
            Elements trs = doc.select("tr");
            
            String tmp[];
            int tmp_ymd=0;
            boolean flag_ymd=false;
            
            //{日期,成交股數,成交金額,開盤價,最高價,最低價,收盤價,漲跌價差,成交筆數}
            for(int i=2; i<trs.size();i++){
                flag_ymd=false;
            
                Elements tds = trs.get(i).select("td");
            
                //<td>111/07/01</td>
                if(tds.size()== 9){
                    tmp_ymd=19110000;
                    tmp= tds.get(0).text().split("/");
                    tmp_ymd= tmp_ymd+Integer.parseInt(tmp[0].trim())*10000+Integer.parseInt(tmp[1].trim())*100+Integer.parseInt(tmp[2].trim()); 
                    
                    if(tmp_ymd==specified_date){
                        flag_ymd=true;
                    }
                }
                
                if(flag_ymd){
                    stock_list_date.add(tmp_ymd);
                    stock_list_number.add(tds.get(1).text());
                    stock_list_amount.add(tds.get(2).text());
                    stock_list_openningPrice.add(tds.get(3).text());
                    stock_list_highestPrice.add(tds.get(4).text());
                    stock_list_lowestPrice.add(tds.get(5).text());
                    stock_list_closingPrice.add(tds.get(6).text());
                    stock_list_tradeVolume.add(tds.get(8).text());
                    stock_list_turnoverRate.add("");
                    stock_list_average.add("");
                    break;
                }
            }
            return responseSuccessObject();
        
        }catch(IOException io){
            return responseError(io.toString());

        }
    }


    public JSONObject getStockTradeInfoMonthly(Integer specified_month) {
        try{
            InputStream URLStream = openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLStream,"UTF-8"));
            String line=null;
            String all_lines="";

            while((line=buffer.readLine())!= null){
                all_lines+=line;
            }

            Document doc =  Jsoup.parse(new String(all_lines.getBytes("UTF-8"), "UTF-8"));
            Elements trs = doc.select("tr");

            int tmp_ymd=0;
            int specified_yyyymm=Integer.parseInt(specified_month.toString().substring(0,6));
            
            boolean flag_ymd=false;

            //{年度,月份,最高價,最低價,加權(A/B)平均價,成交筆數,成交金額(A),成交股數(B),週轉率(%)}
            for(int i=trs.size()-1; i>1;i--){
                flag_ymd=false;
                
                Elements tds = trs.get(i).select("td");

                if(tds.size()== 9){
                    tmp_ymd=(1911+Integer.parseInt(tds.get(0).text().trim()))*100+Integer.parseInt(tds.get(1).text().trim());
                    
                    if(tmp_ymd==specified_yyyymm){
                        flag_ymd=true;
                    }
                }

                if(flag_ymd){
                    stock_list_date.add(tmp_ymd);
                    stock_list_highestPrice.add(tds.get(2).text());
                    stock_list_lowestPrice.add(tds.get(3).text());
                    stock_list_average.add(tds.get(4).text());
                    stock_list_tradeVolume.add(tds.get(5).text());
                    stock_list_amount.add(tds.get(6).text());
                    stock_list_number.add(tds.get(7).text());
                    stock_list_turnoverRate.add(tds.get(8).text());
                    stock_list_closingPrice.add("");
                    stock_list_openningPrice.add("");
                    break;
                }
            }

            return responseSuccessObject();
        
        }catch(IOException io){
            return responseError(io.toString());

        }
    }


    public JSONObject getStockTradeInfoYearly(Integer specified_year) {
        try{
            InputStream URLStream = openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLStream,"UTF-8"));
            String line=null;
            String all_lines="";
            
            while((line=buffer.readLine())!= null){
                all_lines+=line;
            }
            
            Document doc =  Jsoup.parse(new String(all_lines.getBytes("UTF-8"), "UTF-8"));
            Elements trs = doc.select("tr");
            
            int tmp_ymd=0;
            int specified_yyyy=Integer.parseInt(specified_year.toString().substring(0,4));
            
            boolean flag_ymd=false;
            
            //{年度,成交股數,成交金額,成交筆數,最高價,日期,最低價,日期,收盤平均價}
            for(int i=trs.size()-1; i>1;i--){
                flag_ymd=false;
                
                Elements tds = trs.get(i).select("td");
                
                if(tds.size()== 9){
                    tmp_ymd=1911+Integer.parseInt(tds.get(0).text().trim());
                    
                    if(tmp_ymd==specified_yyyy){
                        flag_ymd=true;
                    }
                }
                
                if(flag_ymd){
                    stock_list_date.add(tmp_ymd);
                    stock_list_number.add(tds.get(1).text());
                    stock_list_amount.add(tds.get(2).text());
                    stock_list_tradeVolume.add(tds.get(3).text());
                    stock_list_highestPrice.add(tds.get(4).text());
                    stock_list_lowestPrice.add(tds.get(6).text());
                    stock_list_average.add(tds.get(8).text());
                    stock_list_closingPrice.add("");
                    stock_list_openningPrice.add("");
                    stock_list_turnoverRate.add("");
                    break;
                }
            }
            return responseSuccessObject();
        
        }catch(IOException io){
            return responseError(io.toString());

        }
    }
    private JSONObject responseError(String error_msg) {
        putStatusCode("error",error_msg);
        data.put("data","");
        resultJsonObjectStructure();
        return result;
    }
    private void  putStatusCode(String s,String msg){
        status_code.put("status", s);
        status_code.put("desc", msg);
    }
    private void resultJsonObjectStructure() {
        result.put("metadata", status_code);
        result.put("data", data);
    }

    public JSONObject responseSuccessObject(){
        putStatusCode("success","");
        JSONArray allstockArray= new JSONArray();

        for (int i=0; i<stock_list_date.size(); i++){
            JSONObject tmpstock= new JSONObject();
        
            tmpstock.element("share_number(B)",stock_list_number.get(i));
            tmpstock.element("share_amount(A)",stock_list_amount.get(i));
            tmpstock.element("trade_volume",stock_list_tradeVolume.get(i));
            tmpstock.element("openning_price",stock_list_openningPrice.get(i));
            tmpstock.element("hightest_price",stock_list_highestPrice.get(i));
            tmpstock.element("lowest_price",stock_list_lowestPrice.get(i));
            tmpstock.element("closing_price(average)",stock_list_closingPrice.get(i));
            tmpstock.element("the_average_of_ShareAmount(A)_and_ShareNumber(B)",stock_list_average.get(i));
            tmpstock.element("turnover_rate(%)",stock_list_turnoverRate.get(i));
            
            allstockArray.add(tmpstock);
        }
        data.put("stockdata",allstockArray);
        
        resultJsonObjectStructure();
        return result;
    }
}
