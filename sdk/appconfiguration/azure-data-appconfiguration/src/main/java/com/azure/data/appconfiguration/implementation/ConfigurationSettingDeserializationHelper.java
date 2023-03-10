// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.data.appconfiguration.implementation.models.KeyValue;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.data.appconfiguration.implementation.Utility.CLIENT_FILTERS;
import static com.azure.data.appconfiguration.implementation.Utility.CONDITIONS;
import static com.azure.data.appconfiguration.implementation.Utility.DESCRIPTION;
import static com.azure.data.appconfiguration.implementation.Utility.DISPLAY_NAME;
import static com.azure.data.appconfiguration.implementation.Utility.ENABLED;
import static com.azure.data.appconfiguration.implementation.Utility.ID;
import static com.azure.data.appconfiguration.implementation.Utility.NAME;
import static com.azure.data.appconfiguration.implementation.Utility.PARAMETERS;
import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.azure.data.appconfiguration.implementation.Utility.URI;

/**
 * Configuration Setting Deserialization
 */
public final class ConfigurationSettingDeserializationHelper {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);

    static final String FEATURE_FLAG_CONTENT_TYPE = "application/vnd.microsoft.appconfig.ff+json;charset=utf-8";
    static final String SECRET_REFERENCE_CONTENT_TYPE =
        "application/vnd.microsoft.appconfig.keyvaultref+json;charset=utf-8";
    private static final JsonFactory FACTORY = JsonFactory.builder().build();

    /*
     * Translate generated class KeyValue to public-explored ConfigurationSetting.
     */
    public static ConfigurationSetting toConfigurationSetting(KeyValue keyValue) {
        if (keyValue == null) {
            return null;
        }
        final String contentType = keyValue.getContentType();
        final String key = keyValue.getKey();
        final String value = keyValue.getValue();
        final String label = keyValue.getLabel();
        final String etag = keyValue.getEtag();
        final Map<String, String> tags = keyValue.getTags();
        final ConfigurationSetting setting = new ConfigurationSetting()
                                                 .setKey(key)
                                                 .setValue(value)
                                                 .setLabel(label)
                                                 .setContentType(contentType)
                                                 .setETag(etag)
                                                 .setTags(tags);
        ConfigurationSettingHelper.setLastModified(setting, keyValue.getLastModified());
        ConfigurationSettingHelper.setReadOnly(setting, keyValue.isLocked() == null ? false : keyValue.isLocked());
        try {
            if (key != null && key.startsWith(FeatureFlagConfigurationSetting.KEY_PREFIX)
                    && FEATURE_FLAG_CONTENT_TYPE.equals(contentType)) {
                return subclassConfigurationSettingReflection(setting, parseFeatureFlagValue(setting.getValue()))
                           .setKey(setting.getKey())
                           .setValue(setting.getValue())
                           .setLabel(setting.getLabel())
                           .setETag(setting.getETag())
                           .setContentType(setting.getContentType())
                           .setTags(setting.getTags());
            } else if (SECRET_REFERENCE_CONTENT_TYPE.equals(contentType)) {
                return subclassConfigurationSettingReflection(setting,
                    parseSecretReferenceFieldValue(setting.getKey(), setting.getValue()))
                           .setValue(value)
                           .setLabel(label)
                           .setETag(etag)
                           .setContentType(contentType)
                           .setTags(tags);
            } else {
                // Configuration Setting
                return setting;
            }
        } catch (Exception exception) {
            throw LOGGER.logExceptionAsError(new RuntimeException(
                "The setting is neither a 'FeatureFlagConfigurationSetting' nor "
                    + "'SecretReferenceConfigurationSetting', return the setting as 'ConfigurationSetting'. "
                    + "Error: ", exception));
        }
    }

    private static <T extends ConfigurationSetting> ConfigurationSetting subclassConfigurationSettingReflection(
        ConfigurationSetting setting, T derivedClassSetting) {
        ConfigurationSettingHelper.setReadOnly(derivedClassSetting, setting.isReadOnly());
        ConfigurationSettingHelper.setLastModified(derivedClassSetting, setting.getLastModified());
        return derivedClassSetting;
    }

    /*
       Parse the ConfigurationSetting's value into Feature Flag setting's properties.
     */
    public static FeatureFlagConfigurationSetting parseFeatureFlagValue(String valueInJson) {
        try {
            return getFeatureFlagPropertyValue(FACTORY.createParser(valueInJson.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(e));
        }
    }

    // Secret reference configuration setting: parsing values
    public static SecretReferenceConfigurationSetting parseSecretReferenceFieldValue(String key, String value) {
        try {
            JsonParser parser = FACTORY.createParser(value.getBytes(StandardCharsets.UTF_8));

            // Read first object, "{"
            parser.nextToken();
            JsonToken token = parser.nextToken();
            // uri
            String secretId = null;

            if (token == JsonToken.FIELD_NAME && URI.equals(parser.getCurrentName())) {
                token = parser.nextToken();
                if (token == JsonToken.VALUE_STRING) {
                    secretId = parser.getText();
                }
            }

            // Use the map to get all properties
            SecretReferenceConfigurationSetting secretReferenceConfigurationSetting =
                new SecretReferenceConfigurationSetting(key, secretId);

            parser.close();
            return secretReferenceConfigurationSetting;
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(e));
        }
    }

    // Feature flag configuration setting: parsing feature flag values
    private static FeatureFlagConfigurationSetting getFeatureFlagPropertyValue(JsonParser parser) throws IOException {
        // Read first object, "{"
        JsonToken token = parser.nextToken();
        token = parser.nextToken();
        // id
        Map<String, Object> map = new HashMap<>();
        if (token == JsonToken.FIELD_NAME && ID.equals(parser.getCurrentName())) {
            token = parser.nextToken();
            if (token == JsonToken.VALUE_STRING) {
                map.put(ID, parser.getText());
            }
        }
        // description
        token = parser.nextToken();
        if (token == JsonToken.FIELD_NAME && DESCRIPTION.equals(parser.getCurrentName())) {
            token = parser.nextToken();
            if (token == JsonToken.VALUE_STRING) {
                map.put(DESCRIPTION, parser.getText());
            }
        }
        // display name
        token = parser.nextToken();
        if (token == JsonToken.FIELD_NAME && DISPLAY_NAME.equals(parser.getCurrentName())) {
            token = parser.nextToken();
            if (token == JsonToken.VALUE_STRING) {
                map.put(DISPLAY_NAME, parser.getText());
            }
        }
        // is enabled
        token = parser.nextToken();
        if (token == JsonToken.FIELD_NAME && ENABLED.equals(parser.getCurrentName())) {
            token = parser.nextToken();
            if (token == JsonToken.VALUE_FALSE || token == JsonToken.VALUE_TRUE) {
                map.put(ENABLED, parser.getBooleanValue());
            }
        }

        // Use the map to get all properties
        FeatureFlagConfigurationSetting featureFlagConfigurationSetting =
            new FeatureFlagConfigurationSetting((String) map.get(ID), (boolean) map.get(ENABLED))
                .setDisplayName((String) map.get(DISPLAY_NAME))
                .setDescription((String) map.get(DESCRIPTION));

        // conditional arrays
        token = parser.nextToken();
        if (token == JsonToken.FIELD_NAME && CONDITIONS.equals(parser.getCurrentName())) {
            parser.nextToken(); // get object start
            token = parser.nextToken(); // get field name
            if (token == JsonToken.FIELD_NAME && CLIENT_FILTERS.equals(parser.getCurrentName())) {
                // read JSON array
                featureFlagConfigurationSetting.setClientFilters(readClientFilters(parser));
            }
        }

        parser.close();

        return featureFlagConfigurationSetting;
    }

    // Feature flag configuration setting: client filters
    @SuppressWarnings("unchecked")
    private static List<FeatureFlagFilter> readClientFilters(JsonParser parser) throws IOException {
        List<FeatureFlagFilter> filters = new ArrayList<>();
        JsonToken token = parser.nextToken();
        while (token != END_ARRAY) {
            FeatureFlagFilter flagFilter = null;
            if (token == JsonToken.FIELD_NAME && NAME.equals(parser.getCurrentName())) {
                token = parser.nextToken();
                if (token == JsonToken.VALUE_STRING) {
                    flagFilter = new FeatureFlagFilter(parser.getText());
                }
            }
            token = parser.nextToken();
            if (token == JsonToken.FIELD_NAME && PARAMETERS.equals(parser.getCurrentName())) {
                final Object propertyValue = readAdditionalPropertyValue(parser);
                if (!(propertyValue instanceof Map)) {
                    throw LOGGER.logExceptionAsError(new IllegalStateException(
                        "property class type should be Map<String, Object>, it represents a json data format in Java.")
                    );
                }
                flagFilter.setParameters((Map<String, Object>) propertyValue);  // Map<String, Object>
                filters.add(flagFilter);
            }
        }
        return filters;
    }

    // Feature flag configuration setting: "parameters" values
    private static Object readAdditionalPropertyValue(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        switch (token) {
            case END_OBJECT:
            case START_OBJECT:
            case END_ARRAY:
                return readAdditionalPropertyValue(parser);
            case START_ARRAY:
                parser.nextToken();
                return readAdditionalPropertyValue(parser);
            case FIELD_NAME:
                final String currentName = parser.getCurrentName();
                Map<String, Object> kv = new HashMap<>();
                kv.put(currentName, readAdditionalPropertyValue(parser));
                return kv;
            case VALUE_STRING:
                return parser.getText();
            case VALUE_NUMBER_INT:
                return parser.getIntValue();
            case VALUE_NUMBER_FLOAT:
                return parser.getFloatValue();
            case VALUE_FALSE:
            case VALUE_TRUE:
                return parser.getBooleanValue();
            default:
            case VALUE_NULL:
                return null;
        }
    }

}
