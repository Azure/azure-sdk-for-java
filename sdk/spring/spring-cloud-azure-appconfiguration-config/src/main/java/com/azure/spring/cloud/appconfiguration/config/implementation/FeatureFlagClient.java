// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.CONDITIONS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.DEFAULT_REQUIREMENT_TYPE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.E_TAG;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_CONTENT_TYPE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_PREFIX;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_REFERENCE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.REQUIREMENT_TYPE_SERVICE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.SELECT_ALL_FEATURE_FLAGS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.TELEMETRY;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.FeatureFlags;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.Allocation;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.Feature;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.FeatureTelemetry;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.Variant;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.FeatureFlagTracing;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Loads sets of feature flags, and de-duplicates the results with previously loaded feature flags. Newer Feature Flags
 * take priority.
 */
@Component
class FeatureFlagClient {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(FeatureFlagClient.class);

    private final Map<String, Feature> properties = new LinkedHashMap<>();

    private static final ObjectMapper CASE_INSENSITIVE_MAPPER = JsonMapper.builder()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();
    
    private FeatureFlagTracing tracing = new FeatureFlagTracing();

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
    List<FeatureFlags> loadFeatureFlags(AppConfigurationReplicaClient replicaClient, String customKeyFilter,
        String[] labelFilter, Context context) {
        List<FeatureFlags> loadedFeatureFlags = new ArrayList<>();

        String keyFilter = SELECT_ALL_FEATURE_FLAGS;

        if (StringUtils.hasText(customKeyFilter)) {
            keyFilter = FEATURE_FLAG_PREFIX + customKeyFilter;
        }

        List<String> labels = Arrays.asList(labelFilter);
        Collections.reverse(labels);

        for (String label : labels) {
            SettingSelector settingSelector = new SettingSelector().setKeyFilter(keyFilter).setLabelFilter(label);
            context.addData("FeatureFlagTracing", tracing);

            FeatureFlags features = replicaClient.listFeatureFlags(settingSelector, context);
            loadedFeatureFlags.addAll(proccessFeatureFlags(features, replicaClient.getOriginClient()));
        }
        return loadedFeatureFlags;
    }

