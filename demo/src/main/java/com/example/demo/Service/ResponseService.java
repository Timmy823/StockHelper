package com.example.demo.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ResponseService {
    public static JSONObject responseError(String status, String error_msg) {
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();

        data.put("data", "");

        status_code.put("status", status);
        status_code.put("desc", error_msg);

        result.put("metadata", status_code);
        result.put("data", data);

        return result;
    }

    public static JSONObject responseSuccess(JSONObject data) {
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();

        status_code.put("status", "success");
        status_code.put("desc", "");

        result.put("metadata", status_code);
        result.put("data", data);

        return result;
    }

    public static JSONObject responseJSONArraySuccess(JSONArray data) {
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();

        status_code.put("status", "success");
        status_code.put("desc", "");

        result.put("metadata", status_code);
        result.put("data", data);
        return result;
    }
}
