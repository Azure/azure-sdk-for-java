package com.microsoft.windowsazure.services.blob.implementation;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;

public class HttpURLConnectionClient extends Client {
    private final HttpURLConnectionClientHandler rootHandler;

    public HttpURLConnectionClient(HttpURLConnectionClientHandler handler, ClientConfig config) {
        super(handler, config);
        this.rootHandler = handler;
    }

    public static HttpURLConnectionClient create(ClientConfig config) {
        return new HttpURLConnectionClient(new HttpURLConnectionClientHandler(), config);
    }

    public HttpURLConnectionClientHandler getRootHandler() {
        return rootHandler;
    }
}
