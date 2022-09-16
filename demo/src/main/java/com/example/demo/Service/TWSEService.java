package com.example.demo.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.data.redis.core.StringRedisTemplate;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TWSEService {
    private StringRedisTemplate stringRedisTemplate;

    private String stockUrl;
    String[] stock_info_items = { "date", "number", "amount", "openning", "highest", "lowest", "closing", "tradeVolume",
            "average", "turnoverRate" };

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

    public JSONObject getCompanyList(int type) {
        try {
            String get_company_list_redis_key = (type == 1) ? "listed_company_list" : "OTC_company_list";
            int redis_ttl = 86400; // redis存活一天

            String company_list_string = this.stringRedisTemplate.opsForValue().get(get_company_list_redis_key);
            if (company_list_string != null) {
                return ResponseService.responseJSONArraySuccess(JSONArray.fromObject(company_list_string));
            }

            JSONArray company_list = new JSONArray();
            String[] company_data;
            // get 普通股票 and ETF list.
            ArrayList<String> title_string = new ArrayList<>();
            title_string.add("股票");
            title_string.add("ETF");

            InputStream URLstream = openURL(this.stockUrl);
            //Big5-HKSCS為BIG5之上擴展的字元集標準，含繁中難字。
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "Big5-HKSCS"));
            String line = null;
            String alllines = "";
            while ((line = buffer.readLine()) != null) {
                alllines += line;
            }

            Document doc = Jsoup.parse(new String(alllines.getBytes("UTF-8"), "UTF-8"));
            Elements trs = doc.select("tr");

            boolean find_titles = false;
            for (int i = 0; i < trs.size(); i++) {
                Elements tds = trs.get(i).select("td");
                //tds.size!=7 為title
                if (tds.size() != 7) {
                    find_titles = false;
                    //若沒取到任何指定列表資訊就繼續找。
                    if(title_string.contains(tds.text().trim())) 
                        find_titles = true; 
                    
                    continue;
                }
                //如果還沒找到任何指定title就略過
                if(find_titles == false) 
                    continue;

                JSONObject company = new JSONObject();
                // <td bgcolor="#FAFAD2">1101 台泥</td>
                company_data = tds.get(0).text().split("　");
                // get stock company 
                company.put("ID", company_data[0].trim());
                company.put("Name", company_data[1].trim());
                // <td bgcolor="#FAFAD2">1962/02/09</td>
                company.put("上市/上櫃日期", tds.get(2).text());
                // <td bgcolor="#FAFAD2">水泥工業</td>
                company.put("產業別", tds.get(4).text());

                company_list.add(company);
            }

            this.stringRedisTemplate.opsForValue().setIfAbsent(get_company_list_redis_key,
                    company_list.toString(), redis_ttl, TimeUnit.SECONDS);

            return ResponseService.responseJSONArraySuccess(company_list);
        } catch (IOException io) {
            return ResponseService.responseError("error", io.toString());
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
            return ResponseService.responseSuccess(stock);
        } catch (IOException io) {
            return ResponseService.responseError("error", io.toString());
        }
    }

    public JSONObject getCompanyDividendPolicy(String stock_id) {
        try {
            String get_company_dividend_redis_key = "company_history_dividend_policy:" + stock_id;
            int redis_ttl = 86400; // redis存活1天

            String company_dividend_string = this.stringRedisTemplate.opsForValue().get(get_company_dividend_redis_key);
            if (company_dividend_string != null) {
                return ResponseService.responseJSONArraySuccess(JSONArray.fromObject(company_dividend_string));
            }

            // make up dividend days 指的是填息天數。
            String[] stock_dividend_items = { "dividend_period", "cash_dividend(dollors)", "stock_dividend(shares)",
                    "EX-dividend_date", "EX-right_date", "dividend_payment_date", "right_payment_date",
                    "make_up_dividend_days" };
            JSONArray dividend_info_array = new JSONArray();

            InputStream URLStream = openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLStream, "UTF-8"));

            String line = null;
            String all_lines = "";

            while ((line = buffer.readLine()) != null) {
                all_lines += line;
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

                JSONObject stock_ifo = new JSONObject();
                for (int j = 2; j < tds.size(); j++) {
                    stock_ifo.element(stock_dividend_items[j - 2], tds.get(j).text());
                }
                dividend_info_array.add(stock_ifo);
            }

            this.stringRedisTemplate.opsForValue().setIfAbsent(get_company_dividend_redis_key,
                    dividend_info_array.toString(), redis_ttl, TimeUnit.SECONDS);

            return ResponseService.responseJSONArraySuccess(dividend_info_array);
        } catch (IOException io) {
            return ResponseService.responseError("error", io.toString());
        }
    }

    public JSONObject getListedStockTradeInfo(String type, Integer specific_date, String stock_id) {
        try {
            InputStream URLStream = openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLStream, "UTF-8"));

            String line = null;
            String all_lines = "";

            while ((line = buffer.readLine()) != null) {
                all_lines += line;
            }

            if (type.equals("1")) {
                return listedStockTradeInfoDaily(all_lines, specific_date, stock_id);
            } else if (type.equals("2")) {
                return listedStockTradeInfoMonthly(all_lines, specific_date, stock_id);
            } else if (type.equals("3")) {
                return listedStockTradeInfoYearly(all_lines, specific_date, stock_id);
            }

            return ResponseService.responseError("error", "get stock trade info error.");
        } catch (IOException io) {
            return ResponseService.responseError("error", io.toString());
        }
    }

    private JSONObject listedStockTradeInfoDaily(String all_lines, Integer specific_date, String stock_id) {
        try {
            JSONArray trade_info_array = new JSONArray();
            int redis_ttl = 86400; // redis存活1天

            String trade_info_redis_key = "stock_trade_info_daily_" + specific_date + ":"
                    + stock_id;

            String stock_info_string = this.stringRedisTemplate.opsForValue().get(trade_info_redis_key);
            if (stock_info_string != null) {
                return ResponseService.responseJSONArraySuccess(JSONArray.fromObject(stock_info_string));
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

                JSONObject trade_info = new JSONObject();
                trade_info.element("share_number(B)", tds.get(1).text());
                trade_info.element("share_amount(A)", tds.get(2).text());
                trade_info.element("trade_volume", tds.get(8).text());
                trade_info.element("openning_price", tds.get(3).text());
                trade_info.element("hightest_price", tds.get(4).text());
                trade_info.element("lowest_price", tds.get(5).text());
                trade_info.element("closing_price(average)", tds.get(6).text());
                trade_info.element("the_average_of_ShareAmount(A)_and_ShareNumber(B)", "");
                trade_info.element("turnover_rate(%)", "");
                trade_info_array.add(trade_info);

                this.stringRedisTemplate.opsForValue().setIfAbsent(trade_info_redis_key,
                        trade_info_array.toString(), redis_ttl, TimeUnit.SECONDS);

                return ResponseService.responseJSONArraySuccess(trade_info_array);
            }
            return ResponseService.responseError("error", "查無符合資料");
        } catch (IOException io) {
            return ResponseService.responseError("error", io.toString());
        }
    }

    private JSONObject listedStockTradeInfoMonthly(String all_lines, Integer specific_date, String stock_id) {
        try {
            JSONArray trade_info_array = new JSONArray();
            int redis_ttl = 86400; // redis存活1天

            String trade_info_redis_key = "stock_trade_info_monthly_" + specific_date.toString().substring(0, 6) + ":"
                    + stock_id;

            String stock_info_string = this.stringRedisTemplate.opsForValue().get(trade_info_redis_key);
            if (stock_info_string != null) {
                return ResponseService.responseJSONArraySuccess(JSONArray.fromObject(stock_info_string));
            }

            Document doc = Jsoup.parse(new String(all_lines.getBytes("UTF-8"), "UTF-8"));
            Elements trs = doc.select("tr");

            int temp_ymd = 0;
            int specific_yyyymm = Integer.parseInt(specific_date.toString().substring(0, 6));

            // {年度,月份,最高價,最低價,加權(A/B)平均價,成交筆數,成交金額(A),成交股數(B),週轉率(%)}
            for (int i = trs.size() - 1; i > 1; i--) {
                Elements tds = trs.get(i).select("td");

                if (tds.size() != 9)
                    continue;

                temp_ymd = (1911 + Integer.parseInt(tds.get(0).text().trim())) * 100
                        + Integer.parseInt(tds.get(1).text().trim());

                if (!(temp_ymd == specific_yyyymm))
                    continue;

                JSONObject trade_info = new JSONObject();
                trade_info.element("share_number(B)", tds.get(7).text());
                trade_info.element("share_amount(A)", tds.get(6).text());
                trade_info.element("trade_volume", tds.get(5).text());
                trade_info.element("openning_price", "");
                trade_info.element("hightest_price", tds.get(2).text());
                trade_info.element("lowest_price", tds.get(3).text());
                trade_info.element("closing_price(average)", "");
                trade_info.element("the_average_of_ShareAmount(A)_and_ShareNumber(B)", tds.get(4).text());
                trade_info.element("turnover_rate(%)", tds.get(8).text());
                trade_info_array.add(trade_info);

                this.stringRedisTemplate.opsForValue().setIfAbsent(trade_info_redis_key,
                        trade_info_array.toString(), redis_ttl, TimeUnit.SECONDS);

                return ResponseService.responseJSONArraySuccess(trade_info_array);
            }
            return ResponseService.responseError("error", "查無符合資料");
        } catch (IOException io) {
            return ResponseService.responseError("error", io.toString());
        }
    }

    private JSONObject listedStockTradeInfoYearly(String all_lines, Integer specific_date, String stock_id) {
        try {
            JSONArray trade_info_array = new JSONArray();
            int redis_ttl = 86400 * 30; // redis存活30天

            String trade_info_redis_key = "stock_trade_info_yearly_" + specific_date.toString().substring(0, 4) + ":"
                    + stock_id;

            String stock_info_string = this.stringRedisTemplate.opsForValue().get(trade_info_redis_key);
            if (stock_info_string != null) {
                return ResponseService.responseJSONArraySuccess(JSONArray.fromObject(stock_info_string));
            }

            Document doc = Jsoup.parse(new String(all_lines.getBytes("UTF-8"), "UTF-8"));
            Elements trs = doc.select("tr");

            int temp_ymd = 0;
            int specific_yyyy = Integer.parseInt(specific_date.toString().substring(0, 4));

            // {年度,成交股數,成交金額,成交筆數,最高價,日期,最低價,日期,收盤平均價}
            for (int i = trs.size() - 1; i > 1; i--) {
                Elements tds = trs.get(i).select("td");

                if (tds.size() != 9)
                    continue;

                temp_ymd = 1911 + Integer.parseInt(tds.get(0).text().trim());
                if (!(temp_ymd == specific_yyyy))
                    continue;

                JSONObject trade_info = new JSONObject();
                trade_info.element("share_number(B)", tds.get(1).text());
                trade_info.element("share_amount(A)", tds.get(2).text());
                trade_info.element("trade_volume", tds.get(3).text());
                trade_info.element("openning_price", "");
                trade_info.element("hightest_price", tds.get(4).text());
                trade_info.element("lowest_price", tds.get(6).text());
                trade_info.element("closing_price(average)", "");
                trade_info.element("the_average_of_ShareAmount(A)_and_ShareNumber(B)",
                        tds.get(8).text());
                trade_info.element("turnover_rate(%)", "");

                trade_info_array.add(trade_info);

                this.stringRedisTemplate.opsForValue().setIfAbsent(trade_info_redis_key,
                        trade_info_array.toString(), redis_ttl, TimeUnit.SECONDS);

                return ResponseService.responseJSONArraySuccess(trade_info_array);
            }
            return ResponseService.responseError("error", "查無符合資料");
        } catch (IOException io) {
            return ResponseService.responseError("error", io.toString());
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
}
