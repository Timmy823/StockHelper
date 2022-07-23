package com.example.demo.Controller;

import com.example.demo.Service.StockService;
import com.example.demo.Component.StockTradeInfoParam;
import net.sf.json.JSONObject;

import java.io.IOException;


import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TWSEController {
    @GetMapping("/twse/getStockTradeInfo")
    public JSONObject getStockTradeInfo(@Valid @RequestBody StockTradeInfoParam input,StockService stock){
        String stockUrl="";
    
        Integer specified_date =input.get_date();
        String type=input.get_type();
        String id =input.get_stockID();
        if(type.equals("1")){
            stockUrl="https://www.twse.com.tw/exchangeReport/STOCK_DAY?response=html&date="+specified_date+"&stockNo="+id;
            try{
                stock= new StockService(stockUrl);
                System.out.println(stockUrl);
                return stock.getStockTradeInfoDaily(type,specified_date);
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        if(type.equals("2")){
            stockUrl= "https://www.twse.com.tw/exchangeReport/FMSRFK?response=html&date="+specified_date+"&stockNo="+id;
            try{
                stock= new StockService(stockUrl);
                System.out.println(stockUrl);
                return stock.getStockTradeInfoMonthly(specified_date);
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        if(type.equals("3")){
            stockUrl="https://www.twse.com.tw/exchangeReport/FMNPTK?response=html&stockNo="+id;
            try{
                stock= new StockService(stockUrl);
                System.out.println(stockUrl);
                return stock.getStockTradeInfoYearly(specified_date);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
       
        return null;
    }
}
