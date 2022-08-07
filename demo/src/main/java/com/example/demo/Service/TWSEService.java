package com.example.demo.Service;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class TWSEService {
    String stockUrl;
    String[] stock_revenue_items = {"period","revenue","MoM","last_year","YoY",
    "cumulative","cumulative_year","cumulative_YoY"};

    public TWSEService(String stockUrl) throws IOException{
        this.stockUrl = stockUrl;
    }
    
    public JSONObject getCompanyMonthlyRevenue() {
        try{
            HashMap<String, ArrayList<String>> stock_map = new HashMap<String, ArrayList<String>>();
            for(int i=0; i<stock_revenue_items.length; i++){
                stock_map.put(stock_revenue_items[i], new  ArrayList<String>());
            }
            int count;

            //DriverServer使用System.setProperty 來指定路徑
            System.setProperty("webdriver.chrome.driver","/Library/Application Support/Google/chromedriver");
            WebDriver driver = new ChromeDriver();
            driver.get(this.stockUrl);
            System.out.println("driver title : "+ driver.getTitle());

            //click button "月營收" 
            driver.findElement(By.cssSelector("button[class='D(f) Jc(c) Ai(c) Whs(nw) Bxz(bb) Miw(80px) H(36px) Px(20px) Px(14px)--mobile Bdends(s) Bdendc($bd-primary-divider) "
            +"Bdendc(t):lc Bdendw(1px) Fz(14px) Fw(b) Ff($stockSiteFontFamily) Cur(p) O(0):f Bgc(#daedff) C($c-link-text) Bgc(#f0f3f5):h C($c-link-text):h']")).click();;

            //查找指定營收table的section id
            WebElement table = driver.findElement(By.cssSelector("section[id='qsp-revenue-table']"));
            
            //每一營收年月資料就有一條li
            List<WebElement> li_lists = table.findElements(By.cssSelector("li[class='List(n)']"));
            for (WebElement sub_list: li_lists){
                count=0;
                //div查找每一個table中{年月區間,單月合併,累積合併}，年月先放入map
                List<WebElement> sub_elements = sub_list.findElements(By.cssSelector("div"));
                stock_map.get(stock_revenue_items[count++]).add(sub_elements.get(2).getText());

                //處理單月合併＝{當月營收,月增率%,去年同月營收,年增率%}，累積合併={當月累計營收,去年累計營收,年增率%}，elements依序放入map
                for(int i=4; i<sub_elements.size(); i++){
                    List<WebElement> li_element = sub_elements.get(i).findElements(By.cssSelector("li"));
                    for(WebElement element : li_element){
                        stock_map.get(stock_revenue_items[count++]).add(element.getText());        
                    }
                }
            }

            driver.close();
            driver.quit();
            System.out.println("driver is closed");
            return (stock_map.get(stock_revenue_items[0]).size())!=0 ? responseMonthlyRevenueSuccess(stock_map)  : responseError("網頁查無符合資料");
        }catch(Exception io){
            return responseError(io.toString());
        }
    }

    private JSONObject responseMonthlyRevenueSuccess(HashMap<String,ArrayList<String>> stock_map){
        JSONArray allstockArray= new JSONArray();
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();
        //make up dividend days 指的是填息天數。
        String[] request_key = {"year/month","month_revenue","MoM","revenue_in_same_month_last_year","YoY",
        "cumulative_month_revenue","cumulative_revenue_last_year","cumulative_YoY"};

        for (int i=0; i<stock_map.get(stock_revenue_items[0]).size(); i++){
            JSONObject tempstock= new JSONObject();
            for(int j=0; j<request_key.length; j++){
                tempstock.element(request_key[j],stock_map.get(stock_revenue_items[j]).get(i));
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
