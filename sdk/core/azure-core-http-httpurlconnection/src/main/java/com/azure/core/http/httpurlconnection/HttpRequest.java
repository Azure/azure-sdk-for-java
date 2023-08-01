package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Flux;

import java.net.URL;
import java.nio.ByteBuffer;

public class HttpRequest extends com.azure.core.http.HttpRequest {


    public HttpRequest(HttpMethod httpMethod, URL url) {
        super(httpMethod, url);
    }

    public HttpRequest(HttpMethod httpMethod, String url) {
        super(httpMethod, url);
    }

    public HttpRequest(HttpMethod httpMethod, URL url, HttpHeaders headers) {
        super(httpMethod, url, headers);
    }

    public HttpRequest(HttpMethod httpMethod, URL url, HttpHeaders headers, Flux<ByteBuffer> body) {
        super(httpMethod, url, headers, body);
    }

    public HttpRequest(HttpMethod httpMethod, URL url, HttpHeaders headers, BinaryData body) {
        super(httpMethod, url, headers, body);
    }
}
