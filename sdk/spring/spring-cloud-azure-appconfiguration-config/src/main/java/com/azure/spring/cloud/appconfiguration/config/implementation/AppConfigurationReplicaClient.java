// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.time.Instant;
import java.util.List;

import org.springframework.util.StringUtils;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.TracingInfo;

import reactor.core.publisher.Mono;

/**
 * Client for connecting to App Configuration when multiple replicas are in use.
 */
class AppConfigurationReplicaClient {

    private final String endpoint;

    private final ConfigurationAsyncClient client;

    private Instant backoffEndTime;

    private int failedAttempts;

    private final TracingInfo tracingInfo;

    /**
     * Holds Configuration Client and info needed to manage backoff.
     * @param endpoint client endpoint
     * @param client Configuration Client to App Configuration store
     */
    AppConfigurationReplicaClient(String endpoint, ConfigurationAsyncClient client, TracingInfo tracingInfo) {
        this.endpoint = endpoint;
        this.client = client;
        this.backoffEndTime = Instant.now().minusMillis(1);
        this.failedAttempts = 0;
        this.tracingInfo = tracingInfo;
    }

    /**
     * @return backOffEndTime
     */
    Instant getBackoffEndTime() {
        return backoffEndTime;
    }

    /**
     * Updates the backoff time and increases the number of failed attempts.
     * @param backoffEndTime next time this client can be used.
     */
    void updateBackoffEndTime(Instant backoffEndTime) {
        this.backoffEndTime = backoffEndTime;
        this.failedAttempts += 1;
    }

    /**
     * @return number of failed attempts
     */
    int getFailedAttempts() {
        return failedAttempts;
    }

    /**
     * @return endpoint
     */
    String getEndpoint() {
        return endpoint;
    }

    /**
     * Gets the Configuration Setting for the given config store that match the Setting Selector criteria. Follows
     * retry-after-ms header.
     *
     * @param key String value of the watch key
     * @param label String value of the watch key, use \0 for null.
     * @return 
     * @return The first returned configuration.
     */
    Mono<ConfigurationSetting> getWatchKey(String key, String label)
        throws HttpResponseException {
        try {
            return client.getConfigurationSetting(key, label);
            /*ConfigurationSetting watchKey = NormalizeNull
                .normalizeNullLabel(client.getConfigurationSetting(key, label));
            this.failedAttempts = 0;
            return watchKey;*/
        } catch (HttpResponseException e) {
            if (e.getResponse() != null) {
                int statusCode = e.getResponse().getStatusCode();

                if (statusCode == 429 || statusCode == 408 || statusCode >= 500) {
                    throw new AppConfigurationStatusException(e.getMessage(), e.getResponse(), e.getValue());
                }
            }
            throw e;
        } catch (Exception e) { // TODO (mametcal) This should be an UnknownHostException, but currently it isn't
            // catchable.
            if (e.getMessage().startsWith("java.net.UnknownHostException")
                || e.getMessage().startsWith("java.net.WebSocketHandshakeException")
                || e.getMessage().startsWith("java.net.SocketException")
                || e.getMessage().startsWith("java.io.IOException") || e.getMessage()
                    .startsWith("io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused")) {
                throw new AppConfigurationStatusException(e.getMessage(), null, null);
            }
            throw e;
        }
    }

    /**
     * Gets a list of Configuration Settings from the given config store that match the Setting Selector criteria.
     *
     * @param settingSelector Information on which setting to pull. i.e. number of results, key value...
     * @return 
     * @return List of Configuration Settings.
     */
    PagedFlux<ConfigurationSetting> listSettings(SettingSelector settingSelector)
        throws HttpResponseException {
        //List<ConfigurationSetting> configurationSettings = new ArrayList<>();
        try {
            //return client.listConfigurationSettings(settingSelector);
           return client.listConfigurationSettings(settingSelector);
            /*this.failedAttempts = 0;
            settings.forEach(setting -> configurationSettings.add(NormalizeNull.normalizeNullLabel(setting)));
            return configurationSettings;*/
        } catch (HttpResponseException e) {
            if (e.getResponse() != null) {
                int statusCode = e.getResponse().getStatusCode();

                if (statusCode == 429 || statusCode == 408 || statusCode >= 500) {
                    throw new AppConfigurationStatusException(e.getMessage(), e.getResponse(), e.getValue());
                }
            }
            throw e;
        } catch (Exception e) { // TODO (mametcal) This should be an UnknownHostException, but currently it isn't
            // catchable.
            if (e.getMessage().startsWith("java.net.UnknownHostException")
                || e.getMessage().startsWith("java.net.WebSocketHandshakeException")
                || e.getMessage().startsWith("java.net.SocketException")
                || e.getMessage().startsWith("java.io.IOException") || e.getMessage()
                    .startsWith("io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused")) {
                throw new AppConfigurationStatusException(e.getMessage(), null, null);
            }
            throw e;
        }
    }

    /**
     * Update the sync token for a client store.
     * @param syncToken the sync token.
     */
    void updateSyncToken(String syncToken) {
        if (StringUtils.hasText(syncToken)) {
            client.updateSyncToken(syncToken);
        }
    }

    TracingInfo getTracingInfo() {
        return tracingInfo;
    }

}
