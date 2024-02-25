// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.AUDIENCE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.CONDITIONS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.DEFAULT_REQUIREMENT_TYPE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.DEFAULT_ROLLOUT_PERCENTAGE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.DEFAULT_ROLLOUT_PERCENTAGE_CAPS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.E_TAG;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_CONTENT_TYPE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_ID;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_PREFIX;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_REFERENCE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_MANAGEMENT_KEY;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.GROUPS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.GROUPS_CAPS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.REQUIREMENT_TYPE_SERVICE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.SELECT_ALL_FEATURE_FLAGS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.TARGETING_FILTER;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.TELEMETRY;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.USERS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.USERS_CAPS;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.FeatureTelemetry;
import com.nimbusds.jose.util.Base64URL;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SettingSelector;
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
class AppConfigurationFeatureManagementPropertySource extends AppConfigurationPropertySource {

    private static final ObjectMapper CASE_INSENSITIVE_MAPPER = JsonMapper.builder()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();

    private final String keyFilter;

    private final String[] labelFilter;

    AppConfigurationFeatureManagementPropertySource(String originEndpoint, AppConfigurationReplicaClient replicaClient,
        String keyFilter, String[] labelFilter) {
        super("FM_" + originEndpoint + "/" + getLabelName(labelFilter), replicaClient);
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
    @Override
    public void initProperties(List<String> trim) {
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

            // Reading In Features
            for (ConfigurationSetting setting : features) {
                if (setting instanceof FeatureFlagConfigurationSetting
                    && FEATURE_FLAG_CONTENT_TYPE.equals(setting.getContentType())) {
                    processFeatureFlag(null, (FeatureFlagConfigurationSetting) setting, null);
                }
            }
        }
    }

    List<ConfigurationSetting> getFeatureFlagSettings() {
        return featureConfigurationSettings;
    }

    protected void processFeatureFlag(String key, FeatureFlagConfigurationSetting setting, List<String> trimStrings) {
        TracingInfo tracing = replicaClient.getTracingInfo();
        featureConfigurationSettings.add(setting);
        FeatureFlagConfigurationSetting featureFlag = setting;

        String configName = FEATURE_MANAGEMENT_KEY + setting.getKey().trim().substring(FEATURE_FLAG_PREFIX.length());

        updateTelemetry(featureFlag, tracing);

        properties.put(configName, createFeature(featureFlag, this.replicaClient.getEndpoint()));
    }

    /**
     * Creates a {@code Feature} from a {@code KeyValueItem}
     *
     * @param item Used to create Features before being converted to be set into properties.
     * @return Feature created from KeyValueItem
     */
    @SuppressWarnings("unchecked")
    protected static Object createFeature(FeatureFlagConfigurationSetting item, String originEndpoint) {
        String key = getFeatureSimpleName(item);
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
        Feature feature = new Feature(key, item, requirementType, featureTelemetry);
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
        if (feature.getTelemetry() != null) {
            final FeatureTelemetry telemetry = feature.getTelemetry();
            if (telemetry.isEnabled()) {
                final Map<String, String> originMetadata = telemetry.getMetadata();
                originMetadata.put(FEATURE_FLAG_ID, calculateFeatureFlagId(item.getKey(), item.getLabel()));
                originMetadata.put(E_TAG, item.getETag());
                if (originEndpoint != null && !originEndpoint.isEmpty()) {
                    final String labelPart = item.getLabel().isEmpty() ? "" : String.format("?label=%s", item.getLabel());
                    originMetadata.put(FEATURE_FLAG_REFERENCE, String.format("%s/kv/%s%s", originEndpoint, item.getKey(), labelPart));
                }
            }
        }
        return feature;
    }

    /**
     * Looks at each filter used in a Feature Flag to check what types it is using.
     *
     * @param featureFlag FeatureFlagConfigurationSetting
     * @param tracing The TracingInfo for this store.
     */
    protected static void updateTelemetry(FeatureFlagConfigurationSetting featureFlag, TracingInfo tracing) {
        for (FeatureFlagFilter filter : featureFlag.getClientFilters()) {
            tracing.getFeatureFlagTracing().updateFeatureFilterTelemetry(filter.getName());
        }
    }

    private static String getFeatureSimpleName(ConfigurationSetting setting) {
        return setting.getKey().trim().substring(FEATURE_FLAG_PREFIX.length());
    }

    @SuppressWarnings("null")
    private static Map<String, Object> mapValuesByIndex(List<Object> users) {
        return IntStream.range(0, users.size()).boxed().collect(toMap(String::valueOf, users::get));
    }

    private static void switchKeyValues(Map<String, Object> parameters, String oldKey, String newKey, Object value) {
        parameters.put(newKey, value);
        parameters.remove(oldKey);
    }

    private static List<Object> convertToListOrEmptyList(Map<String, Object> parameters, String key) {
        List<Object> listObjects =
            CASE_INSENSITIVE_MAPPER.convertValue(parameters.get(key), new TypeReference<List<Object>>() {});
        return listObjects == null ? emptyList() : listObjects;
    }

    /**
     * @param key the key of feature flag
     * @param label the label of feature flag. If label is whitespace, treat as null
     * @return base64_url(SHA256(utf8_bytes("${key}\n${label}"))).replace('+', '-').replace('/', '_').trimEnd('=')
     * trimEnd() means trims everything after the first occurrence of the '='
     * */
    private static String calculateFeatureFlagId(String key, String label) {
        final String data = String.format("%s\n%s", key, label.isEmpty() ? null : label);
        final SHA256.Digest digest = new SHA256.Digest();
        final String beforeTrim = Base64URL.encode(digest.digest(data.getBytes(StandardCharsets.UTF_8)))
            .toString().replace('+', '-').replace('/', '_');
        final int index = beforeTrim.indexOf('=');
        return beforeTrim.substring(0, index > -1 ? index : beforeTrim.length());
    }
}
