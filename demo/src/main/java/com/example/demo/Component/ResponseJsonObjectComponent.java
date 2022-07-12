/*package com.example.demo.Component;

import java.util.HashMap;

import net.minidev.json.JSONObject;

abstract class AbstractResponseJsonObjectComponent{
    
    protected abstract void putStatusCode(); 
    protected abstract void putDataList();
    protected abstract void resultJsonObjectStructure();

}


  
class ResponseJsonObjectComponent extends AbstractResponseJsonObjectComponent{
    JSONObject status_code = new JSONObject();
    JSONObject data = new JSONObject();
    HashMap<String, JSONObject> result = new HashMap<String, JSONObject>();



    protected void  putStatusCode(String s,String error_msg){
        status_code.put("status", s);
        status_code.put("desc", error_msg);
    }
    protected void putDataList(String s1,String s2) {
        data.put(s1,s2);
        
    }
    protected void resultJsonObjectStructure() {
        result.put("data", data);
        result.put("metadata", status_code);
    }

}*/

