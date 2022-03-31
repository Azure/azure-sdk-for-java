// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.properties;

import java.time.Instant;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

/**
 * Properties defining connection to Azure App Configuration.
 */
@Configuration
@Validated
@PropertySource("classpath:appConfiguration.yaml")
@ConfigurationProperties(prefix = AppConfigurationProviderProperties.CONFIG_PREFIX)
public class AppConfigurationProviderProperties {

    /**
     * Prefix for the libraries internal configurations.
     */
    public static final String CONFIG_PREFIX = "spring.cloud.appconfiguration";
    private static final Instant startDate = Instant.now();

    @NotEmpty
    @Value("${version:1.0}")
    private String version;

    @NotNull
    @Value("${maxRetries:2}")
    private int maxRetries;

    @NotNull
    @Value("${maxRetryTime:60}")
    private int maxRetryTime;

    @NotNull
    @Value("${prekillTime:5}")
    private int prekillTime;

    @NotNull
    @Value("${defaultMinBackoff:30}")
    private Long defaultMinBackoff;

    @NotNull
    @Value("${defaultMaxBackoff:600}")
    private Long defaultMaxBackoff;

    /**
     * @return the apiVersion
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param apiVersion the apiVersion to set
     */
    public void setVersion(String apiVersion) {
        this.version = apiVersion;
    }

    /**
     * @return the maxRetries
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * @param maxRetries the maxRetries to set
     */
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    /**
     * @return the maxRetryTime
     */
    public int getMaxRetryTime() {
        return maxRetryTime;
    }

    /**
     * @param maxRetryTime the maxRetryTime to set
     */
    public void setMaxRetryTime(int maxRetryTime) {
        this.maxRetryTime = maxRetryTime;
    }

    /**
     * @return the prekillTime
     */
    public int getPrekillTime() {
        return prekillTime;
    }

    /**
     * @param prekillTime the prekillTime to set
     */
    public void setPrekillTime(int prekillTime) {
        this.prekillTime = prekillTime;
    }

    /**
     * @return the startDate
     */
    public Instant getStartDate() {
        return startDate;
    }

    /**
     * @return the defaultMinBackoff
     */
    public Long getDefaultMinBackoff() {
        return defaultMinBackoff;
    }

    /**
     * @param defaultMinBackoff the defaultMinBackoff to set
     */
    public void setDefaultMinBackoff(Long defaultMinBackoff) {
        this.defaultMinBackoff = defaultMinBackoff;
    }

    /**
     * @return the defaultMaxBackoff
     */
    public Long getDefaultMaxBackoff() {
        return defaultMaxBackoff;
    }

    /**
     * @param defaultMaxBackoff the defaultMaxBackoff to set
     */
    public void setDefaultMaxBackoff(Long defaultMaxBackoff) {
        this.defaultMaxBackoff = defaultMaxBackoff;
    }

}
