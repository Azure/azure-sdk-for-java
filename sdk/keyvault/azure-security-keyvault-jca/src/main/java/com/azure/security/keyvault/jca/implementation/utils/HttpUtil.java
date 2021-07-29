// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.utils;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The RestClient that uses the Apache HttpClient class.
 */
public final class HttpUtil {

    static final String USER_AGENT_KEY = "User-Agent";
    static final String DEFAULT_USER_AGENT_VALUE_PREFIX = "az-se-kv-jca/";
    public static final String DEFAULT_VERSION = "unknown";
    public static final String VERSION = Optional.of(HttpUtil.class)
                                                 .map(Class::getPackage)
                                                 .map(Package::getImplementationVersion)
                                                 .orElse(DEFAULT_VERSION);
    public static final String USER_AGENT_VALUE = getUserAgentPrefix() + VERSION;

    public static String get(String url, Map<String, String> headers) {
        String result = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            if (headers != null) {
                headers.forEach(httpGet::addHeader);
            }
            httpGet.addHeader(USER_AGENT_KEY, USER_AGENT_VALUE);
            result = client.execute(httpGet, createResponseHandler());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return result;
    }

    public static String post(String url, String body, String contentType) {
        return post(url, null, body, contentType);
    }

    public static String getUserAgentPrefix() {
        return Optional.of(HttpUtil.class)
                       .map(Class::getClassLoader)
                       .map(c -> c.getResourceAsStream("azure-security-keyvault-jca-user-agent-value-prefix.txt"))
                       .map(InputStreamReader::new)
                       .map(BufferedReader::new)
                       .map(BufferedReader::lines)
                       .orElseGet(Stream::empty)
                       .findFirst()
                       .orElse(DEFAULT_USER_AGENT_VALUE_PREFIX);
    }

    public static String post(String url, Map<String, String> headers, String body, String contentType) {
        String result = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            if (headers != null) {
                httpPost.addHeader(USER_AGENT_KEY, USER_AGENT_VALUE);
                headers.forEach(httpPost::addHeader);
                httpPost.addHeader("Content-Type", "application/json");
            }
            httpPost.setEntity(new StringEntity(body, ContentType.create(contentType)));
            result = client.execute(httpPost, createResponseHandler());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return result;
    }


    private static ResponseHandler<String> createResponseHandler() {
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
