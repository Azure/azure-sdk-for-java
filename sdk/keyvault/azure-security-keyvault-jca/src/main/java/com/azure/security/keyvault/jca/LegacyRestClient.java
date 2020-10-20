// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.HttpEntities;

import java.io.IOException;
import java.util.Map;

/**
 * The RestClient that uses the Apache HttpClient class.
 */
class LegacyRestClient implements RestClient {

    /**
     * Constructor.
     */
    public LegacyRestClient() {
    }

    @Override
    public String get(String url, Map<String, String> headers) {
        String result = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            if (headers != null) {
                headers.entrySet().forEach(entry -> {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    httpGet.addHeader(key, value);
                });
            }
            HttpClientResponseHandler<String> responseHandler = (ClassicHttpResponse response) -> {
                int status = response.getCode();
                String result1 = null;
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    result1 = entity != null ? EntityUtils.toString(entity) : null;
                }
                return result1;
            };
            result = client.execute(httpGet, responseHandler);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return result;
    }

    @Override
    public String post(String url, String body, String contentType) {
        String result = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(HttpEntities.create(body, ContentType.create(contentType)));
            HttpClientResponseHandler<String> responseHandler = (ClassicHttpResponse response) -> {
                int status = response.getCode();
                String result1 = null;
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    result1 = entity != null ? EntityUtils.toString(entity) : null;
                }
                return result1;
            };
            result = client.execute(httpPost, responseHandler);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return result;
    }
}
