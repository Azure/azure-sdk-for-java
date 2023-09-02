// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.AUDIENCE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.DEFAULT_REQUIREMENT_TYPE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.DEFAULT_ROLLOUT_PERCENTAGE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.DEFAULT_ROLLOUT_PERCENTAGE_CAPS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_PREFIX;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.GROUPS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.GROUPS_CAPS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.REQUIREMENT_TYPE_SERVICE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.TARGETING_FILTER;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.USERS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.USERS_CAPS;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.springframework.core.env.EnumerablePropertySource;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.Feature;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.TracingInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Azure App Configuration PropertySource unique per Store Label(Profile) combo.
 *
 * <p>
 * i.e. If connecting to 2 stores and have 2 labels set 4 AppConfigurationPropertySources need to be created.
 * </p>
 */
abstract class AppConfigurationPropertySource extends EnumerablePropertySource<ConfigurationClient> {

    private static final ObjectMapper CASE_INSENSITIVE_MAPPER = JsonMapper.builder()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();

    protected final Map<String, Object> properties = new LinkedHashMap<>();

    protected final List<ConfigurationSetting> featureConfigurationSettings = new ArrayList<>();

    protected final AppConfigurationReplicaClient replicaClient;

    AppConfigurationPropertySource(String name, AppConfigurationReplicaClient replicaClient) {
        // The context alone does not uniquely define a PropertySource, append storeName
        // and label to uniquely define a PropertySource
        super(name);
        this.replicaClient = replicaClient;
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

    protected static String getLabelName(String[] labelFilter) {
        if (labelFilter == null) {
            return "";
        }
        StringBuilder labelName = new StringBuilder();
        for (String label : labelFilter) {

            labelName.append((labelName.length() == 0) ? label : "," + label);
        }
        return labelName.toString();
    }

    List<ConfigurationSetting> getFeatureFlagSettings() {
        return featureConfigurationSettings;
    }

    /**
     * Creates a {@code Feature} from a {@code KeyValueItem}
     *
     * @param item Used to create Features before being converted to be set into properties.
     * @return Feature created from KeyValueItem
     */
    @SuppressWarnings("unchecked")
    protected Object createFeature(FeatureFlagConfigurationSetting item) {
        String key = getFeatureSimpleName(item);
        String requirementType = DEFAULT_REQUIREMENT_TYPE;
        try {
            JsonNode node = CASE_INSENSITIVE_MAPPER.readTree(item.getValue());
            JsonNode conditions = node.get("conditions");
            if (conditions != null && conditions.get(REQUIREMENT_TYPE_SERVICE) != null) {
                requirementType = conditions.get(REQUIREMENT_TYPE_SERVICE).asText();
            }
        } catch (JsonProcessingException e) {

        }
        Feature feature = new Feature(key, item, requirementType);
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

    /**
     * Looks at each filter used in a Feature Flag to check what types it is using.
     * 
     * @param featureFlag FeatureFlagConfigurationSetting
     * @param tracing The TracingInfo for this store.
     */
    protected void updateTelemetry(FeatureFlagConfigurationSetting featureFlag, TracingInfo tracing) {
        for (FeatureFlagFilter filter : featureFlag.getClientFilters()) {
            tracing.getFeatureFlagTracing().updateFeatureFilterTelemetry(filter.getName());
        }
    }

    private String getFeatureSimpleName(ConfigurationSetting setting) {
        return setting.getKey().trim().substring(FEATURE_FLAG_PREFIX.length());
    }

    @SuppressWarnings("null")
    private Map<String, Object> mapValuesByIndex(List<Object> users) {
        return IntStream.range(0, users.size()).boxed().collect(toMap(String::valueOf, users::get));
    }

    private void switchKeyValues(Map<String, Object> parameters, String oldKey, String newKey, Object value) {
        parameters.put(newKey, value);
        parameters.remove(oldKey);
    }

    private static List<Object> convertToListOrEmptyList(Map<String, Object> parameters, String key) {
        List<Object> listObjects = CASE_INSENSITIVE_MAPPER.convertValue(parameters.get(key),
            new TypeReference<List<Object>>() {
            });
        return listObjects == null ? emptyList() : listObjects;
    }

    protected abstract void initProperties(List<String> trim) throws JsonProcessingException;
}
