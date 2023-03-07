// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.appconfiguration.implementation.models.KeyValue;
import com.azure.data.appconfiguration.implementation.models.KeyValueFields;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingFields;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;

/**
 * App Configuration Utility methods, use internally.
 */
public class Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);

    public static final String APP_CONFIG_TRACING_NAMESPACE_VALUE = "Microsoft.AppConfiguration";
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";
    private static final String FEATURE_FLAG_CONTENT_TYPE = "application/vnd.microsoft.appconfig.ff+json;charset=utf-8";
    private static final String SECRET_REFERENCE_CONTENT_TYPE =
        "application/vnd.microsoft.appconfig.keyvaultref+json;charset=utf-8";
    private static final JsonFactory FACTORY = JsonFactory.builder().build();

    static final String ID = "id";
    static final String DESCRIPTION = "description";
    static final String DISPLAY_NAME = "display_name";
    static final String ENABLED = "enabled";
    static final String CONDITIONS = "conditions";
    static final String CLIENT_FILTERS = "client_filters";
    static final String NAME = "name";
    static final String PARAMETERS = "parameters";
    static final String URI = "uri";

    /*
     * Translate public ConfigurationSetting to KeyValue autorest generated class.
     */
    public static KeyValue toKeyValue(ConfigurationSetting setting) {
        return new KeyValue()
                   .setKey(setting.getKey())
                   .setValue(setting.getValue())
                   .setLabel(setting.getLabel())
                   .setContentType(setting.getContentType())
                   .setEtag(setting.getETag())
                   .setLastModified(setting.getLastModified())
                   .setLocked(setting.isReadOnly())
                   .setTags(setting.getTags());
    }

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
                FeatureFlagConfigurationSetting featureFlagConfigurationSetting =
                    getFeatureFlagConfigurationSetting(setting);
                return subclassConfigurationSettingReflection(setting, featureFlagConfigurationSetting);
            } else if (SECRET_REFERENCE_CONTENT_TYPE.equals(contentType)) {
                return getSecretReferenceConfigurationSetting(setting)
                           .setValue(value)
                           .setLabel(label)
                           .setETag(etag)
                           .setContentType(contentType)
                           .setTags(tags);
            }
        } catch (Exception exception) {
            LOGGER.info("The setting is neither a 'FeatureFlagConfigurationSetting' nor "
                            + "'SecretReferenceConfigurationSetting', return the setting as 'ConfigurationSetting'. "
                            + "Error: ", exception);
        }

        return setting;
    }

    /*
     * Extract properties from ConfigurationSetting to SecretReferenceConfigurationSetting.
     */
    public static SecretReferenceConfigurationSetting getSecretReferenceConfigurationSetting(
        ConfigurationSetting setting) {
        try {
            JsonParser parser = FACTORY.createParser(setting.getValue().getBytes(StandardCharsets.UTF_8));
            return
                (SecretReferenceConfigurationSetting) subclassConfigurationSettingReflection(
                    setting, getSecretReferenceFieldValue(setting.getKey(), parser));
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(e));
        }
    }

    /*
     * Extract properties from ConfigurationSetting to FeatureFlagConfigurationSetting.
     */
    public static FeatureFlagConfigurationSetting getFeatureFlagConfigurationSetting(ConfigurationSetting setting) {
        try {
            JsonParser parser = FACTORY.createParser(setting.getValue().getBytes(StandardCharsets.UTF_8));
            return (FeatureFlagConfigurationSetting) subclassConfigurationSettingReflection(setting,
                getFieldValue(parser)
                    .setKey(setting.getKey())
                    .setValue(setting.getValue())
                    .setLabel(setting.getLabel())
                    .setETag(setting.getETag())
                    .setContentType(setting.getContentType())
                    .setTags(setting.getTags()));
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(e));
        }
    }

    private static <T extends ConfigurationSetting> ConfigurationSetting subclassConfigurationSettingReflection(
        ConfigurationSetting setting, T derivedClassSetting) {
        ConfigurationSettingHelper.setReadOnly(derivedClassSetting, setting.isReadOnly());
        ConfigurationSettingHelper.setLastModified(derivedClassSetting, setting.getLastModified());
        return derivedClassSetting;
    }

    // Secret reference configuration setting: parsing feature flag values
    private static SecretReferenceConfigurationSetting getSecretReferenceFieldValue(String key, JsonParser parser)
        throws IOException {
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
    }

    // Feature flag configuration setting: parsing feature flag values
    private static FeatureFlagConfigurationSetting getFieldValue(JsonParser parser) throws IOException {
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

    // Translate generated List<KeyValueFields> to public-explored SettingFields[].
    public static SettingFields[] toSettingFieldsArray(List<KeyValueFields> kvFieldsList) {
        return kvFieldsList.stream()
                   .map(keyValueFields -> toSettingFields(keyValueFields))
                   .collect(Collectors.toList())
                   .toArray(new SettingFields[kvFieldsList.size()]);
    }

    // Translate generated KeyValueFields to public-explored SettingFields.
    public static SettingFields toSettingFields(KeyValueFields keyValueFields) {
        return keyValueFields == null ? null : SettingFields.fromString(keyValueFields.toString());
    }

    // Translate public-explored SettingFields[] to generated List<KeyValueFields>.
    public static List<KeyValueFields> toKeyValueFieldsList(SettingFields[] settingFieldsArray) {
        return Arrays.stream(settingFieldsArray)
                   .map(settingFields -> toKeyValueFields(settingFields))
                   .collect(Collectors.toList());
    }

    // Translate public-explored SettingFields to generated KeyValueFields.
    public static KeyValueFields toKeyValueFields(SettingFields settingFields) {
        return settingFields == null ? null : KeyValueFields.fromString(settingFields.toString());
    }

    /*
     * Azure Configuration service requires that the ETag value is surrounded in quotation marks.
     *
     * @param ETag The ETag to get the value for. If null is pass in, an empty string is returned.
     * @return The ETag surrounded by quotations. (ex. "ETag")
     */
    private static String getETagValue(String etag) {
        return (etag == null || "*".equals(etag)) ? etag : "\"" + etag + "\"";
    }

    /*
     * Get HTTP header value, if-match. Used to perform an operation only if the targeted resource's etag matches the
     *  value provided.
     */
    public static String getIfMatchETag(boolean ifUnchanged, ConfigurationSetting setting) {
        return ifUnchanged ? getETagValue(setting.getETag()) : null;
    }

    /*
     * Get HTTP header value, if-none-match. Used to perform an operation only if the targeted resource's etag does not
     * match the value provided.
     */
    public static String getIfNoneMatchETag(boolean onlyIfChanged, ConfigurationSetting setting) {
        return onlyIfChanged ? getETagValue(setting.getETag()) : null;
    }

    /*
     * Ensure that setting is not null. And, key cannot be null because it is part of the service REST URL.
     */
    public static void validateSetting(ConfigurationSetting setting) {
        Objects.requireNonNull(setting);

        if (setting.getKey() == null) {
            throw new IllegalArgumentException("Parameter 'key' is required and cannot be null.");
        }
    }

    /**
     * Enable the sync stack rest proxy.
     *
     * @param context It offers a means of passing arbitrary data (key-value pairs) to pipeline policies.
     * Most applications do not need to pass arbitrary data to the pipeline and can pass Context.NONE or null.
     *
     * @return The Context.
     */
    public static Context enableSyncRestProxy(Context context) {
        return context.addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
    }

    public static Context addTracingNamespace(Context context) {
        context = context == null ? Context.NONE : context;
        return context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE);
    }
}
