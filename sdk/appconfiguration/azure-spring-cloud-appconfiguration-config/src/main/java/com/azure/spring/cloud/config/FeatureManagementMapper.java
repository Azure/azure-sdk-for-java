// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import static com.azure.spring.cloud.config.AppConfigurationConstants.FEATURE_MANAGEMENT_KEY_V1;
import static com.azure.spring.cloud.config.AppConfigurationConstants.FEATURE_MANAGEMENT_KEY_V2;
import static com.azure.spring.cloud.config.AppConfigurationConstants.FEATURE_MANAGEMENT_V1_SCHEMA;
import static com.azure.spring.cloud.config.AppConfigurationConstants.FEATURE_MANAGEMENT_V2_SCHEMA;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.spring.cloud.config.feature.management.entity.DynamicFeature;
import com.azure.spring.cloud.config.feature.management.entity.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;

class FeatureManagementMapper {

    private static final String USERS = "users";

    private static final String USERS_CAPS = "Users";

    private static final String AUDIENCE = "Audience";

    private static final String GROUPS = "groups";

    private static final String GROUPS_CAPS = "Groups";

    private static final String TARGETING_FILTER = "targetingFilter";

    private static final String DEFAULT_ROLLOUT_PERCENTAGE = "defaultRolloutPercentage";

    private static final String DEFAULT_ROLLOUT_PERCENTAGE_CAPS = "DefaultRolloutPercentage";

    private static final ObjectMapper FEATURE_MAPPER = JsonMapper.builder()
        .propertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE).build();

    private static final ObjectMapper CASE_INSENSITIVE_MAPPER = JsonMapper.builder()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();

    DynamicFeature createDynamicFeature(ConfigurationSetting item)
        throws JsonMappingException, JsonProcessingException {
        DynamicFeature dynamicFeature = FEATURE_MAPPER.readValue(item.getValue(), DynamicFeature.class);

        dynamicFeature.getVariants().values().forEach(
            variant -> variant.setAssignmentParameters(convertTargeting(variant.getAssignmentParameters())));

        return dynamicFeature;
    }

    /**
     * Creates a {@code Feature} from a {@code KeyValueItem}
     *
     * @param item Used to create Features before being converted to be set into properties.
     * @return Feature created from KeyValueItem
     */
    Object createFeature(FeatureFlagConfigurationSetting item) {
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

    int getFeatureSchemaVersion() {
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

    String getFeatureSchema() {
        int version = getFeatureSchemaVersion();

        if (version == FEATURE_MANAGEMENT_V1_SCHEMA) {
            return FEATURE_MANAGEMENT_KEY_V1;
        } else {
            return FEATURE_MANAGEMENT_KEY_V2;
        }
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

    private static List<Object> convertToListOrEmptyList(Map<String, Object> parameters, String key) {
        List<Object> listObjects = CASE_INSENSITIVE_MAPPER.convertValue(parameters.get(key),
            new TypeReference<List<Object>>() {
            });
        return listObjects == null ? emptyList() : listObjects;
    }

}
