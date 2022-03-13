// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Custom JSON serializer for {@link ConfigurationSetting} and its derived classes,
 * {@link SecretReferenceConfigurationSetting} and {@link FeatureFlagConfigurationSetting}.
 */
public final class ConfigurationSettingJsonSerializer extends JsonSerializer<ConfigurationSetting>  {

    static final String ID = "id";
    static final String DESCRIPTION = "description";
    static final String DISPLAY_NAME = "display_name";
    static final String ENABLED = "enabled";
    static final String CONDITIONS = "conditions";
    static final String CLIENT_FILTERS = "client_filters";
    static final String NAME = "name";
    static final String PARAMETERS = "parameters";

    static final String KEY = "key";
    static final String LABEL = "label";
    static final String VALUE = "value";
    static final String CONTENT_TYPE = "content_type";
    static final String ETAG = "etag";
    static final String LAST_MODIFIED = "last_modified";
    static final String LOCKED = "locked";
    static final String TAGS = "tags";
    static final String URI = "uri";

    static final SimpleModule MODULE;

    static {
        MODULE = new SimpleModule();
        MODULE.addSerializer(ConfigurationSetting.class, new ConfigurationSettingJsonSerializer());
    }

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson
     * ObjectMapper.
     *
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule() {
        return MODULE;
    }

    @Override
    public void serialize(ConfigurationSetting value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
        write(value, gen);
    }

    private static void write(ConfigurationSetting value, JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        String settingValue;
        if (value instanceof FeatureFlagConfigurationSetting) {
            settingValue = writeFeatureFlagConfigurationSetting((FeatureFlagConfigurationSetting) value);
        } else if (value instanceof SecretReferenceConfigurationSetting) {
            settingValue = writeSecretReferenceConfigurationSetting((SecretReferenceConfigurationSetting) value);
        } else {
            settingValue = value.getValue();
        }
        gen.writeStringField(VALUE, settingValue);

        gen.writeStringField(KEY, value.getKey());

        if (!CoreUtils.isNullOrEmpty(value.getLabel())) {
            gen.writeStringField(LABEL, value.getLabel());
        }

        gen.writeStringField(CONTENT_TYPE, value.getContentType());
        gen.writeStringField(ETAG, value.getETag());
        gen.writeBooleanField(LOCKED, value.isReadOnly());

        gen.writeObjectFieldStart(TAGS);
        writeMapProperties(value.getTags(), gen);
        gen.writeEndObject();

        if (value.getLastModified() != null) {
            gen.writeStringField(LAST_MODIFIED, value.getLastModified().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        gen.writeEndObject();
    }

    /**
     * Serialize the strongly-type property, {@code secretId} of {@link SecretReferenceConfigurationSetting} into a
     * JSON format string, which is the {@code value} of {@link SecretReferenceConfigurationSetting}.
     *
     * @param setting the {@link SecretReferenceConfigurationSetting}.
     * @return a JSON format string that represents the {@code value} of {@link SecretReferenceConfigurationSetting}.
     * @throws IOException if {@link JsonGenerator} can not be created.
     */
    public static String writeSecretReferenceConfigurationSetting(SecretReferenceConfigurationSetting setting)
        throws IOException {
        // The setting's value is expected to be a JSON string for the SecretReferenceConfigurationSetting,
        // so it is better to use another JSON generator to constructor the value as JSON string, flush into the
        // StringWriter.
        final StringWriter jsonObjectWriter = new StringWriter();
        final JsonGenerator gen = new JsonFactory().createGenerator(jsonObjectWriter);
        gen.writeStartObject();
        gen.writeStringField(URI, setting.getSecretId());
        gen.writeEndObject();
        gen.close();
        return jsonObjectWriter.toString();
    }

    /**
     * Serialize the strong-type properties, such as {@code featureId} of {@link FeatureFlagConfigurationSetting}
     * into a JSON format string, which is the {@code value} of {@link FeatureFlagConfigurationSetting}.
     *
     * @param setting the {@link FeatureFlagConfigurationSetting}.
     * @return a JSON format string that represents the {@code value} of {@link FeatureFlagConfigurationSetting}.
     * @throws IOException if {@link JsonGenerator} can not be created.
     */
    public static String writeFeatureFlagConfigurationSetting(FeatureFlagConfigurationSetting setting)
        throws IOException {
        // The setting's value is expected to be a JSON string for the FeatureFlagConfigurationSetting,
        // so it is better to use another JSON generator to constructor the value as JSON string, flush into the
        // StringWriter.
        final StringWriter jsonObjectWriter = new StringWriter();
        final JsonGenerator gen = new JsonFactory().createGenerator(jsonObjectWriter);
        gen.writeStartObject();

        gen.writeStringField(ID, setting.getFeatureId());
        gen.writeStringField(DESCRIPTION, setting.getDescription());
        gen.writeStringField(DISPLAY_NAME, setting.getDisplayName());
        gen.writeBooleanField(ENABLED, setting.isEnabled());

        gen.writeObjectFieldStart(CONDITIONS);
        gen.writeArrayFieldStart(CLIENT_FILTERS);
        for (FeatureFlagFilter filter : setting.getClientFilters()) {
            gen.writeStartObject();
            gen.writeStringField(NAME, filter.getName());
            gen.writeObjectFieldStart(PARAMETERS);
            writeMapProperties(filter.getParameters(), gen);
            gen.writeEndObject(); // parameters object
            gen.writeEndObject(); // each filter object
        }

        gen.writeEndArray();
        gen.writeEndObject();

        gen.writeEndObject();
        gen.close();

        return jsonObjectWriter.toString();
    }

    private static void writeMapProperties(Map<String, ? extends Object> properties, JsonGenerator gen)
        throws IOException {
        if (CoreUtils.isNullOrEmpty(properties)) {
            return;
        }

        for (Map.Entry<String, ? extends Object> property : properties.entrySet()) {
            gen.writeFieldName(property.getKey());
            gen.writeObject(property.getValue().toString());
        }
    }
}
