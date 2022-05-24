// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import static com.azure.spring.cloud.config.AppConfigurationConstants.DYNAMIC_FEATURE_CONTENT_TYPE;
import static com.azure.spring.cloud.config.AppConfigurationConstants.DYNAMIC_FEATURE_KEY;
import static com.azure.spring.cloud.config.AppConfigurationConstants.FEATURE_MANAGEMENT_KEY_V1;
import static com.azure.spring.cloud.config.AppConfigurationConstants.FEATURE_MANAGEMENT_KEY_V2;
import static com.azure.spring.cloud.config.AppConfigurationConstants.FEATURE_MANAGEMENT_V1_SCHEMA;
import static com.azure.spring.cloud.config.AppConfigurationConstants.FEATURE_MANAGEMENT_V2_SCHEMA;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.spring.cloud.config.feature.management.entity.DynamicFeature;
import com.azure.spring.cloud.config.feature.management.entity.Feature;
import com.azure.spring.cloud.config.feature.management.entity.FeatureSet;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreSelects;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.stores.ClientStore;
import com.azure.spring.cloud.config.stores.KeyVaultClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Azure App Configuration PropertySource unique per Store Label(Profile) combo.
 *
 * <p>
 * i.e. If connecting to 2 stores and have 2 labels set 4 AppConfigurationPropertySources need to be created.
 * </p>
 */
