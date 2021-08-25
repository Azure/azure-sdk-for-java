// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.CLIENT_FILTERS;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.CONDITIONS;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.CONTENT_TYPE;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.DESCRIPTION;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.DISPLAY_NAME;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.ENABLED;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.ID;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.KEY;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.LAST_MODIFIED;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.LOCKED;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.NAME;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.PARAMETERS;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.URI;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.VALUE;

/**
 * Custom JSON deserializer for {@link ConfigurationSetting} and its derived classes,
 * {@link SecretReferenceConfigurationSetting} and {@link FeatureFlagConfigurationSetting}.
 */
public final class ConfigurationSettingJsonDeserializer extends JsonDeserializer<ConfigurationSetting> {
    private static final ClientLogger LOGGER = new ClientLogger(ConfigurationSettingJsonDeserializer.class);
    private static final String CONFIGURATION_SETTING_PATH =
        "com.azure.data.appconfiguration.models.ConfigurationSetting";
    private static final String FEATURE_FLAG_CONTENT_TYPE = "application/vnd.microsoft.appconfig.ff+json;charset=utf-8";
    private static final String SECRET_REFERENCE_CONTENT_TYPE =
        "application/vnd.microsoft.appconfig.keyvaultref+json;charset=utf-8";

    private static final JacksonAdapter MAPPER;
    private static final SimpleModule MODULE;
    static {
        MAPPER = (JacksonAdapter) JacksonAdapter.createDefaultSerializerAdapter();
        MODULE = new SimpleModule()
                     .addDeserializer(ConfigurationSetting.class, new ConfigurationSettingJsonDeserializer())
                     .addDeserializer(SecretReferenceConfigurationSetting.class,
                         configurationSettingSubclassDeserializer(SecretReferenceConfigurationSetting.class))
                     .addDeserializer(FeatureFlagConfigurationSetting.class,
                         configurationSettingSubclassDeserializer(FeatureFlagConfigurationSetting.class));
    }

