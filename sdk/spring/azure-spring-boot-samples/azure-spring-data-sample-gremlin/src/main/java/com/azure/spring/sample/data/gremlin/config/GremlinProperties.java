// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.data.gremlin.config;

import org.apache.tinkerpop.gremlin.driver.ser.Serializers;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("gremlin")
public class GremlinProperties {

    private String endpoint;

    private int port;

    private String username;

    private String password;

    private boolean sslEnabled;

    private boolean telemetryAllowed = true;

    private String serializer = Serializers.GRAPHSON.toString();

    private int maxContentLength;

    public GremlinProperties() {
    }

    public GremlinProperties(String endpoint, int port, String username, String password, boolean sslEnabled,
                             boolean telemetryAllowed, String serializer, int maxContentLength) {
        this.endpoint = endpoint;
        this.port = port;
        this.username = username;
        this.password = password;
        this.sslEnabled = sslEnabled;
        this.telemetryAllowed = telemetryAllowed;
        this.serializer = serializer;
        this.maxContentLength = maxContentLength;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public boolean isTelemetryAllowed() {
        return telemetryAllowed;
    }

    public void setTelemetryAllowed(boolean telemetryAllowed) {
        this.telemetryAllowed = telemetryAllowed;
    }

    public String getSerializer() {
        return serializer;
    }

    public void setSerializer(String serializer) {
        this.serializer = serializer;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }
}
