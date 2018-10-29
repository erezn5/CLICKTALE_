package com.pipe.pipeapp.Production;

import com.pipe.pipeapp.Configuration;
import com.pipe.pipeapp.HttpGetRequest;

import java.io.IOException;

public class CageResponseProduction {

    String url = Configuration.prop.getProperty("suite.pusher.defaultParams.CageProdInstanceIp");
    public String getCageResponse() throws InterruptedException {
        HttpGetRequest cageResponse = new HttpGetRequest(new StringBuilder(url));
        String response= null;
        try {
            System.out.println("Getting cage response from: \n" + Configuration.prop.getProperty("suite.pusher.defaultParams.CageProdInstanceIp")+"/" + "233256" + "/" + "56" + "/" + "1485784893571335");
            response = cageResponse.getRequest(Configuration.prop.getProperty("suite.pusher.defaultParams.CageProdInstanceIp")+"/" + "233256" + "/" + "56" + "/" + "1485784893571335");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }
}
