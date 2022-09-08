package com.example.demo.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.example.demo.Component.StockComponent.StockIdParam;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TWSEService {
    private StringRedisTemplate stringRedisTemplate;

    private String stockUrl;
    String[] stock_info_items = { "date", "number", "amount", "openning", "highest", "lowest", "closing", "tradeVolume",
            "average", "turnoverRate" };
    String[] stock_index_items = { "period", "dividend", "right",
            "dividend_date", "right_date", "payment_date", "right_payment_date", "makeup_days" };

    public TWSEService(String stockUrl, StringRedisTemplate stringRedisTemplate) throws IOException {
        this.stockUrl = stockUrl;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private InputStream openURL(String urlPath) throws IOException {
        URL url = new URL(urlPath);
        createTrustManager(url);

        // open a url connection.
        HttpsURLConnection url_connection = (HttpsURLConnection) url.openConnection();
        url_connection.setDoInput(true);
        url_connection.setDoOutput(true);

        // set request method
        url_connection.setRequestMethod("GET");
        url_connection.setConnectTimeout(15000);
        url_connection.setReadTimeout(15000);
        // set request header
        url_connection.setRequestProperty("User-Agent",
                " Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
        url_connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        url_connection.setRequestProperty("Content-Length", Integer.toString(1000));
        url_connection.setRequestProperty("connection", "Keep-Alive");

        System.out.println("ready to connect!");

        url_connection.connect();

        // the method is used to access the header filed after the connection
        if (url_connection.getResponseCode() != 200) {
            System.out.print("\nConnection Fail:" + url_connection.getResponseCode());
        }

        return url_connection.getInputStream();
    }

    public JSONObject getCompanyMonthlyProductRevenueRaio(StockIdParam data) {
        try {
            JSONArray result_Array = new JSONArray();

            // ArrayList<String> ignore_string = new ArrayList<>();
            // ignore_string.add("(");
            // ignore_string.add(")");
            
            InputStream URLstream = openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "UTF-8"));
            String line = null;
            String alllines = "";
            while ((line = buffer.readLine()) != null) {
                alllines += line;
            }
            Document doc = Jsoup.parse(new String(alllines.getBytes("UTF-8"), "UTF-8"));
            Elements ratio_columns = doc.select("div#divSaleMonProdData").select("div#divDetail").select("tr[align=center]");

            JSONArray product_array = new JSONArray();
            JSONObject product_item = new JSONObject();
            JSONObject revenue = new JSONObject();  
            for(int i=0; i<ratio_columns.size(); i++) {
                Elements column = ratio_columns.get(i).select("td");
                
                //new monthly revenue
                if(column.size() == 16) {
                    //put product_array to revenue
                    if(product_array.size() != 0) {
                        revenue.put("detail", product_array);
                        result_Array.add(revenue);
                    }
                    
                    product_array = new JSONArray();
                    revenue = new JSONObject();
                    product_item = new JSONObject(); 
                    //initail product_item is "-"
                    product_item.put("name", "-");
                    product_item.put("ratio", "-");
                    product_item.put("MoM", "-");
                    product_item.put("YoY", "-");
                    //put product ratio data into product_item. example:  (1) 晶圓,1065,90.8,-4.76,+14.7
                    if(column.get(7).text().contains("(")) {
                        product_item.put("name", column.get(8).text());
                        product_item.put("ratio", column.get(10).text());
                        product_item.put("MoM", column.get(11).text());
                        product_item.put("YoY", column.get(12).text());
                    }
                    product_array.add(product_item);

                    revenue.put("date", column.get(0).text());
                }
                //product ratio for monthly revenue
                if(column.size() == 9) {
                    //skip not a product ratio data
                    if(!column.get(0).text().contains("(") && !column.get(0).text().contains("減"))
                        continue;

                    product_item = new JSONObject();
                    product_item.put("name", column.get(1).text());
                    product_item.put("ratio", column.get(3).text());
                    product_item.put("MoM", column.get(4).text());
                    product_item.put("YoY", column.get(5).text());
                    product_array.add(product_item);
                }
            }
            //put the final product_array into revenue
            revenue.put("detail", product_array);
            result_Array.add(revenue);
            return responseSuccess(result_Array);
        } catch (IOException io) {
            return responseError(io.toString());
        }
        
    }
    public JSONObject getCompanyList(int type) {
        try {
            String get_company_list_redis_key = (type == 1) ? "listed_company_list" : "OTC_company_list";
            int redis_ttl = 86400; // redis存活一天

            String company_list_string = this.stringRedisTemplate.opsForValue().get(get_company_list_redis_key);
            if (company_list_string != null) {
                System.out.println(company_list_string);
                return responseSuccess(JSONArray.fromObject(company_list_string));
            }

            JSONArray company_list = new JSONArray();

            InputStream URLstream = openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "BIG5"));
            String line = null;
            String alllines = "";
            while ((line = buffer.readLine()) != null) {
                alllines += line;
            }

            Document doc = Jsoup.parse(new String(alllines.getBytes("UTF-8"), "UTF-8"));
            Elements trs = doc.select("tr");

            String company_data[];
            for (int i = 0; i < trs.size(); i++) {
                Elements tds = trs.get(i).select("td");
                JSONObject company = new JSONObject();
                if (tds.size() == 7) {
                    // <td bgcolor="#FAFAD2">1101 台泥</td>
                    company_data = tds.get(0).text().split("　");
                    // get stock company ID
                    if (company_data[0].trim().length() == 4) {
                        company.put("ID", company_data[0].trim());
                        company.put("Name", company_data[1].trim());
                        // <td bgcolor="#FAFAD2">1962/02/09</td>
                        company.put("上市/上櫃日期", tds.get(2).text());
                        // <td bgcolor="#FAFAD2">水泥工業</td>
                        company.put("產業別", tds.get(4).text());

                        company_list.add(company);
                    }
                }
            }

            this.stringRedisTemplate.opsForValue().setIfAbsent(get_company_list_redis_key,
                    company_list.toString(), redis_ttl, TimeUnit.SECONDS);

            return responseSuccess(company_list);
        } catch (IOException io) {
            return responseError(io.toString());
        }
    }

    public JSONObject getCompanyInfoProfile() {
        String[] stock_items = { "main_business", "created_date", "telephone", "listed_date", "fax", "website",
                "chairman", "email", "president", "share_capital", "share_number", "address", "market_value",
                "share_hoding_radio" };
        String split_string[];
        // 紀錄stock_items位置
        int count = 0;
        JSONObject stock = new JSONObject();
        // initailize items_position = {4,5,6,7,9,10,11,12,14,16,17,18,20};
        ArrayList<Integer> items_position = new ArrayList<>();
        items_position.add(4);
        items_position.add(5);
        items_position.add(6);
        items_position.add(7);
        items_position.add(9);
        items_position.add(11);
        items_position.add(12);
        items_position.add(14);
        items_position.add(16);
        items_position.add(17);
        items_position.add(18);
        items_position.add(20);

        try {
            InputStream URLstream = openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "UTF-8"));

            String line;
            String all_lines = "";
            while ((line = buffer.readLine()) != null) {
                all_lines += line;
            }

            Document doc = Jsoup.parse(new String(all_lines.getBytes("UTF-8"), "UTF-8"));
            // div.class="D(f) Fx(a) Mb($m-module)"
            // div#id=main-2-QuoteProfile-Proxy > div.class="grid-item item-span-6
            // break-mobile"
            Elements divseElements = doc.select("div#main-2-QuoteProfile-Proxy");
            Elements div = divseElements.get(0).select("div.grid-item.item-span-6.break-mobile");

            // 主要業務內容
            Elements divs2 = doc.select("section");
            divs2.get(0).text().split(" ");
            split_string = divs2.get(0).text().split(" ");
            stock.element(stock_items[count++], split_string[split_string.length - 1].trim());

            for (int i = 0; i < div.size(); i++) {
                split_string = div.get(i).text().split(" ");
                if (split_string[0].equals("股利所屬期間"))
                    break;
                if (items_position.contains(i))
                    stock.element(stock_items[count++], split_string[split_string.length - 1].trim());
            }
            return responseCompanyProfileSuccess(stock);
        } catch (IOException io) {
            return responseError(io.toString());
        }
    }

    public JSONObject getCompanyDividendPolicy() {
        try {
            InputStream URLStream = openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLStream, "UTF-8"));

            String line = null;
            String all_lines = "";

            while ((line = buffer.readLine()) != null) {
                all_lines += line;
            }

            return CompanyDividendPolicyDataParsing(all_lines);
        } catch (IOException io) {
            return responseError(io.toString());
        }
    }

    public JSONObject getStockTradeInfo(String type, Integer specific_date) {
        try {
            InputStream URLStream = openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLStream, "UTF-8"));

            String line = null;
            String all_lines = "";

            while ((line = buffer.readLine()) != null) {
                all_lines += line;
            }

            if (type.equals("1")) {
                return StockTradeInfoDaily(all_lines, specific_date);
            } else if (type.equals("2")) {
                return StockTradeInfoMonthly(all_lines, specific_date);
            } else if (type.equals("3")) {
                return StockTradeInfoYearly(all_lines, specific_date);
            }

            return responseError("get stock trade info error.");
        } catch (IOException io) {
            return responseError(io.toString());
        }
    }

    private JSONObject StockTradeInfoDaily(String all_lines, Integer specific_date) {
        try {
            HashMap<String, ArrayList<String>> stock_map = new HashMap<String, ArrayList<String>>();
            for (int i = 0; i < stock_info_items.length; i++) {
                stock_map.put(stock_info_items[i], new ArrayList<String>());
            }

            Document doc = Jsoup.parse(new String(all_lines.getBytes("UTF-8"), "UTF-8"));
            Elements trs = doc.select("tr");

            String temp[];
            int temp_ymd = 0;

            // {日期,成交股數,成交金額,開盤價,最高價,最低價,收盤價,漲跌價差,成交筆數} trs資料第一行是中文標題欄位，故從i=2開始取得內文資料做處理。
            for (int i = 2; i < trs.size(); i++) {
                Elements tds = trs.get(i).select("td");

                // <td>111/07/01</td> 先處理日期字串中可能有異常空白問題"111
                // /07/01"，再以"/"分割年月日後後重新計算成西元年月日"20220701"。
                if (tds.size() != 9)
                    continue;

                temp_ymd = 19110000;
                temp = tds.get(0).text().replaceAll(" ", "").split("/");
                temp_ymd = temp_ymd + Integer.parseInt(temp[0]) * 10000 + Integer.parseInt(temp[1]) * 100
                        + Integer.parseInt(temp[2]);

                if (!(temp_ymd == specific_date))
                    continue;

                stock_map.get("date").add(String.valueOf(temp_ymd));
                stock_map.get("number").add(tds.get(1).text());
                stock_map.get("amount").add(tds.get(2).text());
                stock_map.get("openning").add(tds.get(3).text());
                stock_map.get("highest").add(tds.get(4).text());
                stock_map.get("lowest").add(tds.get(5).text());
                stock_map.get("closing").add(tds.get(6).text());
                stock_map.get("tradeVolume").add(tds.get(8).text());
                stock_map.get("turnoverRate").add("");
                stock_map.get("average").add("");

                return responseStockTradeInfoSuccess(stock_map);
            }
            return responseError("查無符合資料");
        } catch (IOException io) {
            return responseError(io.toString());
        }
    }

    private JSONObject StockTradeInfoMonthly(String all_lines, Integer specific_month) {
        try {
            HashMap<String, ArrayList<String>> stock_map = new HashMap<String, ArrayList<String>>();
            for (int i = 0; i < stock_info_items.length; i++) {
                stock_map.put(stock_info_items[i], new ArrayList<String>());
            }

            Document doc = Jsoup.parse(new String(all_lines.getBytes("UTF-8"), "UTF-8"));
            Elements trs = doc.select("tr");

            int temp_ymd = 0;
            int specific_yyyymm = Integer.parseInt(specific_month.toString().substring(0, 6));

            // {年度,月份,最高價,最低價,加權(A/B)平均價,成交筆數,成交金額(A),成交股數(B),週轉率(%)}
            for (int i = trs.size() - 1; i > 1; i--) {
                Elements tds = trs.get(i).select("td");

                if (tds.size() != 9)
                    continue;

                temp_ymd = (1911 + Integer.parseInt(tds.get(0).text().trim())) * 100
                        + Integer.parseInt(tds.get(1).text().trim());

                if (!(temp_ymd == specific_yyyymm))
                    continue;

                stock_map.get("date").add(String.valueOf(temp_ymd));
                stock_map.get("highest").add(tds.get(2).text());
                stock_map.get("lowest").add(tds.get(3).text());
                stock_map.get("average").add(tds.get(4).text());
                stock_map.get("tradeVolume").add(tds.get(5).text());
                stock_map.get("amount").add(tds.get(6).text());
                stock_map.get("number").add(tds.get(7).text());
                stock_map.get("turnoverRate").add(tds.get(8).text());
                stock_map.get("closing").add("");
                stock_map.get("openning").add("");

                return responseStockTradeInfoSuccess(stock_map);
            }
            return responseError("查無符合資料");
        } catch (IOException io) {
            return responseError(io.toString());
        }
    }

    private JSONObject StockTradeInfoYearly(String all_lines, Integer specific_year) {
        try {
            HashMap<String, ArrayList<String>> stock_map = new HashMap<String, ArrayList<String>>();
            for (int i = 0; i < stock_info_items.length; i++) {
                stock_map.put(stock_info_items[i], new ArrayList<String>());
            }

            Document doc = Jsoup.parse(new String(all_lines.getBytes("UTF-8"), "UTF-8"));
            Elements trs = doc.select("tr");

            int temp_ymd = 0;
            int specific_yyyy = Integer.parseInt(specific_year.toString().substring(0, 4));

            // {年度,成交股數,成交金額,成交筆數,最高價,日期,最低價,日期,收盤平均價}
            for (int i = trs.size() - 1; i > 1; i--) {
                Elements tds = trs.get(i).select("td");

                if (tds.size() != 9)
                    continue;

                temp_ymd = 1911 + Integer.parseInt(tds.get(0).text().trim());

                if (!(temp_ymd == specific_yyyy))
                    continue;

                stock_map.get("date").add(String.valueOf(temp_ymd));
                stock_map.get("number").add(tds.get(1).text());
                stock_map.get("amount").add(tds.get(2).text());
                stock_map.get("openning").add("");
                stock_map.get("highest").add(tds.get(4).text());
                stock_map.get("lowest").add(tds.get(6).text());
                stock_map.get("closing").add("");
                stock_map.get("tradeVolume").add(tds.get(3).text());
                stock_map.get("turnoverRate").add("");
                stock_map.get("average").add(tds.get(8).text());

                return responseStockTradeInfoSuccess(stock_map);
            }
            return responseError("查無符合資料");
        } catch (IOException io) {
            return responseError(io.toString());
        }
    }

    private JSONObject CompanyDividendPolicyDataParsing(String all_lines) {
        try {
            HashMap<String, ArrayList<String>> stock_map = new HashMap<String, ArrayList<String>>();
            for (int i = 0; i < stock_index_items.length; i++) {
                stock_map.put(stock_index_items[i], new ArrayList<String>());
            }

            // 只需要取得股利政策的table底下的<li class="List(n)">
            Document doc = Jsoup.parse(new String(all_lines.getBytes("UTF-8"), "UTF-8"));
            Elements li_lists = doc.select("div.table-body-wrapper").get(0).select("li");

            // html div element format ={股利所屬期間,現金股利,股票股利,除息日,除權日,現金股利發放日,股票股利發放,填息天數,股利合計}
            for (int i = 0; i < li_lists.size(); i++) {
                // 要取得的八個div element外面包了一層div、第一個element "股利所屬期間" 另外又包了一層div，故加起來有10個div。
                Elements tds = li_lists.get(i).select("div").get(0).select("div");

                if (tds.size() != 10)
                    continue;

                for (int j = 2; j < tds.size(); j++) {
                    stock_map.get(stock_index_items[j - 2]).add(tds.get(j).text());
                }
            }

            return responseCompanyDividendPolicySuccess(stock_map);
        } catch (IOException io) {
            return responseError(io.toString());
        }
    }

    /**
     * 建立ssl憑證
     * 
     * @param urlObj
     * @return
     */
    private TrustManager createTrustManager(URL urlObj) {
        System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
        System.setProperty("javax.net.ssl.trustStore", "keystore");
        TrustManager trust = new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {
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

    private JSONObject responseSuccess(JSONArray json_array_data) {
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();

        status_code.put("status", "success");
        status_code.put("desc", "");

        result.put("metadata", status_code);
        result.put("data", json_array_data);

        return result;
    }

    private JSONObject responseCompanyProfileSuccess(JSONObject data) {
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();

        status_code.put("status", "success");
        status_code.put("desc", "");

        result.put("metadata", status_code);
        result.put("data", data);
        return result;
    }

    private JSONObject responseCompanyDividendPolicySuccess(HashMap<String, ArrayList<String>> stock_map) {
        JSONArray allstockArray = new JSONArray();
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();
        // make up dividend days 指的是填息天數。
        String[] request_key = { "dividend_period", "cash_dividend(dollors)", "stock_dividend(shares)",
                "EX-dividend_date", "EX-right_date", "dividend_payment_date", "right_payment_date",
                "make_up_dividend_days" };

        for (int i = 0; i < stock_map.get(stock_index_items[0]).size(); i++) {
            JSONObject tempstock = new JSONObject();
            for (int j = 0; j < request_key.length; j++) {
                tempstock.element(request_key[j], stock_map.get(stock_index_items[j]).get(i));
            }
            allstockArray.add(tempstock);
        }
        data.put("stockdata", allstockArray);

        status_code.put("status", "success");
        status_code.put("desc", "");

        result.put("metadata", status_code);
        result.put("data", data);
        return result;
    }

    private JSONObject responseStockTradeInfoSuccess(HashMap<String, ArrayList<String>> stock_map) {
        JSONArray allstockArray = new JSONArray();
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();

        for (int i = 0; i < stock_map.get("date").size(); i++) {
            JSONObject tempstock = new JSONObject();
            tempstock.element("share_number(B)", stock_map.get("number").get(i));
            tempstock.element("share_amount(A)", stock_map.get("amount").get(i));
            tempstock.element("trade_volume", stock_map.get("tradeVolume").get(i));
            tempstock.element("openning_price", stock_map.get("openning").get(i));
            tempstock.element("hightest_price", stock_map.get("highest").get(i));
            tempstock.element("lowest_price", stock_map.get("lowest").get(i));
            tempstock.element("closing_price(average)", stock_map.get("closing").get(i));
            tempstock.element("the_average_of_ShareAmount(A)_and_ShareNumber(B)", stock_map.get("average").get(i));
            tempstock.element("turnover_rate(%)", stock_map.get("turnoverRate").get(i));

            allstockArray.add(tempstock);
        }
        data.put("stockdata", allstockArray);

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

        data.put("data", "");

        status_code.put("status", "error");
        status_code.put("desc", error_msg);

        result.put("metadata", status_code);
        result.put("data", data);
        return result;
    }
}