    /**
     * Gets a module wrapping this deserializer as an adapter for the Jackson
     * ObjectMapper.
     *
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule() {
        return MODULE;
    }

    @Override
    public ConfigurationSetting deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return read(ctxt.readTree(p));
    }

    private static ConfigurationSetting read(JsonNode node) {
        String key = null;
        final JsonNode keyNode = node.get(KEY);
        if (keyNode != null && !keyNode.isNull()) {
            key = keyNode.asText();
        }

        final JsonNode contentTypeNode = node.get(CONTENT_TYPE);
        String contentType = null;
        if (contentTypeNode != null && !contentTypeNode.isNull()) {
            contentType = contentTypeNode.asText();
        }

        final ConfigurationSetting baseSetting = readConfigurationSetting(node);
        try {
            if (key != null && key.startsWith(FeatureFlagConfigurationSetting.KEY_PREFIX)
                    && FEATURE_FLAG_CONTENT_TYPE.equals(contentType)) {
                return readFeatureFlagConfigurationSetting(node, baseSetting);
            } else if (SECRET_REFERENCE_CONTENT_TYPE.equals(contentType)) {
                return readSecretReferenceConfigurationSetting(node, baseSetting);
            }
        } catch (Exception exception) {
            LOGGER.info("The setting is neither a 'FeatureFlagConfigurationSetting' nor "
                + "'SecretReferenceConfigurationSetting', return the setting as 'ConfigurationSetting'. "
                + "Error: ", exception);
        }
        return baseSetting;
    }

    private static SecretReferenceConfigurationSetting readSecretReferenceConfigurationSetting(JsonNode settingNode,
        ConfigurationSetting baseSetting) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

        final JsonNode valueNode = settingNode.get(VALUE);
        String settingValue = null;
        if (valueNode != null && !valueNode.isNull()) {
            settingValue = valueNode.asText();
        }

        SecretReferenceConfigurationSetting secretReferenceConfigurationSetting =
            readSecretReferenceConfigurationSettingValue(baseSetting.getKey(), settingValue)
                .setValue(settingValue)
                .setLabel(baseSetting.getLabel())
                .setETag(baseSetting.getETag())
                .setContentType(baseSetting.getContentType())
                .setTags(baseSetting.getTags());

        configurationSettingSubclassReflection(SecretReferenceConfigurationSetting.class,
            secretReferenceConfigurationSetting, settingNode);

        return secretReferenceConfigurationSetting;
    }

    private static ConfigurationSetting readConfigurationSetting(JsonNode setting) {
        try {
            return MAPPER.serializer().treeToValue(setting, ConfigurationSetting.class);
        } catch (JsonProcessingException exception) {
            throw LOGGER.logExceptionAsError(new RuntimeException(exception));
        }
    }

    private static FeatureFlagConfigurationSetting readFeatureFlagConfigurationSetting(JsonNode settingNode,
        ConfigurationSetting baseSetting) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        final JsonNode valueNode = settingNode.get(VALUE);
        String settingValue = null;
        if (valueNode != null && !valueNode.isNull()) {
            settingValue = valueNode.asText();
        }

        final FeatureFlagConfigurationSetting featureFlagConfigurationSetting =
            readFeatureFlagConfigurationSettingValue(settingValue)
                .setKey(baseSetting.getKey())
                .setValue(settingValue)
                .setLabel(baseSetting.getLabel())
                .setETag(baseSetting.getETag())
                .setContentType(baseSetting.getContentType())
                .setTags(baseSetting.getTags());

        configurationSettingSubclassReflection(FeatureFlagConfigurationSetting.class, featureFlagConfigurationSetting,
            settingNode);

        return featureFlagConfigurationSetting;
    }

    private static <T extends ConfigurationSetting> ConfigurationSetting configurationSettingSubclassReflection(
        Class<T> subclass, ConfigurationSetting setting, JsonNode settingNode)
        throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

        Class<?> configurationSettingClass = subclass;
        while (!CONFIGURATION_SETTING_PATH.equals(configurationSettingClass.getName())) {
            configurationSettingClass = Class.forName(subclass.getName()).getSuperclass();
        }

        // private field: readOnly
        Field readOnlyField = configurationSettingClass.getDeclaredField("readOnly");
        readOnlyField.setAccessible(true);

        final JsonNode isLockedNode = settingNode.get(LOCKED);
        boolean locked = false;
        if (isLockedNode != null && !isLockedNode.isNull()) {
            locked = isLockedNode.asBoolean();
        }

        readOnlyField.set(setting, locked);

        final JsonNode lastModifiedNode = settingNode.get(LAST_MODIFIED);
        if (lastModifiedNode != null && !lastModifiedNode.isNull()) {
            String lastModified = lastModifiedNode.asText();
            // private filed: lastModified
            Field lastModifiedField = configurationSettingClass.getDeclaredField("lastModified");
            lastModifiedField.setAccessible(true);
            lastModifiedField.set(setting, OffsetDateTime.parse(lastModified, DateTimeFormatter.ISO_DATE_TIME));
        }
        return setting;
    }

    /**
     * Given a JSON format string {@code settingValue}, deserializes it into a {@link JsonNode} and returns a
     * {@link SecretReferenceConfigurationSetting} object.
     *
     * @param key the {@code key} property of setting.
     * @param settingValue a JSON format string that represents the {@code value} property of setting.
     * @return A {@link SecretReferenceConfigurationSetting} object.
     */
    public static SecretReferenceConfigurationSetting readSecretReferenceConfigurationSettingValue(String key,
        String settingValue) {
        final JsonNode settingValueNode = toJsonNode(settingValue);

        final JsonNode uriNode = settingValueNode.get(URI);
        String secretID = null;
        if (uriNode != null && !uriNode.isNull()) {
            secretID = uriNode.asText(); // uri node contains the secret ID value
        }
        return new SecretReferenceConfigurationSetting(key, secretID);
    }

