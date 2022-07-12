package com.example.demo.Controller;

import com.example.demo.Service.TWSEService;
import net.minidev.json.JSONObject;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TWSEController {
    @GetMapping("/twse/getAllCompanyList")
    public JSONObject getCompanyList(TWSEService twse) {

        String twseUrl = "https://isin.twse.com.tw/isin/C_public.jsp?strMode=2";
        try {
            twse = new TWSEService(twseUrl);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return twse.getCompanyList();
    }

    @GetMapping("/twse/getOTCCompanyList")
    public JSONObject getOTCList(TWSEService otc){
        String otcUrl="https://isin.twse.com.tw/isin/C_public.jsp?strMode=4";
        try{
            otc = new TWSEService(otcUrl);
        }catch(IOException e){
            e.printStackTrace();
        }
        return otc.getCompanyList();
    }

}