// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_CONTENT_TYPE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_PREFIX;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_MANAGEMENT_KEY;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.TracingInfo;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Azure App Configuration PropertySource unique per Store Label(Profile) combo.
 *
 * <p>
 * i.e. If connecting to 2 stores and have 2 labels set 4 AppConfigurationPropertySources need to be created.
 * </p>
 */
final class AppConfigurationApplicationSettingPropertySource extends AppConfigurationPropertySource {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AppConfigurationApplicationSettingPropertySource.class);

    private final AppConfigurationKeyVaultClientFactory keyVaultClientFactory;

    private final int maxRetryTime;

    private final String keyFilter;

    private final String[] labelFilter;

    private final String snapshotName;

    AppConfigurationApplicationSettingPropertySource(String originEndpoint, AppConfigurationReplicaClient replicaClient,
        AppConfigurationKeyVaultClientFactory keyVaultClientFactory, String keyFilter, String[] labelFilter,
        int maxRetryTime) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely define a PropertySource
        super(keyFilter + originEndpoint + "/" + getLabelName(labelFilter), replicaClient);
        this.keyVaultClientFactory = keyVaultClientFactory;
        this.maxRetryTime = maxRetryTime;
        this.keyFilter = keyFilter;
        this.labelFilter = labelFilter;
        this.snapshotName = null;
    }

    AppConfigurationApplicationSettingPropertySource(String originEndpoint, AppConfigurationReplicaClient replicaClient,
        AppConfigurationKeyVaultClientFactory keyVaultClientFactory, String snapshotName, int maxRetryTime) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely define a PropertySource
        super(snapshotName + originEndpoint + "/", replicaClient);
        this.keyVaultClientFactory = keyVaultClientFactory;
        this.maxRetryTime = maxRetryTime;
        this.keyFilter = null;
        this.labelFilter = null;
        this.snapshotName = snapshotName;
    }

    /**
     * <p>
     * Gets settings from Azure/Cache to set as configurations. Updates the cache.
     * </p>
     * 
     * @param trim prefix to trim
     * @throws JsonProcessingException thrown if fails to parse Json content type
     */
    public void initProperties(List<String> trim) throws JsonProcessingException {
        if (StringUtils.hasText(snapshotName)) {
            processConfigurationSettings(replicaClient.listSettingSnapshot(snapshotName), null, trim, true);
        } else {
            List<String> labels = Arrays.asList(labelFilter);
            Collections.reverse(labels);

            for (String label : labels) {
                SettingSelector settingSelector = new SettingSelector().setKeyFilter(keyFilter + "*")
                    .setLabelFilter(label);

                // * for wildcard match
                processConfigurationSettings(replicaClient.listSettings(settingSelector),
                    settingSelector.getKeyFilter(), trim, false);
            }
        }
    }

    private void processConfigurationSettings(List<ConfigurationSetting> settings, String keyFilter,
        List<String> trimStrings, boolean snapshot) throws JsonProcessingException {
        TracingInfo tracing = replicaClient.getTracingInfo();
        for (ConfigurationSetting setting : settings) {
            if (trimStrings == null && StringUtils.hasText(keyFilter)) {
                trimStrings = new ArrayList<>();
                trimStrings.add(keyFilter.substring(0, keyFilter.length() - 1));
            }

            String key = trimKey(setting.getKey(), trimStrings);

            if (setting instanceof SecretReferenceConfigurationSetting) {
                String entry = getKeyVaultEntry((SecretReferenceConfigurationSetting) setting);

                // Null in the case of failFast is false, will just skip entry.
                if (entry != null) {
                    properties.put(key, entry);
                }
            } else if (snapshot && setting instanceof FeatureFlagConfigurationSetting
                && FEATURE_FLAG_CONTENT_TYPE.equals(setting.getContentType())) {
                // Feature Flags are only part of this if they come from a snapshot
                featureConfigurationSettings.add(setting);
                FeatureFlagConfigurationSetting featureFlag = (FeatureFlagConfigurationSetting) setting;

                String configName = FEATURE_MANAGEMENT_KEY
                    + setting.getKey().trim().substring(FEATURE_FLAG_PREFIX.length());

                updateTelemetry(featureFlag, tracing);

                properties.put(configName, createFeature(featureFlag));
            } else if (StringUtils.hasText(setting.getContentType())
                && JsonConfigurationParser.isJsonContentType(setting.getContentType())) {
                Map<String, Object> jsonSettings = JsonConfigurationParser.parseJsonSetting(setting);
                for (Entry<String, Object> jsonSetting : jsonSettings.entrySet()) {
                    key = trimKey(jsonSetting.getKey(), trimStrings);
                    properties.put(key, jsonSetting.getValue());
                }
            } else {
                properties.put(key, setting.getValue());
            }
        }
    }

    private String trimKey(String key, List<String> trimStrings) {
        key = key.trim();
        if (trimStrings != null) {
            for (String trim : trimStrings) {
                if (key.startsWith(trim)) {
                    return key.replaceFirst("^" + trim, "").replace('/', '.');
                }
            }
        }
        return key.replace("/", ".");
    }

    /**
     * Given a Setting's Key Vault Reference stored in the Settings value, it will get its entry in Key Vault.
     *
     * @param secretReference {"uri": "&lt;your-vault-url&gt;/secret/&lt;secret&gt;/&lt;version&gt;"}
     * @return Key Vault Secret Value
     */
    private String getKeyVaultEntry(SecretReferenceConfigurationSetting secretReference) {
        String secretValue = null;
        try {
            URI uri = null;
            KeyVaultSecret secret = null;

            // Parsing Key Vault Reference for URI
            try {
                uri = new URI(secretReference.getSecretId());
                secret = keyVaultClientFactory.getClient("https://" + uri.getHost()).getSecret(uri, maxRetryTime);
            } catch (URISyntaxException e) {
                LOGGER.error("Error Processing Key Vault Entry URI.");
                ReflectionUtils.rethrowRuntimeException(e);
            }

            if (secret == null) {
                throw new IOException("No Key Vault Secret found for Reference.");
            }
            secretValue = secret.getValue();
        } catch (RuntimeException | IOException e) {
            LOGGER.error("Error Retrieving Key Vault Entry");
            ReflectionUtils.rethrowRuntimeException(e);
        }
        return secretValue;
    }
}
