// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_CONTENT_TYPE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_PREFIX;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_MANAGEMENT_KEY;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.SELECT_ALL_FEATURE_FLAGS;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.TracingInfo;

/**
 * Azure App Configuration PropertySource unique per Store Label(Profile) combo.
 *
 * <p>
 * i.e. If connecting to 2 stores and have 2 labels set 4 AppConfigurationPropertySources need to be created.
 * </p>
 */
final class AppConfigurationFeatureManagementPropertySource extends AppConfigurationPropertySource {

    protected final String keyFilter;

    protected final String[] labelFilter;

    AppConfigurationFeatureManagementPropertySource(String originEndpoint, AppConfigurationReplicaClient replicaClient,
        String keyFilter, String[] labelFilter) {
        super("FM_" + originEndpoint, replicaClient);
        this.keyFilter = keyFilter;
        this.labelFilter = labelFilter;
    }

    /**
     * <p>
     * Gets settings from Azure/Cache to set as configurations. Updates the cache.
     * </p>
     *
     * <p>
     * <b>Note</b>: Doesn't update Feature Management, just stores values in cache. Call {@code initFeatures} to update
     * Feature Management, but make sure its done in the last {@code AppConfigurationPropertySource}
     * AppConfigurationPropertySource}
     * </p>
     *
     */
    public void initProperties() {
        SettingSelector settingSelector = new SettingSelector();

        String keyFilter = SELECT_ALL_FEATURE_FLAGS;

        if (StringUtils.hasText(this.keyFilter)) {
            keyFilter = FEATURE_FLAG_PREFIX + this.keyFilter;
        }

        settingSelector.setKeyFilter(keyFilter);

        List<String> labels = Arrays.asList(labelFilter);
        Collections.reverse(labels);

        for (String label : labels) {
            settingSelector.setLabelFilter(label);

            List<ConfigurationSetting> features = replicaClient.listSettings(settingSelector);
            TracingInfo tracing = replicaClient.getTracingInfo();

            // Reading In Features
            for (ConfigurationSetting setting : features) {
                if (setting instanceof FeatureFlagConfigurationSetting
                    && FEATURE_FLAG_CONTENT_TYPE.equals(setting.getContentType())) {
                    featureConfigurationSettings.add(setting);
                    FeatureFlagConfigurationSetting featureFlag = (FeatureFlagConfigurationSetting) setting;

                    String configName = FEATURE_MANAGEMENT_KEY
                        + setting.getKey().trim().substring(FEATURE_FLAG_PREFIX.length());

                    updateTelemetry(featureFlag, tracing);

                    properties.put(configName, createFeature(featureFlag));
                }
            }
        }
    }
}
