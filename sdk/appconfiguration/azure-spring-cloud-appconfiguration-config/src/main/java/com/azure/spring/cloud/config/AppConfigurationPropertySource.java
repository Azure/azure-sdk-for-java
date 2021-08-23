// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import static com.azure.spring.cloud.config.Constants.FEATURE_FLAG_PREFIX;
import static com.azure.spring.cloud.config.Constants.FEATURE_MANAGEMENT_KEY;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.spring.cloud.config.feature.management.entity.Feature;
import com.azure.spring.cloud.config.feature.management.entity.FeatureSet;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreSelects;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.stores.ClientStore;
import com.azure.spring.cloud.config.stores.KeyVaultClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

/**
 * Azure App Configuration PropertySource unique per Store Label(Profile) combo.
 * 
 * <p>
 * i.e. If connecting to 2 stores and have 2 labels set 4 AppConfigurationPropertySources need to be created.
 * </p>
 */
public class AppConfigurationPropertySource extends EnumerablePropertySource<ConfigurationClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationPropertySource.class);

    private static final String USERS = "users";

    private static final String USERS_CAPS = "Users";

    private static final String AUDIENCE = "Audience";

    private static final String GROUPS = "groups";

    private static final String GROUPS_CAPS = "Groups";

    private static final String TARGETING_FILTER = "targetingFilter";

    private static final String DEFAULT_ROLLOUT_PERCENTAGE = "defaultRolloutPercentage";

    private static final String DEFAULT_ROLLOUT_PERCENTAGE_CAPS = "DefaultRolloutPercentage";

    private static final ObjectMapper CASE_INSENSITIVE_MAPPER = new ObjectMapper()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

    private final String context;

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

    AppConfigurationPropertySource(String context, ConfigStore configStore, AppConfigurationStoreSelects selectedKeys, List<String> profiles,
        AppConfigurationProperties appConfigurationProperties, ClientStore clients,
        AppConfigurationProviderProperties appProperties, KeyVaultCredentialProvider keyVaultCredentialProvider,
        SecretClientBuilderSetup keyVaultClientProvider, KeyVaultSecretProvider keyVaultSecretProvider) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely define a PropertySource
        super(context + configStore.getEndpoint() + "/" + selectedKeys.getLabel());
        this.context = context;
        this.configStore = configStore;
        this.selectedKeys = selectedKeys;
        this.profiles = profiles;
        this.appConfigurationProperties = appConfigurationProperties;
        this.appProperties = appProperties;
        this.keyVaultClients = new HashMap<String, KeyVaultClient>();
        this.clients = clients;
        this.keyVaultCredentialProvider = keyVaultCredentialProvider;
        this.keyVaultClientProvider = keyVaultClientProvider;
        this.keyVaultSecretProvider = keyVaultSecretProvider;
    }

    private static List<Object> convertToListOrEmptyList(Map<String, Object> parameters, String key) {
        List<Object> listObjects = CASE_INSENSITIVE_MAPPER.convertValue(
            parameters.get(key),
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
     * Feature Management, but make sure its done in the last {@code
     * AppConfigurationPropertySource}
     * </p>
     *
     * @param featureSet The set of Feature Management Flags from various config stores.
     * @return Updated Feature Set from Property Source
     * @throws IOException Thrown when processing key/value failed when reading feature flags
     */
    FeatureSet initProperties(FeatureSet featureSet) throws IOException {
        String storeName = configStore.getEndpoint();
        Date date = new Date();
        SettingSelector settingSelector = new SettingSelector();
        
        List<ConfigurationSetting> features = new ArrayList<>();
        // Reading In Features
        if (configStore.getFeatureFlags().getEnabled()) {
            settingSelector.setKeyFilter(configStore.getFeatureFlags().getKeyFilter())
                .setLabelFilter(configStore.getFeatureFlags().getLabelFilter());
            features = clients.listSettings(settingSelector, storeName);

            if (features == null) {
                throw new IOException("Unable to load properties from App Configuration Store.");
            }
        }
        
        List<String> labels = Arrays.asList(selectedKeys.getLabelFilter(profiles));
        Collections.reverse(labels);

        for (String label: labels) {
            settingSelector = new SettingSelector().setKeyFilter(selectedKeys.getKeyFilter()).setLabelFilter(label);

            // * for wildcard match
            settingSelector.setKeyFilter(context + "*");
            List<ConfigurationSetting> settings = clients.listSettings(settingSelector, storeName);
            
            if (settings == null) {
                throw new IOException("Unable to load properties from App Configuration Store.");
            }
            
            for (ConfigurationSetting setting : settings) {
                String key = setting.getKey().trim().substring(context.length()).replace('/', '.');
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
                        key = jsonSetting.getKey().trim().substring(context.length());
                        properties.put(key, jsonSetting.getValue());
                    }
                } else {
                    properties.put(key, setting.getValue());
                }
            }
        }
        return addToFeatureSet(featureSet, features, date);
    }

    /**
     * Given a Setting's Key Vault Reference stored in the Settings value, it will get its entry in Key Vault.
     *
     * @param value {"uri": "&lt;your-vault-url&gt;/secret/&lt;secret&gt;/&lt;version&gt;"}
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
            LOGGER.error("Error Retreiving Key Vault Entry");
            ReflectionUtils.rethrowRuntimeException(e);
        }
        return secretValue;
    }

    /**
     * Initializes Feature Management configurations. Only one {@code AppConfigurationPropertySource} can call this, and
     * it needs to be done after the rest have run initProperties.
     *
     * @param featureSet Feature Flag info to be set to this property source.
     */
    void initFeatures(FeatureSet featureSet) {
        ObjectMapper featureMapper = new ObjectMapper();
        featureMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
        properties.put(FEATURE_MANAGEMENT_KEY,
            featureMapper.convertValue(featureSet.getFeatureManagement(), LinkedHashMap.class));
    }

    private FeatureSet addToFeatureSet(FeatureSet featureSet, List<ConfigurationSetting> settings, Date date)
        throws IOException {
        // Reading In Features
        for (ConfigurationSetting setting : settings) {
            if (setting instanceof FeatureFlagConfigurationSetting) {
                Object feature = createFeature((FeatureFlagConfigurationSetting) setting);
                if (feature != null) {
                    featureSet.addFeature(setting.getKey().trim().substring(FEATURE_FLAG_PREFIX.length()), feature);
                }
            }
        }
        return featureSet;
    }

    /**
     * Creates a {@code Feature} from a {@code KeyValueItem}
     *
     * @param item Used to create Features before being converted to be set into properties.
     * @return Feature created from KeyValueItem
     * @throws IOException - If a ConfigurationSetting isn't of the feature flag content type.
     */
    @SuppressWarnings("unchecked")
    private Object createFeature(FeatureFlagConfigurationSetting item) throws IOException {
        String key = getFeatureSimpleName(item);
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
        for (int filter = 0; filter < feature.getEnabledFor().size(); filter++) {
            FeatureFlagFilter featureFilterEvaluationContext = featureEnabledFor.get(filter);
            Map<String, Object> parameters = featureFilterEvaluationContext.getParameters();

            if (parameters == null || !TARGETING_FILTER.equals(featureEnabledFor.get(filter).getName())) {
                continue;
            }

            Object audienceObject = parameters.get(AUDIENCE);
            if (audienceObject != null) {
                parameters = (Map<String, Object>) audienceObject;
            }

            List<Object> users = convertToListOrEmptyList(parameters, USERS_CAPS);
            List<Object> groupRollouts = convertToListOrEmptyList(parameters, GROUPS_CAPS);

            switchKeyValues(parameters, USERS_CAPS, USERS, mapValuesByIndex(users));
            switchKeyValues(parameters, GROUPS_CAPS, GROUPS, mapValuesByIndex(groupRollouts));
            switchKeyValues(parameters, DEFAULT_ROLLOUT_PERCENTAGE_CAPS, DEFAULT_ROLLOUT_PERCENTAGE,
                parameters.get(DEFAULT_ROLLOUT_PERCENTAGE_CAPS));

            featureFilterEvaluationContext.setParameters(parameters);
            featureEnabledFor.put(filter, featureFilterEvaluationContext);
            feature.setEnabledFor(featureEnabledFor);
        }
        return feature;

    }

    private String getFeatureSimpleName(ConfigurationSetting setting) {
        return setting.getKey().trim().substring(FEATURE_FLAG_PREFIX.length());
    }

    private Map<String, Object> mapValuesByIndex(List<Object> users) {
        return IntStream.range(0, users.size()).boxed().collect(toMap(String::valueOf, users::get));
    }

    private void switchKeyValues(Map<String, Object> parameters, String oldKey, String newKey, Object value) {
        parameters.put(newKey, value);
        parameters.remove(oldKey);
    }
}
