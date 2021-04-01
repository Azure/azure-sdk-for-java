// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

/**
 * The RestClient that uses the Apache HttpClient class.
 */
class LegacyRestClient implements RestClient {

    /**
     * Constructor.
     */
    LegacyRestClient() {
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
            result = client.execute(httpGet, createResponseHandler());
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
            httpPost.setEntity(
                new StringEntity(body, ContentType.create(contentType)));
            result = client.execute(httpPost, createResponseHandler());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return result;
    }

    private ResponseHandler<String> createResponseHandler() {
        return (HttpResponse response) -> {
            int status = response.getStatusLine().getStatusCode();
            String result = null;
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                result = entity != null ? EntityUtils.toString(entity) : null;
            }
            return result;
        };
    }
}
