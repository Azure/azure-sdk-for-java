package com.azure.perfstress;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import reactor.core.publisher.Mono;

public class PerfStressHttpClient {
    public static HttpClient create(PerfStressOptions options) {
        HttpClient httpClient = HttpClient.createDefault();

        if (options.Insecure) {
            makeInsecure(httpClient);
        }

        if (options.Host != null && options.Host.length() > 0) {
            httpClient = new ChangeUriHttpClient(httpClient, options.Host, options.Port);
        }

        return httpClient;
    }
     
    private static void makeInsecure(HttpClient httpClient) {
        Field nettyClientField;
        try {
            nettyClientField = httpClient.getClass().getDeclaredField("nettyClient");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
        nettyClientField.setAccessible(true);

        reactor.netty.http.client.HttpClient nettyClient;
        try {
            nettyClient = (reactor.netty.http.client.HttpClient) nettyClientField.get(httpClient);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        SslContextBuilder sslContextBuilder = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE);

        try {
            nettyClientField.set(httpClient, nettyClient.secure(spec -> spec.sslContext(sslContextBuilder)));
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ChangeUriHttpClient implements HttpClient {
        private final HttpClient _httpClient;
        private final String _host;
        private final int _port;
    
        public ChangeUriHttpClient(HttpClient httpClient, String host, int port) {
            _httpClient = httpClient;
            _host = host;
            _port = port;
        }
    
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            request.getHeaders().put("Host", request.getUrl().getHost());
    
            String protocol = request.getUrl().getProtocol();
            String host = _host;
            int port = _port;
            String file = request.getUrl().getFile();
    
            try {
                request.setUrl(new URL(protocol, host, port, file));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
    
            return _httpClient.send(request);
        }
    }    
}
