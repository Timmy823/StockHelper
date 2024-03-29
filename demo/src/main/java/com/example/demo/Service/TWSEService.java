package com.example.demo.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.redis.core.StringRedisTemplate;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TWSEService {
    private StringRedisTemplate stringRedisTemplate;

    private String stockUrl;

    private ArrayList<String> Q1 = new ArrayList<String>(Arrays.asList("01", "02", "03"));
    private ArrayList<String> Q2 = new ArrayList<String>(Arrays.asList("04", "05", "06"));
    private ArrayList<String> Q3 = new ArrayList<String>(Arrays.asList("07", "08", "09"));
    private ArrayList<String> Q4 = new ArrayList<String>(Arrays.asList("10", "11", "12"));

    public TWSEService(String stockUrl, StringRedisTemplate stringRedisTemplate) throws IOException {
        this.stockUrl = stockUrl;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public JSONObject getLastDailyTopStockTradingVolume() {
        int max_stocks = 5;
        String top_trading_volume_listed_stock_url = "https://www.twse.com.tw/exchangeReport/MI_INDEX20?response=json";
        String top_trading_volume_OTC_stock_url = "https://www.tpex.org.tw/web/stock/aftertrading/trading_volume/vol_rank_result.php?l=zh-tw&t=D";
        String OTC_stock_trade_info_url = "https://www.tpex.org.tw/web/stock/aftertrading/daily_trading_info/st43_result.php?l=zh-tw&stkno=";
        String top_trading_volume_ETF_url = "https://tw.stock.yahoo.com/_td-stock/api/resource/StockServices.etfRanking;limit="
                + max_stocks + ";offset=0;rankId=volume?region=TW&site=finance&tz=Asia/Taipei6";

        String[] top_stock_string = { "ranking", "stock_id", "stock_name", "shares_amount", "closing_price" };
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date system_date = new Date();
            String stock_volume_redis_key = "Last_daily_top_stop_trading_volume:" + sdf.format(system_date);
            int redis_ttl = 86400; // redis存活1天

            String stock_volume_string = this.stringRedisTemplate.opsForValue().get(stock_volume_redis_key);
            if (stock_volume_string != null) {
                return ResponseService.responseJSONArraySuccess(JSONArray.fromObject(stock_volume_string));
            }

            // set response URL
            JSONObject stock_type = new JSONObject();
            JSONObject top_stock_info = new JSONObject();
            // get url json
            JSONArray listed_volume_array = new JSONArray();
            JSONArray OTC_volume_array = new JSONArray();
            JSONArray ETF_volume_array = new JSONArray();
            JSONArray stock_array_info = new JSONArray();
            JSONObject stock_item_info = new JSONObject();

            HashMap<String, ArrayList<String>> OTC_stock = new HashMap<String, ArrayList<String>>();
            for (int i = 0; i < max_stocks; i++) {
                OTC_stock.put(top_stock_string[i], new ArrayList<String>());
            }

            // connet to listed stock top trading volumn URL
            HttpsService open_url = new HttpsService();
            InputStream URLstream = open_url.openURL(top_trading_volume_listed_stock_url);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "UTF-8"));
            String line = null;
            String alllines = "";
            while ((line = buffer.readLine()) != null) {
                alllines += line;
            }
            JSONArray listed_stock_array = JSONObject.fromObject(alllines).getJSONArray("data");

            // connet to OTC stock top trading volume URL
            URLstream = open_url.openURL(top_trading_volume_OTC_stock_url);
            buffer = new BufferedReader(new InputStreamReader(URLstream, "BIG5"));
            line = null;
            alllines = "";
            while ((line = buffer.readLine()) != null) {
                alllines += line;
            }
            JSONObject readLineObject = JSONObject.fromObject(alllines);
            JSONArray OTC_stock_array = readLineObject.getJSONArray("aaData");
            String reportDate = readLineObject.getString("reportDate");

            // connet to ETF top trading volume URL
            URLstream = open_url.openURL(top_trading_volume_ETF_url);
            buffer = new BufferedReader(new InputStreamReader(URLstream, "UTF-8"));
            line = null;
            alllines = "";
            while ((line = buffer.readLine()) != null) {
                alllines += line;
            }
            JSONArray ETF_array = JSONObject.fromObject(alllines).getJSONArray("list");

            // get listed item info
            for (int i = 0, count = 1; i < listed_stock_array.size() & count <= max_stocks; i++) {
                stock_array_info = listed_stock_array.getJSONArray(i);

                // get listed stock , not including ETF
                if (stock_array_info.get(1).toString().length() != 4)
                    continue;

                top_stock_info = new JSONObject();
                top_stock_info.put(top_stock_string[0], String.valueOf(count++));
                top_stock_info.put(top_stock_string[1], stock_array_info.get(1));
                top_stock_info.put(top_stock_string[2], stock_array_info.get(2));
                top_stock_info.put(top_stock_string[3], stock_array_info.get(3).toString().replace(",", ""));
                top_stock_info.put(top_stock_string[4], stock_array_info.get(8));
                listed_volume_array.add(top_stock_info);
            }

            // get ETF info
            for (int i = 0, count = 1; i < ETF_array.size() & count <= max_stocks; i++) {
                stock_item_info = ETF_array.getJSONObject(i);

                top_stock_info = new JSONObject();
                top_stock_info.put(top_stock_string[0], String.valueOf(count++));
                top_stock_info.put(top_stock_string[1], stock_item_info.getString("symbol").replace(".tw", ""));
                top_stock_info.put(top_stock_string[2], stock_item_info.getString("symbolName"));
                top_stock_info.put(top_stock_string[3], String.valueOf(stock_item_info.getInt("volumeK") * 1000));
                top_stock_info.put(top_stock_string[4], stock_item_info.get("price"));
                ETF_volume_array.add(top_stock_info);
            }

            // get otc info
            for (int i = 0, count = 1; i < OTC_stock_array.size() & count <= max_stocks; i++) {
                stock_array_info = OTC_stock_array.getJSONArray(i);

                // get OTC stock , not including ETF
                if (stock_array_info.get(1).toString().length() != 4)
                    continue;

                OTC_stock.get(top_stock_string[0]).add(String.valueOf(count++));
                OTC_stock.get(top_stock_string[1]).add(stock_array_info.get(1).toString());
                OTC_stock.get(top_stock_string[2]).add(stock_array_info.get(2).toString());
            }

            // get OTC stock top trading volume info
            for (int i = 0; i < max_stocks; i++) {
                URLstream = open_url.openURL(OTC_stock_trade_info_url + OTC_stock.get(top_stock_string[1]).get(i));
                buffer = new BufferedReader(new InputStreamReader(URLstream, "BIG5"));
                line = null;
                alllines = "";
                while ((line = buffer.readLine()) != null) {
                    alllines += line;
                }
                OTC_stock_array = JSONObject.fromObject(alllines).getJSONArray("aaData");
                // get OTC stock closing price and shares amount.
                for (int j = OTC_stock_array.size() - 1; j >= 0; j--) {
                    stock_array_info = OTC_stock_array.getJSONArray(j);
                    if (!stock_array_info.get(0).equals(reportDate))
                        continue;

                    OTC_stock.get(top_stock_string[3]).add(String
                            .valueOf(Integer.parseInt(stock_array_info.get(1).toString().replace(",", "")) * 1000));
                    OTC_stock.get(top_stock_string[4]).add(stock_array_info.get(6).toString());
                    break;
                }
            }

            // put OTC stock info into OTC valume array
            for (int i = 0; i < max_stocks; i++) {
                top_stock_info = new JSONObject();
                for (int j = 0; j < top_stock_string.length; j++) {
                    top_stock_info.put(top_stock_string[j], OTC_stock.get(top_stock_string[j]).get(i).toString());
                }
                OTC_volume_array.add(top_stock_info);
            }

            // put ETF, listed and OTC stock info into stock_type.
            stock_type.put("listed_stock", listed_volume_array);
            stock_type.put("OTC_stock", OTC_volume_array);
            stock_type.put("ETF", ETF_volume_array);

            this.stringRedisTemplate.opsForValue().setIfAbsent(stock_volume_redis_key,
                    stock_type.toString(), redis_ttl, TimeUnit.SECONDS);

            return ResponseService.responseSuccess(stock_type);
        } catch (IOException io) {
            return ResponseService.responseError("error", io.toString());
        }
    }

    public JSONObject getStockEps(String stock_id) {
        try {
            // check redis
            String stock_eps_redis_key = "recent_five_years_eps:" + stock_id;
            int redis_ttl = 86400; // redis存活1天

            String eps_string = this.stringRedisTemplate.opsForValue().get(stock_eps_redis_key);
            if (eps_string != null) {
                return ResponseService.responseJSONArraySuccess(JSONArray.fromObject(eps_string));
            }

            JSONArray eps_array = new JSONArray();
            JSONObject revenue_item = new JSONObject();
            String[] belong_date;
            String belong_season = "";
            String revenue_item_string;

            // https connection
            HttpsService open_url = new HttpsService();
            InputStream URLstream = open_url.openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "UTF-8"));
            String line = null;
            String alllines = "";
            while ((line = buffer.readLine()) != null) {
                alllines += line;
            }

            JSONArray revenues = JSONObject.fromObject(alllines)
                    .getJSONObject("data").getJSONObject("result").getJSONArray("revenues");

            for (int i = 0; i < revenues.size(); i++) {
                JSONObject eps_item = new JSONObject();
                revenue_item = revenues.getJSONObject(i);
                // 處理日期時間 置換對應季度
                belong_date = revenue_item.getString("date").split("-");
                if (this.Q1.contains(belong_date[1])) {
                    belong_season = "Q1";
                } else if (this.Q2.contains(belong_date[1])) {
                    belong_season = "Q2";
                } else if (this.Q3.contains(belong_date[1])) {
                    belong_season = "Q3";
                } else if (this.Q4.contains(belong_date[1])) {
                    belong_season = "Q4";
                }

                eps_item.put("year", belong_date[0]);
                eps_item.put("season", belong_season);
                eps_item.put("eps", (revenue_item_string = revenue_item.getString("eps")).equals("null") ? ""
                        : revenue_item_string);
                eps_item.put("epsQoQ", (revenue_item_string = revenue_item.getString("epsQoQ")).equals("null") ? ""
                        : revenue_item_string);
                eps_item.put("epsYoY", (revenue_item_string = revenue_item.getString("epsYoY")).equals("null") ? ""
                        : revenue_item_string);
                eps_array.add(eps_item);
            }

            this.stringRedisTemplate.opsForValue().setIfAbsent(stock_eps_redis_key,
                    eps_array.toString(), redis_ttl, TimeUnit.SECONDS);

            return ResponseService.responseJSONArraySuccess(eps_array);
        } catch (IOException io) {
            return ResponseService.responseError("error", io.toString());
        }
    }

    public JSONObject getMarginPurchaseAndShortSaleAmountDaily(String stock_id, String specified_date) {
        try {
            String redis_key = "margin_purchase_and_short_sale_amount_" + specified_date + ":" + stock_id;
            int redis_ttl = 86400; // redis存活一天

            String amount_string = this.stringRedisTemplate.opsForValue().get(redis_key);
            if (amount_string != null) {
                return ResponseService.responseJSONArraySuccess(JSONArray.fromObject(amount_string));
            }

            String[] item_string = { "buy", "sell", "repayment", "balance_yesterday", "balance", "position_limit" };

            JSONArray amount_array = new JSONArray();
            JSONObject amount_item = new JSONObject();
            JSONObject margin_purchase_item = new JSONObject();
            JSONObject short_sale_item = new JSONObject();
            // get data from url.
            JSONArray stock_item = new JSONArray();

            // https connection
            HttpsService open_url = new HttpsService();
            InputStream URLstream = open_url.openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "UTF-8"));
            String line = null;
            String alllines = "";
            while ((line = buffer.readLine()) != null) {
                alllines += line;
            }
            JSONArray data = JSONObject.fromObject(alllines).getJSONArray("data");
            for (int i = 0; i < data.size(); i++) {
                stock_item = data.getJSONArray(i);

                if (!stock_item.get(0).equals(stock_id))
                    continue;

                margin_purchase_item.put(item_string[0], stock_item.get(2));
                margin_purchase_item.put(item_string[1], stock_item.get(3));
                margin_purchase_item.put(item_string[2], stock_item.get(4));
                margin_purchase_item.put(item_string[3], stock_item.get(5));
                margin_purchase_item.put(item_string[4], stock_item.get(6));
                margin_purchase_item.put(item_string[5], stock_item.get(7));

                short_sale_item.put(item_string[0], stock_item.get(8));
                short_sale_item.put(item_string[1], stock_item.get(9));
                short_sale_item.put(item_string[2], stock_item.get(10));
                short_sale_item.put(item_string[3], stock_item.get(11));
                short_sale_item.put(item_string[4], stock_item.get(12));
                short_sale_item.put(item_string[5], stock_item.get(13));

                amount_item.put("margin_purchase", margin_purchase_item);
                amount_item.put("short_sale", short_sale_item);
                amount_item.put("offset_of_margin_purchasing_and_short_selling", stock_item.get(14));
                amount_array.add(amount_item);
                break;
            }

            this.stringRedisTemplate.opsForValue().setIfAbsent(redis_key,
                    amount_array.toString(), redis_ttl, TimeUnit.SECONDS);

            return ResponseService.responseJSONArraySuccess(amount_array);
        } catch (IOException io) {
            return ResponseService.responseError("error", io.toString());
        }
    }

    public JSONObject getExtrangeTradedFundRatio(String stock_id) {
        try {
            String get_ETF_redis_key = "ETF_ratio:" + stock_id;
            int redis_ttl = 86400; // redis存活一天

            String ETF_ratio_string = this.stringRedisTemplate.opsForValue().get(get_ETF_redis_key);
            if (ETF_ratio_string != null) {
                return ResponseService.responseSuccess(JSONObject.fromObject(ETF_ratio_string));
            }

            String[] radio_info_string = { "industry_radio", "asset_distribution", "top_10_stock_radio" };
            JSONObject etf_ratio_info = new JSONObject();
            // 為了remove 字串前綴有編號 "3.聯電" in asset_distribution and top_10_stock table.
            String name_string;

            // https connection
            HttpsService open_url = new HttpsService();
            InputStream URLstream = open_url.openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "UTF-8"));
            String line = null;
            String alllines = "";
            while ((line = buffer.readLine()) != null) {
                alllines += line;
            }

            Document doc = Jsoup.parse(new String(alllines.getBytes("UTF-8"), "UTF-8"));
            Elements divs = doc.select("div#main-2-QuoteHolding-Proxy");
            if (divs.size() == 0) {
                return ResponseService.responseError("error", "It's not ETF.");
            }
            Elements tables = divs.select("div").get(0).select("div.grid-item");

            // get industry and asset and top_10_stock table
            for (int i = 1; i < tables.size() && i <= radio_info_string.length + 1; i++) {
                Elements radio_items = tables.get(i).select("li");
                JSONArray radio_list = new JSONArray();

                for (int j = 0; j < radio_items.size(); j++) {
                    Elements columns = radio_items.get(j).select("div");

                    // 一層li中包含:名稱欄位包在兩層div裏面，資料明細欄位包在一層div裏
                    if (columns.size() != 3)
                        continue;

                    JSONObject radio_info = new JSONObject();
                    name_string = columns.get(0).text();
                    radio_info.put("name", name_string.contains(".") ? name_string.substring(3) : name_string);
                    radio_info.put("ratio", columns.get(2).text());
                    radio_list.add(radio_info);
                }
                etf_ratio_info.put(radio_info_string[i - 1], radio_list);
            }

            this.stringRedisTemplate.opsForValue().setIfAbsent(get_ETF_redis_key,
                    etf_ratio_info.toString(), redis_ttl, TimeUnit.SECONDS);

            return ResponseService.responseSuccess(etf_ratio_info);
        } catch (IOException io) {
            return ResponseService.responseError("error", io.toString());
        }
    }

    public JSONObject getCompanyProfitablityIndex(String stock_id) {
        try {
            String get_company_profit_redis_key = "company_profitablity_index :" + stock_id;
            int redis_ttl = 86400; // redis存活一天

            String company_profit_string = this.stringRedisTemplate.opsForValue().get(get_company_profit_redis_key);
            if (company_profit_string != null) {
                return ResponseService.responseJSONArraySuccess(JSONArray.fromObject(company_profit_string));
            }

            JSONArray profitablity_array = new JSONArray();
            // 年度，營業毛利，營業利益，營業損益，稅後淨利，ROE，ROA
            String[] profit_info_string = { "year", "gross_profit", "operating_profit", "operating_income",
                    "net_income_after_taxes", "ROE", "ROA" };
            // record profit_info_string index.
            int profit_string_index;
            // 營業毛利，營業利益，營業損益，稅後淨利 分成 金額及利率
            String[] profit_item_string = { "amount", "margin" };
            // column info position of html table.
            Integer[] column_info_posString = { 0, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 };

            // https connectiont
            HttpsService httpsService = new HttpsService();
            InputStream URLstream = httpsService.openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "UTF-8"));

            String line = null;
            String alllines = "";
            while ((line = buffer.readLine()) != null) {
                alllines += line;
            }

            Document doc = Jsoup.parse(new String(alllines.getBytes("UTF-8"), "UTF-8"));
            // get profit table.
            Elements table = doc.select("div#txtFinDetailData").select("table#tblDetail").select("tr[align=center]");
            // string ready to check if it contans "(", and split it.
            String item = "";

            for (Element column : table) {
                profit_string_index = 0;
                JSONObject profit_info = new JSONObject();
                Elements column_info = column.select("td");
                // put year
                profit_info.put(profit_info_string[profit_string_index++],
                        column_info.get(column_info_posString[0]).text());
                // put amount and margin of profitablity items, and profit_string_index++
                for (int i = profit_string_index; i <= 4; i++, profit_string_index++) {
                    JSONObject profit_item = new JSONObject();
                    profit_item.put(profit_item_string[0], column_info.get(column_info_posString[i]).text());
                    profit_item.put(profit_item_string[1], column_info.get(column_info_posString[i + 4]).text());

                    profit_info.put(profit_info_string[i], profit_item);
                }
                // put ROE and ROA
                item = column_info.get(column_info_posString[9]).text();
                profit_info.put(profit_info_string[profit_string_index++],
                        item.contains("(") ? item.split("[(]")[0].trim() : item);

                item = column_info.get(column_info_posString[10]).text();
                profit_info.put(profit_info_string[profit_string_index++],
                        item.contains("(") ? item.split("[(]")[0].trim() : item);

                profitablity_array.add(profit_info);
            }

            this.stringRedisTemplate.opsForValue().setIfAbsent(get_company_profit_redis_key,
                    profitablity_array.toString(), redis_ttl, TimeUnit.SECONDS);

            return ResponseService.responseJSONArraySuccess(profitablity_array);
        } catch (IOException io) {
            return ResponseService.responseError("error", io.toString());
        }
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

            // https connection
            HttpsService open_url = new HttpsService();
            InputStream URLstream = open_url.openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "BIG5"));
            String line = null;
            String alllines = "";
            while ((line = buffer.readLine()) != null) {
                alllines += line;
            }

            Document doc = Jsoup.parse(new String(alllines.getBytes("UTF-8"), "UTF-8"));
            Elements trs = doc.select("tr");

            Boolean find_titles = false;
            String current_title = "";
            for (int i = 0; i < trs.size(); i++) {
                Elements tds = trs.get(i).select("td");
                // tds.size!=7 為title
                if (tds.size() != 7) {
                    current_title = tds.text().trim().toString();
                    find_titles = title_string.contains(current_title);
                    continue;
                }
                // 如果還沒找到任何指定title就略過
                if (!find_titles)
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
                if (current_title.equals("ETF")) {
                    company.put("產業別", current_title);
                } else {
                    company.put("產業別", tds.get(4).text());
                }

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
        String[] stock_items = { "main_business", "created_date", "telephone", "stock_date", "fax", "website",
                "chairman", "email", "president", "share_capital", "share_number", "address", "market_value",
                "share_holding_radio" };
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
        items_position.add(10);
        items_position.add(11);
        items_position.add(12);
        items_position.add(14);
        items_position.add(16);
        items_position.add(17);
        items_position.add(18);
        items_position.add(20);

        try {
            // https connection
            HttpsService open_url = new HttpsService();
            InputStream URLstream = open_url.openURL(this.stockUrl);
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

            // reponse
            JSONArray dividend_info_array = new JSONArray();
            JSONObject dividend_info = new JSONObject();
            // get json
            JSONObject dividend_detail = new JSONObject();
            JSONObject detail_item;

            SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.TAIWAN);
            SimpleDateFormat outputFormatter = new SimpleDateFormat("yyyy/MM/dd");
            String dividend_detail_string;

            // https connection
            HttpsService open_url = new HttpsService();
            InputStream URLstream = open_url.openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "UTF-8"));

            String line = null;
            String all_lines = "";

            while ((line = buffer.readLine()) != null) {
                all_lines += line;
            }

            JSONArray dividend_array = JSONObject.fromObject(all_lines).getJSONArray("dividends");
            for (int i = 0; i < dividend_array.size(); i++) {
                dividend_detail = dividend_array.getJSONObject(i);

                // initail dividend info
                // make up dividend days 指的是填息天數。
                dividend_info = new JSONObject();
                dividend_info.put("year", dividend_detail.getString("year"));
                dividend_info.put("period", dividend_detail.getString("period"));
                dividend_info.put("make_up_dividend_days", "");
                dividend_info.put("cash_dividend", "0");
                dividend_info.put("EX-dividend_date", "");
                dividend_info.put("dividend_payment_date", "");
                dividend_info.put("stock_dividend", "0");
                dividend_info.put("EX-right_date", "");
                dividend_info.put("right_payment_date", "");

                // cash dividend
                if (!dividend_detail.get("exDividend").equals(null)) {
                    detail_item = dividend_detail.getJSONObject("exDividend");
                    dividend_info.put("make_up_dividend_days",
                            (dividend_detail_string = detail_item.get("recoveryDays").toString()).equals("null") ? ""
                                    : dividend_detail_string);
                    dividend_info.put("cash_dividend",
                            (dividend_detail_string = detail_item.getString("cash")).equals("-") ? "0"
                                    : detail_item.getString("cash"));
                    dividend_info.put("EX-dividend_date",
                            (dividend_detail_string = detail_item.get("date").toString()).equals("null") ? ""
                                    : outputFormatter.format(inputFormatter.parse(dividend_detail_string)));
                    dividend_info.put("dividend_payment_date",
                            (dividend_detail_string = detail_item.get("cashPayDate").toString()).equals("null") ? ""
                                    : outputFormatter.format(inputFormatter.parse(dividend_detail_string)));
                }
                // right dividend
                if (!dividend_detail.get("exRight").equals(null)) {
                    detail_item = dividend_detail.getJSONObject("exRight");
                    dividend_info.put("stock_dividend",
                            (dividend_detail_string = detail_item.getString("stock")).equals("-") ? "0"
                                    : detail_item.getString("stock"));
                    dividend_info.put("EX-right_date",
                            (dividend_detail_string = detail_item.get("date").toString()).equals("null") ? ""
                                    : outputFormatter.format(inputFormatter.parse(dividend_detail_string)));
                    dividend_info.put("right_payment_date",
                            (dividend_detail_string = detail_item.get("stockPayDate").toString()).equals("null") ? ""
                                    : outputFormatter.format(inputFormatter.parse(dividend_detail_string)));
                }
                dividend_info_array.add(dividend_info);
            }

            this.stringRedisTemplate.opsForValue().setIfAbsent(get_company_dividend_redis_key,
                    dividend_info_array.toString(), redis_ttl, TimeUnit.SECONDS);

            return ResponseService.responseJSONArraySuccess(dividend_info_array);
        } catch (IOException io) {
            return ResponseService.responseError("error", io.toString());
        } catch (ParseException io) {
            return ResponseService.responseError("error", io.toString());
        }
    }

    public JSONObject getListedStockTradeInfo(String type, Integer specific_date, String stock_id) {
        try {
            // https connection
            HttpsService open_url = new HttpsService();
            InputStream URLstream = open_url.openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "UTF-8"));

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

    public JSONObject getCompanyMonthlyRevenue(String stock_id) {
        try {
            // check redis
            String monthly_revenue_redis_key = "monthly_revenue:" + stock_id;
            int redis_ttl = 86400; // redis存活1天

            String monthly_revenue_string = this.stringRedisTemplate.opsForValue().get(monthly_revenue_redis_key);
            if (monthly_revenue_string != null) {
                return ResponseService.responseJSONArraySuccess(JSONArray.fromObject(monthly_revenue_string));
            }

            // get every revenue data frome URL's revenues array.
            JSONObject monthly_revenue_item = new JSONObject();
            // it is response array.
            JSONArray revenue_array = new JSONArray();
            // response info put into revenue_array
            JSONObject revenue_info = new JSONObject();
            // revene data belong date "2022-08-01T00:00:00+08:00"
            String[] belong_date;
            // revene_info key string
            String[] revenue_string = { "year", "month", "revenue", "cumulative_revenue",
                    "MoM", "YoY", "cumulative_YoY",
                    "revenue_in_same_monthly_last_year", "cumulative_revenue_last_year" };

            // https connection
            HttpsService open_url = new HttpsService();
            InputStream URLstream = open_url.openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "UTF-8"));
            String line = null;
            String alllines = "";
            while ((line = buffer.readLine()) != null) {
                alllines += line;
            }
            JSONArray revenues = JSONObject.fromObject(alllines)
                    .getJSONObject("data").getJSONObject("result").getJSONArray("revenues");

            for (int i = 0; i < revenues.size(); i++) {
                monthly_revenue_item = revenues.getJSONObject(i);
                revenue_info = new JSONObject();
                // get revenue belong year and month
                belong_date = monthly_revenue_item.getString("date").split("-");

                revenue_info.put(revenue_string[0], belong_date[0]);
                revenue_info.put(revenue_string[1], belong_date[1]);
                revenue_info.put(revenue_string[2], monthly_revenue_item.getString("revenue"));
                revenue_info.put(revenue_string[3], monthly_revenue_item.getString("revenueAcc"));
                revenue_info.put(revenue_string[4], monthly_revenue_item.getString("revenueMoM"));
                revenue_info.put(revenue_string[5], monthly_revenue_item.getString("revenueYoY"));
                revenue_info.put(revenue_string[6], monthly_revenue_item.getString("revenueYoYAcc"));
                revenue_info.put(revenue_string[7],
                        monthly_revenue_item.getJSONObject("lastYear").getString("revenue"));
                revenue_info.put(revenue_string[8],
                        monthly_revenue_item.getJSONObject("lastYear").getString("revenueYoYAcc"));
                revenue_array.add(revenue_info);
            }

            this.stringRedisTemplate.opsForValue().setIfAbsent(monthly_revenue_redis_key,
                    revenue_array.toString(), redis_ttl, TimeUnit.SECONDS);

            return ResponseService.responseJSONArraySuccess(revenue_array);
        } catch (Exception io) {
            return ResponseService.responseError("error", io.toString());
        }
    }

    public JSONObject getStockTradeInfo(String stock_id) {
        JSONArray trade_info_array = new JSONArray();
        // yahoo各欄位mapping
        String[] stock_item_string = { "t", "o", "h", "l", "c", "v" };
        String[] trade_info_string = { "date", "openning_price", "hightest_price", "lowest_price", "closing_price",
                "volum" };

        try {
            // check redis
            String stock_trade_redis_key = "stock_trade_info_recent_half_year:" + stock_id;
            int redis_ttl = 86400; // redis存活1天

            String stock_info_string = this.stringRedisTemplate.opsForValue().get(stock_trade_redis_key);
            if (stock_info_string != null) {
                return ResponseService.responseJSONArraySuccess(JSONArray.fromObject(stock_info_string));
            }

            // https connection
            HttpsService open_url = new HttpsService();
            InputStream URLstream = open_url.openURL(this.stockUrl);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "UTF-8"));

            String line = null;
            String all_lines = "";

            while ((line = buffer.readLine()) != null) {
                all_lines += line;
            }

            JSONArray stock_info = JSONObject.fromObject(all_lines.replace("null(", "").replace(");", ""))
                    .getJSONArray("ta");
            for (int i = 0; i < stock_info.size(); i++) {
                JSONObject stock_item = stock_info.getJSONObject(i);
                JSONObject trade_info = new JSONObject();
                for (int j = 0; j < stock_item_string.length; j++) {
                    trade_info.put(trade_info_string[j], stock_item.getString(stock_item_string[j]));
                }
                trade_info_array.add(trade_info);
            }

            this.stringRedisTemplate.opsForValue().setIfAbsent(stock_trade_redis_key,
                    trade_info_array.toString(), redis_ttl, TimeUnit.SECONDS);

            return ResponseService.responseJSONArraySuccess(trade_info_array);
        } catch (IOException io) {
            return ResponseService.responseError("error", io.toString());
        }
    }
}
