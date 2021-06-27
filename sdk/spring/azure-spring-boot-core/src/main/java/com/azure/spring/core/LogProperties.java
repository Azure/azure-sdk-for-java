// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.core;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.util.logging.LogLevel;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Azure properties used for client log option.
 */
@ConfigurationProperties(AzureProperties.PREFIX)
public class LogProperties {

    /**
     * Enables logging by setting a log level.
     */
    private LogLevel logLevel;

    /**
     * Enables HTTP request/response logging by setting an HTTP log detail level.
     */
    private HttpLogDetailLevel httpLogDetailLevel;

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public HttpLogDetailLevel getHttpLogDetailLevel() {
        return httpLogDetailLevel;
    }

    public void setHttpLogDetailLevel(HttpLogDetailLevel httpLogDetailLevel) {
        this.httpLogDetailLevel = httpLogDetailLevel;
    }
}