    List<FeatureFlags> proccessFeatureFlags(FeatureFlags features, String endpoint) {
        List<FeatureFlags> loadedFeatureFlags = new ArrayList<>();
        loadedFeatureFlags.add(features);

        // Reading In Features
        for (ConfigurationSetting setting : features.getFeatureFlags()) {
            if (setting instanceof FeatureFlagConfigurationSetting
                && FEATURE_FLAG_CONTENT_TYPE.equals(setting.getContentType())) {
                FeatureFlagConfigurationSetting featureFlag = (FeatureFlagConfigurationSetting) setting;
                updateTelemetry(featureFlag);
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
        FeatureTelemetry featureTelemetry = null;
        Feature feature = null;
        try {
            JsonNode node = CASE_INSENSITIVE_MAPPER.readTree(item.getValue());
            JsonNode conditions = node.get(CONDITIONS);
            if (conditions != null && conditions.get(REQUIREMENT_TYPE_SERVICE) != null) {
                requirementType = conditions.get(REQUIREMENT_TYPE_SERVICE).asText();
            }
            JsonNode telemetryNode = node.get(TELEMETRY);
            if (telemetryNode != null && !telemetryNode.isEmpty()) {
                ObjectMapper objectMapper = JsonMapper.builder()
                    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();
                featureTelemetry = objectMapper.convertValue(telemetryNode, FeatureTelemetry.class);
            }

            feature = new Feature(item, requirementType, featureTelemetry);

            // Parse variants if present
            JsonNode variantsNode = node.get("variants");
            if (variantsNode != null && variantsNode.isArray()) {
                List<Variant> variants = new ArrayList<>();
                for (JsonNode variantNode : variantsNode) {
                    Variant variant = CASE_INSENSITIVE_MAPPER.convertValue(variantNode, Variant.class);
                    variants.add(variant);
                }
                feature.setVariants(variants);
            }

            // Parse allocation if present
            JsonNode allocationNode = node.get("allocation");
            if (allocationNode != null && !allocationNode.isNull()) {
                Allocation allocation = CASE_INSENSITIVE_MAPPER.convertValue(allocationNode, Allocation.class);
                feature.setAllocation(allocation);
            }

            if (feature.getTelemetry() != null) {
                final FeatureTelemetry telemetry = feature.getTelemetry();
                if (telemetry.isEnabled()) {
                    final Map<String, String> originMetadata = telemetry.getMetadata();
                    originMetadata.put(E_TAG, item.getETag());
                    if (originEndpoint != null && !originEndpoint.isEmpty()) {
                        final String labelPart = item.getLabel().isEmpty() ? ""
                            : String.format("?label=%s", item.getLabel());
                        originMetadata.put(FEATURE_FLAG_REFERENCE,
                            String.format("%s/kv/%s%s", originEndpoint, item.getKey(), labelPart));
                    }
                    originMetadata.put("AllocationId", generateAllocationId(node));
                }
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("Error parsing feature flag value for key: {}", item.getKey(), e);
        }
        return feature;
    }

    /**
     * @return the properties
     */
    public List<Feature> getFeatureFlags() {
        return properties.values().stream().toList();
    }
    
    public void resetTelemetry() {
        tracing.resetFeatureFilterTelemetry();
    }
    
    /**
     * Looks at each filter used in a Feature Flag to check what types it is using.
     * 
     * @param featureFlag FeatureFlagConfigurationSetting
     * @param tracing The TracingInfo for this store.
     */
    private void updateTelemetry(FeatureFlagConfigurationSetting featureFlag) {
        for (FeatureFlagFilter filter : featureFlag.getClientFilters()) {
            tracing.updateFeatureFilterTelemetry(filter.getName());
        }
    }

    /**
     * Generates a unique allocation ID for the feature flag based on its configuration.
     *
     * @param featureFlagValue The feature flag value as a map.
     * @return A unique allocation ID or null if the allocation is not valid.
     */
    static String generateAllocationId(JsonNode featureFlagValue) {
        StringBuilder allocationId = new StringBuilder();
        List<String> allocatedVariants = new ArrayList<>();

        // Retrieve allocation object
        JsonNode allocation = featureFlagValue.get("allocation");
        if (allocation == null) {
            return null;
        }

        // Seed
        allocationId.append("seed=").append(allocation.has("seed") ? allocation.get("seed").asText() : "");

        // DefaultWhenEnabled
        if (allocation.has("default_when_enabled")) {
            allocatedVariants.add(allocation.get("default_when_enabled").asText());
        }
        allocationId.append("\ndefault_when_enabled=").append(allocation.has("default_when_enabled") ? allocation.get("default_when_enabled").asText() : "");

        // Percentile
        allocationId.append("\npercentiles=");
        JsonNode percentile = allocation.get("percentile");
        List<JsonNode> percentileAllocations = new ArrayList<>();
        if (percentile != null && percentile.isArray()) {
            percentile.forEach(p -> {
                if (!Objects.equals(p.get("from").asText(), p.get("to").asText())) {
                    percentileAllocations.add(p);
                }
            });
            percentileAllocations.sort(Comparator.comparing(p -> p.get("from").asInt()));
        }

        for (JsonNode percentileAllocation : percentileAllocations) {
            if (percentileAllocation.has("variant")) {
                allocatedVariants.add(percentileAllocation.get("variant").asText());
            }
        }

        allocationId.append(percentileAllocations.stream()
            .map(pa -> pa.get("from") + ","
                + Base64.getEncoder().encodeToString(pa.get("variant").asText().getBytes(StandardCharsets.UTF_8)) + ","
                + pa.get("to"))
            .collect(Collectors.joining(";")));

        if (allocatedVariants.isEmpty() && (allocation.get("seed") == null)) {
            return null;
        }

        // Variants
        allocationId.append("\nvariants=");
        List<Map<String, Object>> variantsValue = new ArrayList<>();
        JsonNode variantsNode = featureFlagValue.get("variants");
        if (variantsNode != null && variantsNode.isArray()) {
            variantsNode.forEach(variantNode -> {
                if (variantNode.isObject()) {
                    Map<String, Object> variantMap = new LinkedHashMap<>();
                    variantNode.properties().iterator().forEachRemaining(entry -> variantMap.put(entry.getKey(), entry.getValue()));
                    variantsValue.add(variantMap);
                }
            });
        }
        if (variantsValue != null && !variantsValue.isEmpty()) {
            List<Map<String, Object>> sortedVariants = variantsValue.stream()
                .filter(v -> allocatedVariants.contains(v.get("name")))
                .sorted(Comparator.comparing(v -> (String) v.get("name")))
                .collect(Collectors.toList());

            for (Map<String, Object> variant : sortedVariants) {
                allocationId.append(Base64.getEncoder().encodeToString(((String) variant.get("name")).getBytes(StandardCharsets.UTF_8))).append(",");
                Object configValue = variant.get("configuration_value");
                if (configValue instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> configMap = (Map<String, Object>) configValue;
                    allocationId.append(new TreeMap<>(configMap).toString());
                }
                allocationId.append(";");
            }
            if (!sortedVariants.isEmpty()) {
                allocationId.setLength(allocationId.length() - 1); // Remove trailing semicolon
            }
        }

        // Create a SHA-256 hash of the allocationId
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(allocationId.toString().getBytes(StandardCharsets.UTF_8));

            // Encode the first 15 bytes in Base64 URL-safe format
            return Base64.getUrlEncoder().withoutPadding().encodeToString(Arrays.copyOf(hashBytes, 15));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
