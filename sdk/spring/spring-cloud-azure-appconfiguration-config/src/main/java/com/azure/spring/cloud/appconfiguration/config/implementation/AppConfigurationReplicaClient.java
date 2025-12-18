// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSnapshot;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.data.appconfiguration.models.SnapshotComposition;
import com.azure.spring.cloud.appconfiguration.config.implementation.configuration.CollectionMonitoring;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Client for connecting to App Configuration when multiple replicas are in use.
 * 
 * The client automatically retries retryable HTTP errors (429, 408, 5xx) and implements backoff logic to prevent
 * overwhelming failing replicas.
 */
class AppConfigurationReplicaClient {
    private static final long INITIAL_BACKOFF_OFFSET_MS = 1L;

    /** HTTP status code for "Not Modified" responses */
    private static final int HTTP_NOT_MODIFIED = 304;

    private final String endpoint;

    private final String originClient;

    private final ConfigurationClient client;

    private Instant backoffEndTime;

    private int failedAttempts;

    /**
     * Holds Configuration Client and info needed to manage backoff.
     * @param endpoint client endpoint
     * @param originClient origin client identifier
     * @param client Configuration Client to App Configuration store
     */
    AppConfigurationReplicaClient(String endpoint, String originClient, ConfigurationClient client) {
        this.endpoint = endpoint;
        this.originClient = originClient;
        this.client = client;
        this.backoffEndTime = Instant.now().minusMillis(INITIAL_BACKOFF_OFFSET_MS);
        this.failedAttempts = 0;
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
     * @return originClient
     */
    String getOriginClient() {
        return originClient;
    }

    /**
     * Gets the Configuration Setting for the given config store that match the Setting Selector criteria. Follows
     * retry-after-ms header.
     *
     * @param key String value of the watch key
     * @param label String value of the watch key, use \0 for null.
     * @param context Azure SDK context for request correlation
     * @return The first returned configuration.
     * @throws HttpResponseException if the request fails
     */
    ConfigurationSetting getWatchKey(String key, String label, Context context)
        throws HttpResponseException {
        try {
            ConfigurationSetting selector = new ConfigurationSetting().setKey(key).setLabel(label);
            ConfigurationSetting watchKey = NormalizeNull
                .normalizeNullLabel(
                    client.getConfigurationSettingWithResponse(selector, null, false, context).getValue());
            this.failedAttempts = 0;
            return watchKey;
        } catch (HttpResponseException e) {
            throw handleHttpResponseException(e);
        } catch (UncheckedIOException e) {
            throw new AppConfigurationStatusException(e.getMessage(), null, null);
        }
    }

    /**
     * Gets a list of Configuration Settings from the given config store that match the Setting Selector criteria.
     *
     * @param settingSelector Information on which setting to pull. i.e. number of results, key value...
     * @param context Azure SDK context for request correlation
     * @return List of Configuration Settings.
     * @throws HttpResponseException if the request fails
     */
    List<ConfigurationSetting> listSettings(SettingSelector settingSelector, Context context)
        throws HttpResponseException {
        List<ConfigurationSetting> configurationSettings = new ArrayList<>();
        try {
            PagedIterable<ConfigurationSetting> settings = client.listConfigurationSettings(settingSelector, context);
            settings.forEach(setting -> {
                configurationSettings.add(NormalizeNull.normalizeNullLabel(setting));
            });
            // Needs to happen after or we don't know if the request succeeded or failed.
            this.failedAttempts = 0;
            return configurationSettings;
        } catch (HttpResponseException e) {
            throw handleHttpResponseException(e);
        } catch (UncheckedIOException e) {
            throw new AppConfigurationStatusException(e.getMessage(), null, null);
        }
    }

    /**
     * Gets configuration settings using collection monitoring. This method retrieves all settings matching
     * the selector and captures ETags for collection-based refresh monitoring.
     * 
     * @param settingSelector selector criteria for configuration settings
     * @param context Azure SDK context for request correlation
     * @return CollectionMonitoring containing the retrieved configuration settings and match conditions
     * @throws HttpResponseException if the request fails
     */
    CollectionMonitoring collectionMonitoring(SettingSelector settingSelector, Context context) {
        List<ConfigurationSetting> configurationSettings = new ArrayList<>();
        List<MatchConditions> checks = new ArrayList<>();
        try {
            client.listConfigurationSettings(settingSelector, context).streamByPage().forEach(pagedResponse -> {
                checks.add(
                    new MatchConditions().setIfNoneMatch(pagedResponse.getHeaders().getValue(HttpHeaderName.ETAG)));
                for (ConfigurationSetting setting : pagedResponse.getValue()) {
                    configurationSettings.add(NormalizeNull.normalizeNullLabel(setting));
                }
            });

            // Needs to happen after or we don't know if the request succeeded or failed.
            this.failedAttempts = 0;
            settingSelector.setMatchConditions(checks);
            return new CollectionMonitoring(settingSelector, configurationSettings);
        } catch (HttpResponseException e) {
            throw handleHttpResponseException(e);
        } catch (UncheckedIOException e) {
            throw new AppConfigurationStatusException(e.getMessage(), null, null);
        }
    }

    /**
     * Lists feature flags from the Azure App Configuration store.
     * 
     * @param settingSelector selector criteria for feature flags
     * @param context Azure SDK context for request correlation
     * @return FeatureFlags containing the retrieved feature flags and match conditions
     * @throws HttpResponseException if the request fails
     */
    CollectionMonitoring listFeatureFlags(SettingSelector settingSelector, Context context)
        throws HttpResponseException {
        List<ConfigurationSetting> configurationSettings = new ArrayList<>();
        List<MatchConditions> checks = new ArrayList<>();
        try {
            client.listConfigurationSettings(settingSelector, context).streamByPage().forEach(pagedResponse -> {
                checks.add(
                    new MatchConditions().setIfNoneMatch(pagedResponse.getHeaders().getValue(HttpHeaderName.ETAG)));
                for (ConfigurationSetting featureFlag : pagedResponse.getValue()) {
                    configurationSettings
                        .add((FeatureFlagConfigurationSetting) NormalizeNull.normalizeNullLabel(featureFlag));
                }
            });

            // Needs to happen after or we don't know if the request succeeded or failed.
            this.failedAttempts = 0;
            settingSelector.setMatchConditions(checks);
            return new CollectionMonitoring(settingSelector, configurationSettings);
        } catch (HttpResponseException e) {
            throw handleHttpResponseException(e);
        } catch (UncheckedIOException e) {
            throw new AppConfigurationStatusException(e.getMessage(), null, null);
        }
    }

    /**
     * Lists configuration settings from a specific snapshot.
     * 
     * @param snapshotName the name of the snapshot to retrieve settings from
     * @param context Azure SDK context for request correlation
     * @return list of configuration settings from the snapshot
     * @throws IllegalArgumentException if the snapshot is not of type KEY
     * @throws HttpResponseException if the request fails
     */
    List<ConfigurationSetting> listSettingSnapshot(String snapshotName, Context context) {
        List<ConfigurationSetting> configurationSettings = new ArrayList<>();
        try {
            // Because Spring always refreshes all we still have to load snapshots on refresh to build the property
            // sources.
            ConfigurationSnapshot snapshot = client.getSnapshotWithResponse(snapshotName, null, context).getValue();
            if (!SnapshotComposition.KEY.equals(snapshot.getSnapshotComposition())) {
                throw new IllegalArgumentException("Snapshot " + snapshotName + " needs to be of type Key.");
            }

            PagedIterable<ConfigurationSetting> settings = client.listConfigurationSettingsForSnapshot(snapshotName);
            this.failedAttempts = 0;
            settings.forEach(setting -> configurationSettings.add(NormalizeNull.normalizeNullLabel(setting)));
            return configurationSettings;
        } catch (HttpResponseException e) {
            throw handleHttpResponseException(e);
        } catch (UncheckedIOException e) {
            throw new AppConfigurationStatusException(e.getMessage(), null, null);
        }
    }

    boolean checkWatchKeys(SettingSelector settingSelector, Context context) {
        List<PagedResponse<ConfigurationSetting>> results = client
            .listConfigurationSettings(settingSelector, context)
            .streamByPage().filter(pagedResponse -> pagedResponse.getStatusCode() != HTTP_NOT_MODIFIED).toList();
        return results.size() > 0;
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

    /**
     * Handles HTTP response exceptions by determining if they are retryable.
     * 
     * @param e the HTTP response exception to handle
     * @return an AppConfigurationStatusException for retryable errors, or the original exception for non-retryable
     * errors
     */
    private HttpResponseException handleHttpResponseException(HttpResponseException e) {
        if (e.getResponse() != null) {
            int statusCode = e.getResponse().getStatusCode();

            if (statusCode == HttpResponseStatus.TOO_MANY_REQUESTS.code()
                || statusCode == HttpResponseStatus.REQUEST_TIMEOUT.code()
                || statusCode >= HttpResponseStatus.INTERNAL_SERVER_ERROR.code()) {
                return new AppConfigurationStatusException(e.getMessage(), e.getResponse(), e.getValue());
            }
        }
        return e;
    }

}
