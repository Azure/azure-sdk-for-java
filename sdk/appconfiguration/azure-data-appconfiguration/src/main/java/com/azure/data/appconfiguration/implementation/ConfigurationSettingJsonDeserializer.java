// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.ETAG;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.ID;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.KEY;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.LABEL;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.LAST_MODIFIED;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.LOCKED;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.NAME;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.PARAMETERS;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.TAGS;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.URI;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.VALUE;

/**
 * Custom JSON deserializer for {@link ConfigurationSetting} and its derived classes,
 * {@link SecretReferenceConfigurationSetting} and {@link FeatureFlagConfigurationSetting}.
 */
public class ConfigurationSettingJsonDeserializer extends JsonDeserializer<ConfigurationSetting> {
    private static final ClientLogger LOGGER = new ClientLogger(ConfigurationSettingJsonDeserializer.class);

    private static final String FEATURE_FLAG_CONTENT_TYPE = "application/vnd.microsoft.appconfig.ff+json;charset=utf-8";
    private static final String SECRET_REFERENCE_CONTENT_TYPE =
        "application/vnd.microsoft.appconfig.keyvaultref+json;charset=utf-8";

    static final SimpleModule MODULE;
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
    }

    public static SimpleModule getModule() {
        return MODULE;
    }

    static {
        MODULE = new SimpleModule()
                     .addDeserializer(ConfigurationSetting.class, new ConfigurationSettingJsonDeserializer())
                     .addDeserializer(SecretReferenceConfigurationSetting.class,
                         configurationSettingSubclassDeserializer(SecretReferenceConfigurationSetting.class))
                     .addDeserializer(FeatureFlagConfigurationSetting.class,
                         configurationSettingSubclassDeserializer(FeatureFlagConfigurationSetting.class));
    }

    @Override
    public ConfigurationSetting deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return read(ctxt.readTree(p));
    }

    private static ConfigurationSetting read(JsonNode node) {
        final String key = getRequiredProperty(node, KEY).asText();

        final JsonNode contentTypeNode = node.get(CONTENT_TYPE);
        String contentType = null;
        if (contentTypeNode != null && !contentTypeNode.isNull()) {
            contentType = getRequiredProperty(node, CONTENT_TYPE).asText();
        }

        try {
            if (key.startsWith(FeatureFlagConfigurationSetting.KEY_PREFIX) &&
                    FEATURE_FLAG_CONTENT_TYPE.equals(contentType)) {
                return readFeatureFlagConfigurationSetting(node, contentType);
            } else if (SECRET_REFERENCE_CONTENT_TYPE.equals(contentType)) {
                return readSecretReferenceConfigurationSetting(node, contentType);
            }
        } catch (Exception exception) {
            LOGGER.info("Can't read Configuration setting, error is " + exception.getMessage());
        }

        return readConfigurationSetting(node, contentType);
    }

    /*
     * Attempts to retrieve a required property node value.
     *
     * @param node Parent JsonNode.
     * @param name Property being retrieved.
     * @return The JsonNode of the required property.
     */
    private static JsonNode getRequiredProperty(JsonNode node, String name) {
        JsonNode requiredNode = node.get(name);

        if (requiredNode == null) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                String.format("Setting expected to have '%s' property.", name)));
        }

        return requiredNode;
    }

    private static SecretReferenceConfigurationSetting readSecretReferenceConfigurationSetting(JsonNode settingNode,
        String contentType)
        throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        final ConfigurationSetting baseSetting = readConfigurationSetting(settingNode, contentType);

        final String settingValue = getRequiredProperty(settingNode, VALUE).asText();

        final JsonNode settingValueNode = toJsonNode(settingValue);
        final String secretID = getRequiredProperty(settingValueNode, URI).asText();

        SecretReferenceConfigurationSetting secretReferenceConfigurationSetting =
            new SecretReferenceConfigurationSetting(secretID, secretID)
                .setKey(baseSetting.getKey())
                .setValue(settingValue)
                .setLabel(baseSetting.getLabel())
                .setETag(baseSetting.getETag())
                .setContentType(baseSetting.getContentType())
                .setTags(baseSetting.getTags());

        configurationSettingSubclassReflection(SecretReferenceConfigurationSetting.class,
            secretReferenceConfigurationSetting, settingNode);

        return secretReferenceConfigurationSetting;
    }

    private static ConfigurationSetting readConfigurationSetting(JsonNode setting, String contentType) {
        final String value = getRequiredProperty(setting, VALUE).asText();

        final String key = getRequiredProperty(setting, KEY).asText();
        final JsonNode labelNode = setting.get(LABEL);
        String label = null;
        if (labelNode != null && !labelNode.isNull()) {
            label = labelNode.asText();
        }

        final JsonNode etagNode = setting.get(ETAG);
        String etag = null;
        if (etagNode != null && !etagNode.isNull()) {
            etag = etagNode.asText();
        }

        final JsonNode tagsNode = setting.get(TAGS);
        Map<String, String> tagsMap = null;
        if (tagsNode != null && !tagsNode.isNull()) {
            tagsMap = readTags(tagsNode);
        }

        return new ConfigurationSetting()
                   .setKey(key)
                   .setValue(value)
                   .setLabel(label)
                   .setETag(etag)
                   .setContentType(contentType)
                   .setTags(tagsMap);
    }

    private static FeatureFlagConfigurationSetting readFeatureFlagConfigurationSetting(JsonNode settingNode,
        String contentType)
        throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        final ConfigurationSetting baseSetting = readConfigurationSetting(settingNode, contentType);

        final String settingValue = getRequiredProperty(settingNode, VALUE).asText();
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

        Class<?> superClass = Class.forName(subclass.getName()).getSuperclass();

        // private field: readOnly
        Field readOnlyField = superClass.getDeclaredField("readOnly");
        readOnlyField.setAccessible(true);

        final boolean locked = getRequiredProperty(settingNode, LOCKED).asBoolean();
        readOnlyField.set(setting, locked);

        final JsonNode lastModifiedNode = settingNode.get(LAST_MODIFIED);
        if (lastModifiedNode != null && !lastModifiedNode.isNull()) {
            String lastModified = lastModifiedNode.asText();
            // private filed: lastModified
            Field lastModifiedField = superClass.getDeclaredField("lastModified");
            lastModifiedField.setAccessible(true);
            LocalDate lastModifiedDate = LocalDate.parse(lastModified, DateTimeFormatter.ofPattern("yyyyMMdd"));
            lastModifiedField.set(setting, lastModifiedDate);
        }
        return setting;
    }

    private static Map<String, String> readTags(JsonNode node) {
        Map<String, String> tags = null;
        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = node.fields();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> field = fieldsIterator.next();
            String propertyName = field.getKey();
            if (tags == null) {
                tags = new HashMap<>();
            }
            tags.put(propertyName, field.getValue().asText());
        }
        return tags;
    }

    private static FeatureFlagConfigurationSetting readFeatureFlagConfigurationSettingValue(String settingValue) {
        JsonNode settingValueNode = toJsonNode(settingValue);
        final String featureId = getRequiredProperty(settingValueNode, ID).asText();

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

        final boolean isEnabled = getRequiredProperty(settingValueNode, ENABLED).asBoolean();

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
        JsonNode clientFiltersNode = getRequiredProperty(conditionsNode, CLIENT_FILTERS);
        return readFeatureFlagFilters(clientFiltersNode);
    }

    private static List<FeatureFlagFilter> readFeatureFlagFilters(JsonNode featureFlagFilters) {
        List<FeatureFlagFilter> filters = new ArrayList<>();
        featureFlagFilters.forEach(filter -> filters.add(readFeatureFlagFilter(filter)));
        return filters;
    }

    private static FeatureFlagFilter readFeatureFlagFilter(JsonNode filter) {
        final String name = getRequiredProperty(filter, NAME).asText();
        final FeatureFlagFilter flagFilter = new FeatureFlagFilter(name);
        final JsonNode parametersNode = getRequiredProperty(filter, PARAMETERS);

        flagFilter.setParameters(readParameters(parametersNode));
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
        JsonNode settingValueNode;
        try {
            settingValueNode = MAPPER.readTree(settingValue);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        return settingValueNode;
    }
}
