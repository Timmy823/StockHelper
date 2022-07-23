package com.example.demo.Controller;

import com.example.demo.Service.TWSEService;
import net.sf.json.JSONObject;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TWSEController {
    @GetMapping("/twse/getAllCompanyList")
    public JSONObject getCompanyList(@RequestBody JSONObject input, TWSEService twse) {

        String twseUrl="";
        int list_level = input.getInt("type");
        twseUrl = "https://isin.twse.com.tw/isin/C_public.jsp?strMode="+String.valueOf(list_level*2);
        try {
            twse = new TWSEService(twseUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return twse.getCompanyList();
    }
}