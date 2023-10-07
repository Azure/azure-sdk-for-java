package com.azure.core.http.httpurlconnection.implementation;

import java.time.Duration;

public class HttpUrlConnectionTimeouts {
    public Duration connectionTimeout;
    public Duration readTimeout;
    public Duration writeTimeout;
    public Duration responseTimeout;

    public HttpUrlConnectionTimeouts(Duration connectionTimeout, Duration readTimeout, Duration writeTimeout, Duration responseTimeout) {
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
        this.responseTimeout = responseTimeout;
    }


}
