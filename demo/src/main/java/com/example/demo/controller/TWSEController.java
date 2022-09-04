package com.example.demo.Controller;

import net.sf.json.JSONObject;

import java.io.IOException;

import org.springframework.web.bind.annotation.*;

import com.example.demo.Service.TWSEService;
    
@RestController
public class TWSEController {

    @GetMapping("/twse/getCompanyProfile")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject getCompanyInfoProfile(@RequestBody JSONObject input,TWSEService stock){
        Integer stockid =input.getInt("id");
        String stockUrl="https://tw.stock.yahoo.com/quote/" + stockid + "/profile";
        try{
            stock= new TWSEService(stockUrl);  
            return stock.getCompanyInfoProfile(); 
        }catch(IOException e) {
            return stock.responseError(e.toString());
        }   
    }
}
