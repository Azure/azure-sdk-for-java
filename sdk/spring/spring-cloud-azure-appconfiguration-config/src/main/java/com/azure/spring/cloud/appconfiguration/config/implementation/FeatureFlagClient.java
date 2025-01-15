// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.CONDITIONS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.DEFAULT_REQUIREMENT_TYPE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.E_TAG;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_CONTENT_TYPE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_ID;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_PREFIX;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_REFERENCE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.REQUIREMENT_TYPE_SERVICE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.SELECT_ALL_FEATURE_FLAGS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.TELEMETRY;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.FeatureFlags;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.Feature;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.FeatureTelemetry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.nimbusds.jose.util.Base64URL;

/**
 * Loads sets of feature flags, and de-duplicates the results with previously loaded feature flags. Newer Feature Flags
 * take priority.
 */
@Component
public class FeatureFlagClient {

    protected final Map<String, Feature> properties = new LinkedHashMap<>();

    private static final ObjectMapper CASE_INSENSITIVE_MAPPER = JsonMapper.builder()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();

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
    public List<FeatureFlags> loadFeatureFlags(AppConfigurationReplicaClient replicaClient, String customKeyFilter,
        String[] labelFilter, boolean isRefresh) {
        List<FeatureFlags> loadedFeatureFlags = new ArrayList<>();

        String keyFilter = SELECT_ALL_FEATURE_FLAGS;

        if (StringUtils.hasText(customKeyFilter)) {
            keyFilter = FEATURE_FLAG_PREFIX + customKeyFilter;
        }

        List<String> labels = Arrays.asList(labelFilter);
        Collections.reverse(labels);

        for (String label : labels) {
            SettingSelector settingSelector = new SettingSelector().setKeyFilter(keyFilter).setLabelFilter(label);

            FeatureFlags features = replicaClient.listFeatureFlags(settingSelector, isRefresh);
            loadedFeatureFlags.addAll(proccessFeatureFlags(features, keyFilter));
        }
        return loadedFeatureFlags;
    }

    public List<FeatureFlags> proccessFeatureFlags(FeatureFlags features, String endpoint) {
        List<FeatureFlags> loadedFeatureFlags = new ArrayList<>();
        loadedFeatureFlags.add(features);

        // Reading In Features
        for (ConfigurationSetting setting : features.getFeatureFlags()) {
            if (setting instanceof FeatureFlagConfigurationSetting
                && FEATURE_FLAG_CONTENT_TYPE.equals(setting.getContentType())) {
                FeatureFlagConfigurationSetting featureFlag = (FeatureFlagConfigurationSetting) setting;
                properties.put(featureFlag.getKey(), createFeature(featureFlag, endpoint));
            }
        }
        return loadedFeatureFlags;
    }

    /**
     * Creates a {@code Feature} from a {@code KeyValueItem}
     *
     * @param item Used to create Features before being converted to be set into properties.
     * @return Feature created from KeyValueItem
     */
    protected static Feature createFeature(FeatureFlagConfigurationSetting item, String originEndpoint) {
        String requirementType = DEFAULT_REQUIREMENT_TYPE;
        FeatureTelemetry featureTelemetry = new FeatureTelemetry();
        try {
            JsonNode node = CASE_INSENSITIVE_MAPPER.readTree(item.getValue());
            JsonNode conditions = node.get(CONDITIONS);
            if (conditions != null && conditions.get(REQUIREMENT_TYPE_SERVICE) != null) {
                requirementType = conditions.get(REQUIREMENT_TYPE_SERVICE).asText();
            }
            JsonNode telemetryNode = node.get(TELEMETRY);
            if (telemetryNode != null) {
                ObjectMapper objectMapper = JsonMapper.builder()
                    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();
                featureTelemetry = objectMapper.convertValue(telemetryNode, FeatureTelemetry.class);
            }
        } catch (JsonProcessingException e) {

        }

        Feature feature = new Feature(item, requirementType, featureTelemetry);

        if (feature.getTelemetry() != null) {
            final FeatureTelemetry telemetry = feature.getTelemetry();
            if (telemetry.isEnabled()) {
                final Map<String, String> originMetadata = telemetry.getMetadata();
                originMetadata.put(FEATURE_FLAG_ID, calculateFeatureFlagId(item.getKey(), item.getLabel()));
                originMetadata.put(E_TAG, item.getETag());
                if (originEndpoint != null && !originEndpoint.isEmpty()) {
                    final String labelPart = item.getLabel().isEmpty() ? ""
                        : String.format("?label=%s", item.getLabel());
                    originMetadata.put(FEATURE_FLAG_REFERENCE,
                        String.format("%s/kv/%s%s", originEndpoint, item.getKey(), labelPart));
                }
            }
        }
        return feature;
    }

    /**
     * @param key the key of feature flag
     * @param label the label of feature flag. If label is whitespace, treat as null
     * @return base64_url(SHA256(utf8_bytes("${key}\n${label}"))).replace('+', '-').replace('/', '_').trimEnd('=')
     * trimEnd() means trims everything after the first occurrence of the '='
     */
    private static String calculateFeatureFlagId(String key, String label) {
        final String data = String.format("%s\n%s", key, label.isEmpty() ? null : label);
        final SHA256.Digest digest = new SHA256.Digest();
        final String beforeTrim = Base64URL.encode(digest.digest(data.getBytes(StandardCharsets.UTF_8)))
            .toString().replace('+', '-').replace('/', '_');
        final int index = beforeTrim.indexOf('=');
        return beforeTrim.substring(0, index > -1 ? index : beforeTrim.length());
    }

    /**
     * @return the properties
     */
    public Map<String, Feature> getProperties() {
        return properties;
    }

}
