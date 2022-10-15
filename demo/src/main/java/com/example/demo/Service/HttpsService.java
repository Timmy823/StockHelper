package com.example.demo.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsService {
    public InputStream openURL(String urlPath) throws Exception {
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
        url_connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        url_connection.setRequestProperty("Content-Length", Integer.toString(1000));
        url_connection.setRequestProperty("connection", "Keep-Alive");
        
        System.out.println("ready to connect!");
        url_connection.connect();

        // the method is used to access the header filed after the connection
        if (url_connection.getResponseCode() != 200) {
            System.out.print("\nConnection Fail:" + url_connection.getResponseCode());
            throw new Exception("connect error " + url_connection.getResponseCode());
        }
        return url_connection.getInputStream();
    }

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

    public String getURLBufferString(String url) throws Exception {
        //connect https url
        InputStream URLstream = openURL(url);
        //get url info
        BufferedReader buffer = new BufferedReader(new InputStreamReader(URLstream, "UTF-8"));
        String line = null;
        String alllines = "";
        while ((line = buffer.readLine()) != null) {
            alllines += line;
        }
        return alllines;
    }
}
