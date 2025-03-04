package com.azure.core.http.netty.samples;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;

public class HttpPostFormDataExample {
    public static void main(String... args) {
        HttpClient client = new NettyAsyncHttpClientBuilder().build();
        HttpHeaders headers = new HttpHeaders().set("Content-Type", "application/x-www-form-urlencoded");
        String formData = "key1=value1&key2=value2";
        HttpRequest request = new HttpRequest(HttpMethod.POST, "https://example.com", headers, formData);

        HttpResponse response = client.send(request).block();
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
    }
}
