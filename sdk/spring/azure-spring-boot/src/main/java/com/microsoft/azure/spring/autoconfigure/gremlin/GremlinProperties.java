// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.autoconfigure.gremlin;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * Configuration properties for Gremlin login, telemetry, ssl.
 */
@Validated
@ConfigurationProperties("gremlin")
public class GremlinProperties {

    @NotEmpty
    private String endpoint;

    private int port;

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    private boolean telemetryAllowed = true;

    private boolean sslEnabled = true;

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

    public boolean isTelemetryAllowed() {
        return telemetryAllowed;
    }

    public void setTelemetryAllowed(boolean telemetryAllowed) {
        this.telemetryAllowed = telemetryAllowed;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }
}
