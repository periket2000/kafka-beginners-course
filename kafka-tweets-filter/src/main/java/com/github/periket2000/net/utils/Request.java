package com.github.periket2000.net.utils;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class Request {

    /**
     * Send a post request with json body
     * @param url
     * @param json
     * @return
     * @throws IOException
     */
    public static String sendPOST(String url, String json) throws IOException {
        String result;
        HttpPost post = new HttpPost(url);
        // send a JSON data
        post.setEntity(new StringEntity(json));
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {

            result = EntityUtils.toString(response.getEntity());
        }
        return result;
    }

}
