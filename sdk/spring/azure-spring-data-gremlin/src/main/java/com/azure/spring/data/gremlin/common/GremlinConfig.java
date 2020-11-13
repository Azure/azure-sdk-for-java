// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common;

import java.beans.ConstructorProperties;
import org.apache.tinkerpop.gremlin.driver.ser.Serializers;

public final class GremlinConfig {

    private String endpoint;

    private int port;

    private String username;

    private String password;

    private boolean sslEnabled;

    private boolean telemetryAllowed;

    private String serializer;

    private int maxContentLength;

    @ConstructorProperties({"endpoint", "port", "username", "password", "sslEnabled", "telemetryAllowed", "serializer",
        "maxContentLength"})
    private GremlinConfig(String endpoint, int port, String username, String password, boolean sslEnabled,
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

    public static GremlinConfig.GremlinConfigBuilder defaultBuilder() {
        return new GremlinConfig.GremlinConfigBuilder();
    }

    public static GremlinConfigBuilder builder(String endpoint, String username, String password) {
        return defaultBuilder()
            .endpoint(endpoint)
            .username(username)
            .password(password)
            .port(Constants.DEFAULT_ENDPOINT_PORT)
            .sslEnabled(true)
            .serializer(Serializers.GRAPHSON.toString())
            .telemetryAllowed(true);
    }

    public static class GremlinConfigBuilder {

        private String endpoint;
        private int port;
        private String username;
        private String password;
        private boolean sslEnabled;
        private boolean telemetryAllowed;
        private String serializer;
        private int maxContentLength;

        public GremlinConfig.GremlinConfigBuilder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public GremlinConfig.GremlinConfigBuilder port(int port) {
            this.port = port;
            return this;
        }

        public GremlinConfig.GremlinConfigBuilder username(String username) {
            this.username = username;
            return this;
        }

        public GremlinConfig.GremlinConfigBuilder password(String password) {
            this.password = password;
            return this;
        }

        public GremlinConfig.GremlinConfigBuilder sslEnabled(boolean sslEnabled) {
            this.sslEnabled = sslEnabled;
            return this;
        }

        public GremlinConfig.GremlinConfigBuilder telemetryAllowed(boolean telemetryAllowed) {
            this.telemetryAllowed = telemetryAllowed;
            return this;
        }

        public GremlinConfig.GremlinConfigBuilder serializer(String serializer) {
            this.serializer = serializer;
            return this;
        }

        public GremlinConfig.GremlinConfigBuilder maxContentLength(int maxContentLength) {
            this.maxContentLength = maxContentLength;
            return this;
        }

        public GremlinConfig build() {
            return new GremlinConfig(this.endpoint, this.port, this.username, this.password, this.sslEnabled,
                this.telemetryAllowed, this.serializer, this.maxContentLength);
        }

        public String toString() {
            return "GremlinConfig.GremlinConfigBuilder(endpoint=" + this.endpoint + ", port=" + this.port
                + ", username=" + this.username + ", password=" + this.password + ", sslEnabled=" + this.sslEnabled
                + ", telemetryAllowed=" + this.telemetryAllowed + ", serializer=" + this.serializer
                + ", maxContentLength=" + this.maxContentLength + ")";
        }
    }

    public String getEndpoint() {
        return endpoint;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public boolean isTelemetryAllowed() {
        return telemetryAllowed;
    }

    public String getSerializer() {
        return serializer;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }
}