public final class AppConfigurationPropertySource extends EnumerablePropertySource<ConfigurationClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationPropertySource.class);

    private static final String USERS = "users";

    private static final String USERS_CAPS = "Users";

    private static final String AUDIENCE = "Audience";

    private static final String GROUPS = "groups";

    private static final String GROUPS_CAPS = "Groups";

    private static final String TARGETING_FILTER = "targetingFilter";

    private static final String DEFAULT_ROLLOUT_PERCENTAGE = "defaultRolloutPercentage";

    private static final String DEFAULT_ROLLOUT_PERCENTAGE_CAPS = "DefaultRolloutPercentage";

    private static final ObjectMapper CASE_INSENSITIVE_MAPPER = JsonMapper.builder()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();

    private static final ObjectMapper FEATURE_MAPPER = JsonMapper.builder()
        .propertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE).build();

    private final AppConfigurationStoreSelects selectedKeys;

    private final List<String> profiles;

    private final Map<String, Object> properties = new LinkedHashMap<>();

    private final AppConfigurationProperties appConfigurationProperties;

    private final HashMap<String, KeyVaultClient> keyVaultClients;

    private final ClientStore clients;

    private final KeyVaultCredentialProvider keyVaultCredentialProvider;

    private final SecretClientBuilderSetup keyVaultClientProvider;

    private final KeyVaultSecretProvider keyVaultSecretProvider;

    private final AppConfigurationProviderProperties appProperties;

    private final ConfigStore configStore;

    AppConfigurationPropertySource(ConfigStore configStore, AppConfigurationStoreSelects selectedKeys,
        List<String> profiles, AppConfigurationProperties appConfigurationProperties, ClientStore clients,
        AppConfigurationProviderProperties appProperties, KeyVaultCredentialProvider keyVaultCredentialProvider,
        SecretClientBuilderSetup keyVaultClientProvider, KeyVaultSecretProvider keyVaultSecretProvider) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely define a PropertySource
        super(
            selectedKeys.getKeyFilter() + configStore.getEndpoint() + "/" + selectedKeys.getLabelFilterText(profiles));
        this.configStore = configStore;
        this.selectedKeys = selectedKeys;
        this.profiles = profiles;
        this.appConfigurationProperties = appConfigurationProperties;
        this.appProperties = appProperties;
        this.keyVaultClients = new HashMap<>();
        this.clients = clients;
        this.keyVaultCredentialProvider = keyVaultCredentialProvider;
        this.keyVaultClientProvider = keyVaultClientProvider;
        this.keyVaultSecretProvider = keyVaultSecretProvider;
    }

    private static List<Object> convertToListOrEmptyList(Map<String, Object> parameters, String key) {
        List<Object> listObjects = CASE_INSENSITIVE_MAPPER.convertValue(parameters.get(key),
            new TypeReference<List<Object>>() {
            });
        return listObjects == null ? emptyList() : listObjects;
    }

    @Override
    public String[] getPropertyNames() {
        Set<String> keySet = properties.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
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
     * @param featureSet The set of Feature Management Flags from various config stores.
     * @return Updated Feature Set from Property Source
     * @throws IOException Thrown when processing key/value failed when reading feature flags
     */
    void initProperties(FeatureSet featureSet) throws IOException {
        String storeName = configStore.getEndpoint();
        SettingSelector settingSelector = new SettingSelector();

        PagedIterable<ConfigurationSetting> features = null;
        // Reading In Features
        if (configStore.getFeatureFlags().getEnabled()) {
            settingSelector.setKeyFilter(configStore.getFeatureFlags().getKeyFilter())
                .setLabelFilter(configStore.getFeatureFlags().getLabelFilter());
            features = clients.listSettings(settingSelector, storeName);

        }

        List<String> labels = Arrays.asList(selectedKeys.getLabelFilter(profiles));
        Collections.reverse(labels);

        for (String label : labels) {
            settingSelector = new SettingSelector().setKeyFilter(selectedKeys.getKeyFilter() + "*")
                .setLabelFilter(label);

            // * for wildcard match
            PagedIterable<ConfigurationSetting> settings = clients.listSettings(settingSelector, storeName);

            for (ConfigurationSetting setting : settings) {
                String key = setting.getKey().trim().substring(selectedKeys.getKeyFilter().length()).replace('/', '.');
                if (setting instanceof SecretReferenceConfigurationSetting) {
                    String entry = getKeyVaultEntry((SecretReferenceConfigurationSetting) setting);

                    // Null in the case of failFast is false, will just skip entry.
                    if (entry != null) {
                        properties.put(key, entry);
                    }
                } else if (StringUtils.hasText(setting.getContentType())
                    && JsonConfigurationParser.isJsonContentType(setting.getContentType())) {
                    HashMap<String, Object> jsonSettings = JsonConfigurationParser.parseJsonSetting(setting);
                    for (Entry<String, Object> jsonSetting : jsonSettings.entrySet()) {
                        key = jsonSetting.getKey().trim().substring(selectedKeys.getKeyFilter().length());
                        properties.put(key, jsonSetting.getValue());
                    }
                } else {
                    properties.put(key, setting.getValue());
                }
            }
        }
        addToFeatureSet(featureSet, features);
    }
    
    /**
     * Initializes Feature Management configurations. Only one {@code AppConfigurationPropertySource} can call this, and
     * it needs to be done after the rest have run initProperties.
     *
     * @param featureSet Feature Flag info to be set to this property source.
     */
    void initFeatures(FeatureSet featureSet) {
        properties.put(getFeatureSchema(),
            FEATURE_MAPPER.convertValue(featureSet.getFeatureManagement(), LinkedHashMap.class));
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

            // Parsing Key Vault Reference for URI
            try {
                uri = new URI(secretReference.getSecretId());
            } catch (URISyntaxException e) {
                LOGGER.error("Error Processing Key Vault Entry URI.");
                ReflectionUtils.rethrowRuntimeException(e);
            }

            // Check if we already have a client for this key vault, if not we will make
            // one
            if (!keyVaultClients.containsKey(uri.getHost())) {
                KeyVaultClient client = new KeyVaultClient(appConfigurationProperties, uri, keyVaultCredentialProvider,
                    keyVaultClientProvider, keyVaultSecretProvider);
                keyVaultClients.put(uri.getHost(), client);
            }
            KeyVaultSecret secret = keyVaultClients.get(uri.getHost()).getSecret(uri, appProperties.getMaxRetryTime());
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

    private FeatureSet addToFeatureSet(FeatureSet featureSet, PagedIterable<ConfigurationSetting> features)
        throws IOException {
        if (features == null) {
            return featureSet;
        }
        // Reading In Features
        for (ConfigurationSetting setting : features) {
            if (setting instanceof FeatureFlagConfigurationSetting) {
                FeatureFlagConfigurationSetting featureSetting = (FeatureFlagConfigurationSetting) setting;
                Object feature = createFeature(featureSetting);
                featureSet.addFeature(featureSetting.getFeatureId(), feature);
            } else if (DYNAMIC_FEATURE_CONTENT_TYPE.equalsIgnoreCase(setting.getContentType())
                && getFeatureSchemaVersion() >= 2) {
                DynamicFeature dynamicFeature = createDynamicFeature(setting);
                properties.put(DYNAMIC_FEATURE_KEY + dynamicFeature.getName(),
                    FEATURE_MAPPER.convertValue(dynamicFeature, LinkedHashMap.class));
            }
        }
        return featureSet;
    }

    private int getFeatureSchemaVersion() {
        String version = System
            .getenv(AppConfigurationConstants.AZURE_APP_CONFIGURATION_FEATURE_MANAGEMENT_SCHEMA_VERSION);

        switch (StringUtils.hasText(version) ? version : "") {
            case "1":
                return FEATURE_MANAGEMENT_V1_SCHEMA;
            case "2":
                return FEATURE_MANAGEMENT_V2_SCHEMA;
            default:
                return FEATURE_MANAGEMENT_V1_SCHEMA;
        }
    }

    private String getFeatureSchema() {
        int version = getFeatureSchemaVersion();

        if (version == FEATURE_MANAGEMENT_V1_SCHEMA) {
            return FEATURE_MANAGEMENT_KEY_V1;
        } else {
            return FEATURE_MANAGEMENT_KEY_V2;
        }
    }

    /**
     * Creates a {@code Feature} from a {@code KeyValueItem}
     *
     * @param item Used to create Features before being converted to be set into properties.
     * @return Feature created from KeyValueItem
     */
    private Object createFeature(FeatureFlagConfigurationSetting item) {
        String key = item.getFeatureId();
        Feature feature = new Feature(key, item);
        Map<Integer, FeatureFlagFilter> featureEnabledFor = feature.getEnabledFor();

        // Setting Enabled For to null, but enabled = true will result in the feature
        // being on. This is the case of a feature is on/off and set to on. This is to
        // tell the difference between conditional/off which looks exactly the same...
        // It should never be the case of Conditional On, and no filters coming from
        // Azure, but it is a valid way from the config file, which should result in
        // false being returned.
        if (featureEnabledFor.size() == 0 && item.isEnabled()) {
            return true;
        } else if (!item.isEnabled()) {
            return false;
        }

        feature.setEnabledFor(convertMap(featureEnabledFor));

        return feature;
    }

    private DynamicFeature createDynamicFeature(ConfigurationSetting item)
        throws JsonMappingException, JsonProcessingException {
        DynamicFeature dynamicFeature = FEATURE_MAPPER.readValue(item.getValue(), DynamicFeature.class);

        dynamicFeature.getVariants().values()
            .forEach(variant -> variant.setAssignmentParameters(convertTargeting(variant.getAssignmentParameters())));

        return dynamicFeature;
    }

    private Map<Integer, FeatureFlagFilter> convertMap(Map<Integer, FeatureFlagFilter> featureEnabledFor) {
        for (int filter = 0; filter < featureEnabledFor.size(); filter++) {
            FeatureFlagFilter featureFilterEvaluationContext = featureEnabledFor.get(filter);
            Map<String, Object> parameters = featureFilterEvaluationContext.getParameters();

            if (parameters == null || !TARGETING_FILTER.equals(featureEnabledFor.get(filter).getName())) {
                continue;
            }

            featureFilterEvaluationContext.setParameters(convertTargeting(parameters));
            featureEnabledFor.put(filter, featureFilterEvaluationContext);
        }
        return featureEnabledFor;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertTargeting(Map<String, Object> target) {

        Object audienceObject = target.get(AUDIENCE);
        if (audienceObject != null) {
            target = (Map<String, Object>) audienceObject;
        }

        List<Object> users = convertToListOrEmptyList(target, USERS_CAPS);
        List<Object> groupRollouts = convertToListOrEmptyList(target, GROUPS_CAPS);

        switchKeyValues(target, USERS_CAPS, USERS, mapValuesByIndex(users));
        switchKeyValues(target, GROUPS_CAPS, GROUPS, mapValuesByIndex(groupRollouts));
        switchKeyValues(target, DEFAULT_ROLLOUT_PERCENTAGE_CAPS, DEFAULT_ROLLOUT_PERCENTAGE,
            target.get(DEFAULT_ROLLOUT_PERCENTAGE_CAPS));

        return target;
    }

    private Map<String, Object> mapValuesByIndex(List<Object> users) {
        return IntStream.range(0, users.size()).boxed().collect(toMap(String::valueOf, users::get));
    }

    private void switchKeyValues(Map<String, Object> parameters, String oldKey, String newKey, Object value) {
        parameters.put(newKey, value);
        parameters.remove(oldKey);
    }
}
