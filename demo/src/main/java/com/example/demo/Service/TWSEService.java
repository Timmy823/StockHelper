package com.example.demo.Service;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext; 
import javax.net.ssl.TrustManager; 
import javax.net.ssl.X509TrustManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import net.minidev.json.JSONObject;



public class TWSEService {
    String twseUrl;
    ArrayList<String> companyId = new ArrayList<>();
    ArrayList<String> companyName = new ArrayList<>();

    public TWSEService(String twseUrl) throws IOException{
        this.twseUrl = twseUrl;
    }
    
    private InputStream openURL(String urlPath) throws IOException{
        URL url = new URL(urlPath);
        createTrustManager(url);

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
    public JSONObject getCompanyList() {
        try {
            InputStream URLstream = openURL(this.twseUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream,"BIG5"));
            String line = null;
            String alllines = ""; 
            while ((line=buffer.readLine()) != null) {
                alllines+=line;
            }

            Document doc = Jsoup.parse(new String(alllines.getBytes("UTF-8"), "UTF-8"));
            Elements trs = doc.select("tr");
            
            String tmp[];
            for(int i=0; i<trs.size(); i++){
                Elements tds = trs.get(i).select("td");
                if(tds.size() == 7){
                    //<td bgcolor="#FAFAD2">1101　台泥</td>
                    tmp = tds.get(0).text().split("　");
                    //get stock company ID
                    if(tmp[0].trim().length()==4){
                        companyId.add(tmp[0].trim());
                        companyName.add(tmp[1].trim());
                    }
                }
            }
            JSONObject object = new JSONObject();
            for (int i=0; i<companyId.size();i++){
                object.put(companyId.get(i),companyName.get(i));
            }
            return responseListSuccess(object);
        }   
        catch (IOException io){
            return responseError(io.toString());
        }
    }
    /**
     * 建立ssl憑證
     * @param urlObj
     * @return
     */
    private TrustManager createTrustManager(URL urlObj){
        System.setProperty("java.protocol.handler.pkgs","com.sun.net.ssl.internal.www.protocol");
        System.setProperty("javax.net.ssl.trustStore","keystore");
        TrustManager trust = new X509TrustManager(){
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType){
            }
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
            }
        };

        SSLContext sslcontext;
        try {
            sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[] { trust }, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }


        return trust;
    }

    private JSONObject responseListSuccess(JSONObject temp_data) {
        JSONObject status_code = new JSONObject();
        JSONObject data = new JSONObject();
        HashMap<String, JSONObject> result = new HashMap<String, JSONObject>();

        status_code.put("status", "success");
        status_code.put("desc", "");

        data.put("data", temp_data);

        result.put("metadata", status_code);
        result.put("data", data);

        return new JSONObject(result);
    }
    
    private JSONObject responseError(String error_msg) {
        JSONObject status_code = new JSONObject();
        JSONObject data = new JSONObject();
        HashMap<String, JSONObject> result = new HashMap<String, JSONObject>();

        status_code.put("status", "error");
        status_code.put("desc", error_msg);

        data.put("data", new JSONObject());

        result.put("metadata", status_code);
        result.put("data", data);

        return new JSONObject(result);
    }




}
