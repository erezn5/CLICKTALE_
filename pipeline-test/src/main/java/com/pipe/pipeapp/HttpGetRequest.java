package com.pipe.pipeapp;


import helpers.CageArchivePackage;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.apache.http.HttpHeaders.USER_AGENT;

public class HttpGetRequest {

    StringBuilder m_url = new StringBuilder();
    String result = "";
    String m_domain = "";
    String m_ua = "";
    String m_referrer = "";
    CageArchivePackage cageArchivePackage;


    public HttpGetRequest(StringBuilder i_url) {
        m_url = i_url;
    }

    public HttpGetRequest(){
        ;
    }
    public String sendAuthRequest(boolean isMobile) {

        HttpGet httpGet = new HttpGet(m_url.toString());

        if (m_domain.equals(""))
            httpGet.setHeader("Origin", Configuration.prop.getProperty("suite.pusher.defaultParams.origin"));
        if(isMobile==true){
            httpGet.addHeader("User-Agent",Configuration.prop.getProperty("suite.pusher.defaultParams.uaIphone6"));
        }else{
            httpGet.addHeader("User-Agent", Configuration.prop.getProperty("suite.pusher.defaultParams.ua"));
        }

        if (m_referrer.equals(""))
            httpGet.addHeader("Referer", Configuration.prop.getProperty("suite.pusher.defaultParams.referer"));
        try {

            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute(httpGet);
            StringWriter writer = new StringWriter();
            IOUtils.copy(response.getEntity().getContent(), writer, "UTF-8");
            result = writer.toString();


        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public String getRequest(String url) throws IOException, InterruptedException {
        Thread.sleep(2000);
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection)obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null){
            response.append(inputLine);
        }
        in.close();

        return response.toString();

    }

}
