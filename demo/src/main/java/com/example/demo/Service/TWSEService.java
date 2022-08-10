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
    String[] stock_revenue_Yearly_items = {"period","revenue","last_year","YoY"};

    public TWSEService(String stockUrl) throws IOException{
        this.stockUrl = stockUrl;
    }
    
    public JSONObject getCompanyYearlyRevenue() {
        try{
            HashMap<String, ArrayList<String>> stock_map = new HashMap<String, ArrayList<String>>();
            for(int i=0; i<stock_revenue_Yearly_items.length; i++){
                stock_map.put(stock_revenue_Yearly_items[i], new  ArrayList<String>());
            }
            //紀錄stock_revenue_Yearly_items 位置
            int count;

            //DriverServer使用System.setProperty 來指定路徑
            System.setProperty("webdriver.chrome.driver","/Library/Application Support/Google/chromedriver");
            WebDriver driver = new ChromeDriver();
            driver.get(this.stockUrl);
            System.out.println("driver title : "+ driver.getTitle());

            //click button "年營收" 
            List<WebElement> button =driver.findElement(By.cssSelector("section[id='qsp-revenue-chart']")).findElements(By.cssSelector("button"));
            button.get(2).click();
            
            //查找指定營收table的section id
            WebElement table = driver.findElement(By.cssSelector("section[id='qsp-revenue-table']"));
            
            //每一營收資料就有一條li
            List<WebElement> li_lists = table.findElements(By.cssSelector("li[class='List(n)']"));
            for (WebElement sub_list: li_lists){
                count=0;

                //div查找每一個table中{年月區間,單月合併}，年月先放入map
                List<WebElement> sub_elements = sub_list.findElements(By.cssSelector("div"));
                stock_map.get(stock_revenue_Yearly_items[count++]).add(sub_elements.get(2).getText());

                ////處理單月合併＝{當年營收,去年營收,年增率%}，elements依序放入map
                List<WebElement> li_element = sub_elements.get(3).findElements(By.cssSelector("li"));
                if(li_element.size() != 3)
                    continue;
                for(WebElement element : li_element){
                    stock_map.get(stock_revenue_Yearly_items[count++]).add(element.getText());        
                }
            }

            driver.close();
            driver.quit();
            System.out.println("driver is closed");
            return (stock_map.get(stock_revenue_Yearly_items[stock_revenue_Yearly_items.length-1]).size())!=0 ? responseYearlyRevenueSuccess(stock_map)  : responseError("網頁查無符合資料");
        }catch(Exception io){
            return responseError(io.toString());
        }
    }

    private JSONObject responseYearlyRevenueSuccess(HashMap<String,ArrayList<String>> stock_map){
        JSONArray allstockArray= new JSONArray();
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();

        String[] request_key = {"year","yearly_revenue","last_year_revenue","YoY"};

        for (int i=0; i<stock_map.get(stock_revenue_Yearly_items[0]).size(); i++){
            JSONObject tempstock= new JSONObject();
            for(int j=0; j<request_key.length; j++){
                tempstock.element(request_key[j],stock_map.get(stock_revenue_Yearly_items[j]).get(i));
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
