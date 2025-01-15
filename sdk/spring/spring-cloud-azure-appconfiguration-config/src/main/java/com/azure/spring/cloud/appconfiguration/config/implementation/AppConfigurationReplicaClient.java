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
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.FeatureFlags;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.TracingInfo;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Client for connecting to App Configuration when multiple replicas are in use.
 */
class AppConfigurationReplicaClient {

    private final String endpoint;

    private final ConfigurationClient client;

    private Instant backoffEndTime;

    private int failedAttempts;

    private final TracingInfo tracingInfo;

    /**
     * Holds Configuration Client and info needed to manage backoff.
     * @param endpoint client endpoint
     * @param client Configuration Client to App Configuration store
     */
    AppConfigurationReplicaClient(String endpoint, ConfigurationClient client, TracingInfo tracingInfo) {
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
     * @return The first returned configuration.
     */
    ConfigurationSetting getWatchKey(String key, String label, Boolean isRefresh)
        throws HttpResponseException {
        try {
            Context context = new Context("refresh", isRefresh);
            ConfigurationSetting selector = new ConfigurationSetting().setKey(key).setLabel(label);
            ConfigurationSetting watchKey = NormalizeNull
                .normalizeNullLabel(
                    client.getConfigurationSettingWithResponse(selector, null, false, context).getValue());
            this.failedAttempts = 0;
            return watchKey;
        } catch (HttpResponseException e) {
            throw hanndleHttpResponseException(e);
        } catch (UncheckedIOException e) {
            throw new AppConfigurationStatusException(e.getMessage(), null, null);
        }
    }

    /**
     * Gets a list of Configuration Settings from the given config store that match the Setting Selector criteria.
     *
     * @param settingSelector Information on which setting to pull. i.e. number of results, key value...
     * @return List of Configuration Settings.
     */
    List<ConfigurationSetting> listSettings(SettingSelector settingSelector, Boolean isRefresh)
        throws HttpResponseException {
        List<ConfigurationSetting> configurationSettings = new ArrayList<>();
        try {
            Context context = new Context("refresh", isRefresh);
            PagedIterable<ConfigurationSetting> settings = client.listConfigurationSettings(settingSelector, context);
            settings.forEach(setting -> {
                configurationSettings.add(NormalizeNull.normalizeNullLabel(setting));
            });
            // Needs to happen after or we don't know if the request succeeded or failed.
            this.failedAttempts = 0;
            return configurationSettings;
        } catch (HttpResponseException e) {
            throw hanndleHttpResponseException(e);
        } catch (UncheckedIOException e) {
            throw new AppConfigurationStatusException(e.getMessage(), null, null);
        }
    }

    FeatureFlags listFeatureFlags(SettingSelector settingSelector, Boolean isRefresh) throws HttpResponseException {
        List<ConfigurationSetting> configurationSettings = new ArrayList<>();
        List<MatchConditions> checks = new ArrayList<>();
        try {
            Context context = new Context("refresh", isRefresh);
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
            return new FeatureFlags(settingSelector, configurationSettings);
        } catch (HttpResponseException e) {
            throw hanndleHttpResponseException(e);
        } catch (UncheckedIOException e) {
            throw new AppConfigurationStatusException(e.getMessage(), null, null);
        }
    }

    List<ConfigurationSetting> listSettingSnapshot(String snapshotName) {
        List<ConfigurationSetting> configurationSettings = new ArrayList<>();
        try {
            ConfigurationSnapshot snapshot = client.getSnapshot(snapshotName);
            if (!SnapshotComposition.KEY.equals(snapshot.getSnapshotComposition())) {
                throw new IllegalArgumentException("Snapshot " + snapshotName + " needs to be of type Key.");
            }

            PagedIterable<ConfigurationSetting> settings = client.listConfigurationSettingsForSnapshot(snapshotName);
            this.failedAttempts = 0;
            settings.forEach(setting -> configurationSettings.add(NormalizeNull.normalizeNullLabel(setting)));
            return configurationSettings;
        } catch (HttpResponseException e) {
            throw hanndleHttpResponseException(e);
        } catch (UncheckedIOException e) {
            throw new AppConfigurationStatusException(e.getMessage(), null, null);
        }
    }

    Boolean checkWatchKeys(SettingSelector settingSelector, Boolean isRefresh) {
        Context context = new Context("refresh", isRefresh);
        List<PagedResponse<ConfigurationSetting>> results = client.listConfigurationSettings(settingSelector, context)
            .streamByPage().filter(pagedResponse -> pagedResponse.getStatusCode() != 304).toList();
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

    private HttpResponseException hanndleHttpResponseException(HttpResponseException e) {
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

    TracingInfo getTracingInfo() {
        return tracingInfo;
    }

}
