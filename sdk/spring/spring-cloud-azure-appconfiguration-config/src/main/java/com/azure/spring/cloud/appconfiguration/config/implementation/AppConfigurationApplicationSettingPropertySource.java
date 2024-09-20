// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_CONTENT_TYPE;

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
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

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

    private final String[] labelFilters;

    AppConfigurationApplicationSettingPropertySource(String name, AppConfigurationReplicaClient replicaClient,
        AppConfigurationKeyVaultClientFactory keyVaultClientFactory, String keyFilter, String[] labelFilters) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely define a PropertySource
        super(name + getLabelName(labelFilters), replicaClient);
        this.keyVaultClientFactory = keyVaultClientFactory;
        this.keyFilter = keyFilter;
        this.labelFilters = labelFilters;
    }

    /**
     * <p>
     * Gets settings from Azure/Cache to set as configurations. Updates the cache.
     * </p>
     *
     * @param keyPrefixTrimValues prefixs to trim from key values
     * @throws InvalidConfigurationPropertyValueException thrown if fails to parse Json content type
     */
    public void initProperties(List<String> keyPrefixTrimValues, boolean isRefresh) throws InvalidConfigurationPropertyValueException {

        List<String> labels = Arrays.asList(labelFilters);
        // Reverse labels so they have the right priority order.
        Collections.reverse(labels);

        for (String label : labels) {
            SettingSelector settingSelector = new SettingSelector().setKeyFilter(keyFilter + "*").setLabelFilter(label);

            // * for wildcard match
            processConfigurationSettings(replicaClient.listSettings(settingSelector, isRefresh), settingSelector.getKeyFilter(),
                keyPrefixTrimValues);
        }
    }

    protected void processConfigurationSettings(List<ConfigurationSetting> settings, String keyFilter,
        List<String> keyPrefixTrimValues)
        throws InvalidConfigurationPropertyValueException {
        for (ConfigurationSetting setting : settings) {
            if (keyPrefixTrimValues == null && StringUtils.hasText(keyFilter)) {
                keyPrefixTrimValues = new ArrayList<>();
                keyPrefixTrimValues.add(keyFilter.substring(0, keyFilter.length() - 1));
            }
            String key = trimKey(setting.getKey(), keyPrefixTrimValues);

            if (setting instanceof SecretReferenceConfigurationSetting) {
                handleKeyVaultReference(key, (SecretReferenceConfigurationSetting) setting);
            } else if (setting instanceof FeatureFlagConfigurationSetting
                && FEATURE_FLAG_CONTENT_TYPE.equals(setting.getContentType())) {
                handleFeatureFlag(key, (FeatureFlagConfigurationSetting) setting, keyPrefixTrimValues);
            } else if (StringUtils.hasText(setting.getContentType())
                && JsonConfigurationParser.isJsonContentType(setting.getContentType())) {
                handleJson(setting, keyPrefixTrimValues);
            } else {
                properties.put(key, setting.getValue());
            }
        }
    }

    /**
     * Given a Setting's Key Vault Reference stored in the Settings value, it will get its entry in Key Vault.
     *
     * @param key Application Setting name
     * @param secretReference {"uri": "&lt;your-vault-url&gt;/secret/&lt;secret&gt;/&lt;version&gt;"}
     * @return Key Vault Secret Value
     * @throws InvalidConfigurationPropertyValueException
     */
    protected void handleKeyVaultReference(String key, SecretReferenceConfigurationSetting secretReference)
        throws InvalidConfigurationPropertyValueException {
        // Parsing Key Vault Reference for URI
        try {
            URI uri = new URI(secretReference.getSecretId());
            KeyVaultSecret secret = keyVaultClientFactory.getClient("https://" + uri.getHost()).getSecret(uri);
            properties.put(key, secret.getValue());
        } catch (URISyntaxException e) {
            LOGGER.error(String.format("Error Retrieving Key Vault Entry for key %s.", key));
            throw new InvalidConfigurationPropertyValueException(key, "<Redacted>",
                "Invalid URI found in JSON property field 'uri' unable to parse.");
        } catch (RuntimeException e) {
            LOGGER.error(String.format("Error Retrieving Key Vault Entry for key %s.", key));
            throw e;
        }
    }

    void handleFeatureFlag(String key, FeatureFlagConfigurationSetting setting, List<String> trimStrings)
        throws InvalidConfigurationPropertyValueException {
        handleJson(setting, trimStrings);
    }

    void handleJson(ConfigurationSetting setting, List<String> keyPrefixTrimValues)
        throws InvalidConfigurationPropertyValueException {
        Map<String, Object> jsonSettings = JsonConfigurationParser.parseJsonSetting(setting);
        for (Entry<String, Object> jsonSetting : jsonSettings.entrySet()) {
            String key = trimKey(jsonSetting.getKey(), keyPrefixTrimValues);
            properties.put(key, jsonSetting.getValue());
        }
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
