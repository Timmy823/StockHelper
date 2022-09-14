package com.example.demo.Service;
import java.io.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TWSEService {
    String stockUrl;

    public TWSEService(String stockUrl) throws IOException{
        this.stockUrl = stockUrl;
    }
    
    public JSONObject  getStockRealtimeOTCTradeInfo() {
        String [] web_info_string ={"v","o","h","l","u","w","t","z","tv"};
        String[] trade_info_string = {"accumulation_volume","openning_price","highest_price","lowest_price","limit_up","limit_down"
        ,"trade_time","price","trade_volume"};

        try{
            JSONObject realtime_trade_info = new JSONObject();
            //取得html內文 body資料
            HttpsService open_url = new HttpsService();
            InputStream URLstream = open_url.openURL(this.stockUrl);

            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "UTF-8"));

            String line=null;
            String all_lines="";
            while((line=buffer.readLine())!= null){
                all_lines+=line;
            }

            JSONArray realtime_info = JSONObject.fromObject(all_lines).getJSONArray("msgArray");
            for (int i=0 ; i<realtime_info.size(); i++) {
                for(int j=0; j<web_info_string.length ; j++){
                    realtime_trade_info.element(trade_info_string[j], realtime_info.getJSONObject(i).getString(web_info_string[j]));
                }
            }
            return ResponseService.responseSuccess(realtime_trade_info); 
        }catch(IOException io){
            return ResponseService.responseError("error", io.toString());
        }
    }
}
