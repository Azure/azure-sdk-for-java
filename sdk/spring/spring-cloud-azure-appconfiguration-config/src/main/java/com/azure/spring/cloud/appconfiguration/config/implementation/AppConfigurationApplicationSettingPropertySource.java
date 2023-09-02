// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_CONTENT_TYPE;

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
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Azure App Configuration PropertySource unique per Store Label(Profile) combo.
 *
 * <p>
 * i.e. If connecting to 2 stores and have 2 labels set 4 AppConfigurationPropertySources need to be created.
 * </p>
 */
class AppConfigurationApplicationSettingPropertySource extends AppConfigurationPropertySource {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AppConfigurationApplicationSettingPropertySource.class);

    private final AppConfigurationKeyVaultClientFactory keyVaultClientFactory;

    private final String keyFilter;

    private final String[] labelFilter;

    AppConfigurationApplicationSettingPropertySource(String name, AppConfigurationReplicaClient replicaClient,
        AppConfigurationKeyVaultClientFactory keyVaultClientFactory, String keyFilter, String[] labelFilter) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely define a PropertySource
        super(name + getLabelName(labelFilter), replicaClient);
        this.keyVaultClientFactory = keyVaultClientFactory;
        this.keyFilter = keyFilter;
        this.labelFilter = labelFilter;
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

        List<String> labels = Arrays.asList(labelFilter);
        Collections.reverse(labels);

        for (String label : labels) {
            SettingSelector settingSelector = new SettingSelector().setKeyFilter(keyFilter + "*").setLabelFilter(label);

            // * for wildcard match
            processConfigurationSettings(replicaClient.listSettings(settingSelector), settingSelector.getKeyFilter(),
                trim, keyVaultClientFactory);
        }

    }

    protected void processConfigurationSettings(List<ConfigurationSetting> settings, String keyFilter,
        List<String> trimStrings, AppConfigurationKeyVaultClientFactory keyVaultClientFactory)
        throws JsonProcessingException {
        for (ConfigurationSetting setting : settings) {
            if (trimStrings == null && StringUtils.hasText(keyFilter)) {
                trimStrings = new ArrayList<>();
                trimStrings.add(keyFilter.substring(0, keyFilter.length() - 1));
            }
            String key = trimKey(setting.getKey(), trimStrings);

            if (setting instanceof SecretReferenceConfigurationSetting) {
                handleKeyVaultReference(key, (SecretReferenceConfigurationSetting) setting);
            } else if (setting instanceof FeatureFlagConfigurationSetting
                && FEATURE_FLAG_CONTENT_TYPE.equals(setting.getContentType())) {
                handleFeatureFlag(key, (FeatureFlagConfigurationSetting) setting, trimStrings);
            } else if (StringUtils.hasText(setting.getContentType())
                && JsonConfigurationParser.isJsonContentType(setting.getContentType())) {
                handleJson(setting, trimStrings);
            } else {
                properties.put(key, setting.getValue());
            }
        }
    }

    void handleKeyVaultReference(String key, SecretReferenceConfigurationSetting setting) {
        String entry = getKeyVaultEntry(keyVaultClientFactory, setting);

        // Null in the case of failFast is false, will just skip entry.
        if (entry != null) {
            properties.put(key, entry);
        }
    }

    void handleFeatureFlag(String key, FeatureFlagConfigurationSetting setting, List<String> trimStrings)
        throws JsonProcessingException {
        handleJson(setting, trimStrings);
    }

    void handleJson(ConfigurationSetting setting, List<String> trimStrings)
        throws JsonProcessingException {
        Map<String, Object> jsonSettings = JsonConfigurationParser.parseJsonSetting(setting);
        for (Entry<String, Object> jsonSetting : jsonSettings.entrySet()) {
            String key = trimKey(jsonSetting.getKey(), trimStrings);
            properties.put(key, jsonSetting.getValue());
        }
    }

    /**
     * Given a Setting's Key Vault Reference stored in the Settings value, it will get its entry in Key Vault.
     *
     * @param secretReference {"uri": "&lt;your-vault-url&gt;/secret/&lt;secret&gt;/&lt;version&gt;"}
     * @return Key Vault Secret Value
     */
    protected String getKeyVaultEntry(AppConfigurationKeyVaultClientFactory keyVaultClientFactory,
        SecretReferenceConfigurationSetting secretReference) {
        String secretValue = null;
        try {
            URI uri = null;
            KeyVaultSecret secret = null;

            // Parsing Key Vault Reference for URI
            try {
                uri = new URI(secretReference.getSecretId());
                secret = keyVaultClientFactory.getClient("https://" + uri.getHost()).getSecret(uri);
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

    protected String trimKey(String key, List<String> trimStrings) {
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
}
