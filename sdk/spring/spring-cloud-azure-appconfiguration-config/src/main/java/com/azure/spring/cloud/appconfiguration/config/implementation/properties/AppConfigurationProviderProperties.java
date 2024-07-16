// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import jakarta.annotation.PostConstruct;

/**
 * Properties defining connection to Azure App Configuration.
 */
@ConfigurationProperties(prefix = AppConfigurationProviderProperties.CONFIG_PREFIX)
public class AppConfigurationProviderProperties {

    /**
     * Prefix for the libraries internal configurations.
     */
    public static final String CONFIG_PREFIX = "spring.cloud.appconfiguration";

    private static final Instant START_DATE = Instant.now();

    @Value("${version:1.0}")
    private String version;

    @Value("${maxRetries:2}")
    private int maxRetries;

    @Value("${maxRetryTime:60}")
    private int maxRetryTime;

    @Value("${prekillTime:5}")
    private int prekillTime;

    @Value("${defaultMinBackoff:30}")
    private Long defaultMinBackoff;

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
        return START_DATE;
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
    
    @PostConstruct
    public void validateAndInit() {
        Assert.hasLength(version, "A version of app configuration should be set.");
        Assert.notNull(maxRetries, "A number of max retries has to be configured.");
        Assert.notNull(maxRetryTime, "A max retry value needs to be configured");
        Assert.notNull(prekillTime, "A preKill time value needs to be configured.");
        Assert.notNull(defaultMinBackoff, "A default minimum backoff time value needs to be set.");
        Assert.notNull(defaultMaxBackoff, "A default max backoff time value needs to be set.");
    }

}