    /**
     * Given a JSON format string {@code settingValue}, deserializes it into a {@link JsonNode} and returns a
     * {@link FeatureFlagConfigurationSetting} object.
     *
     * @param settingValue a JSON format string that represents the {@code value} property of setting.
     * @return A {@link FeatureFlagConfigurationSetting} object which converted from the {@code settingValue}.
     */
    public static FeatureFlagConfigurationSetting readFeatureFlagConfigurationSettingValue(String settingValue) {
        JsonNode settingValueNode = toJsonNode(settingValue);

        final JsonNode featureIdNode = settingValueNode.get(ID);
        String featureId = null;
        if (featureIdNode != null && !featureIdNode.isNull()) {
            featureId = featureIdNode.asText();
        }

        final JsonNode descriptionNode = settingValueNode.get(DESCRIPTION);
        String description = null;
        if (descriptionNode != null && !descriptionNode.isNull()) {
            description = descriptionNode.asText();
        }

        final JsonNode displayNameNode = settingValueNode.get(DISPLAY_NAME);
        String displayName = null;
        if (displayNameNode != null && !displayNameNode.isNull()) {
            displayName = displayNameNode.asText();
        }

        final JsonNode isEnabledNode = settingValueNode.get(ENABLED);
        boolean isEnabled = false;
        if (isEnabledNode != null && !isEnabledNode.isNull()) {
            isEnabled = isEnabledNode.asBoolean();
        }

        final JsonNode conditionsNode = settingValueNode.get(CONDITIONS);
        List<FeatureFlagFilter> filters = null;
        if (conditionsNode != null && !conditionsNode.isNull()) {
            filters = readConditions(conditionsNode);
        }

        return new FeatureFlagConfigurationSetting(featureId, isEnabled)
                   .setDescription(description)
                   .setDisplayName(displayName)
                   .setClientFilters(filters);
    }

    private static List<FeatureFlagFilter> readConditions(JsonNode conditionsNode) {
        JsonNode clientFiltersNode = conditionsNode.get(CLIENT_FILTERS);
        if (clientFiltersNode == null || clientFiltersNode.isNull()) {
            return Collections.emptyList();
        }
        return readFeatureFlagFilters(clientFiltersNode);
    }

    private static List<FeatureFlagFilter> readFeatureFlagFilters(JsonNode featureFlagFilters) {
        List<FeatureFlagFilter> filters = new ArrayList<>();
        featureFlagFilters.forEach(filter -> filters.add(readFeatureFlagFilter(filter)));
        return filters;
    }

    private static FeatureFlagFilter readFeatureFlagFilter(JsonNode filter) {
        String name = null;
        final JsonNode filterNameNode = filter.get(NAME);
        if (filterNameNode != null && !filterNameNode.isNull()) {
            name = filterNameNode.asText();
        }

        final FeatureFlagFilter flagFilter = new FeatureFlagFilter(name);

        final JsonNode parametersNode = filter.get(PARAMETERS);
        if (parametersNode != null && !parametersNode.isNull()) {
            flagFilter.setParameters(readParameters(parametersNode));
        }
        return flagFilter;
    }

    private static Map<String, Object> readParameters(JsonNode node) {
        Map<String, Object> additionalProperties = null;
        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = node.fields();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> field = fieldsIterator.next();
            String propertyName = field.getKey();
            if (additionalProperties == null) {
                additionalProperties = new HashMap<>();
            }

            additionalProperties.put(propertyName, readAdditionalPropertyValue(field.getValue()));
        }

        return additionalProperties;
    }

    private static Object readAdditionalPropertyValue(JsonNode node) {
        switch (node.getNodeType()) {
            case STRING:
                return node.asText();
            case NUMBER:
                if (node.isInt()) {
                    return node.asInt();
                } else if (node.isLong()) {
                    return node.asLong();
                } else if (node.isFloat()) {
                    return node.floatValue();
                } else {
                    return node.asDouble();
                }
            case BOOLEAN:
                return node.asBoolean();
            case NULL:
            case MISSING:
                return null;
            case OBJECT:
                Map<String, Object> object = new HashMap<>();
                node.fields().forEachRemaining(
                    field -> object.put(field.getKey(), readAdditionalPropertyValue(field.getValue())));
                return object;
            case ARRAY:
                List<Object> array = new ArrayList<>();
                node.forEach(element -> array.add(readAdditionalPropertyValue(element)));

                return array;
            default:
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    String.format("Unsupported additional property type %s.", node.getNodeType())));
        }
    }

    private static <T extends ConfigurationSetting> JsonDeserializer<T> configurationSettingSubclassDeserializer(
        Class<T> subclass) {
        return new JsonDeserializer<T>() {
            @Override
            public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return subclass.cast(read(ctxt.readTree(p)));
            }
        };
    }

    private static JsonNode toJsonNode(String settingValue) {
        try {
            return MAPPER.serializer().readTree(settingValue);
        } catch (JsonProcessingException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(e));
        }
    }
}
