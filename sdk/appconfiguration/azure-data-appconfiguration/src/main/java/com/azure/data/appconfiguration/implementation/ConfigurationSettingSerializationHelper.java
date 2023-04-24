// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static com.azure.data.appconfiguration.implementation.Utility.CLIENT_FILTERS;
import static com.azure.data.appconfiguration.implementation.Utility.CONDITIONS;
import static com.azure.data.appconfiguration.implementation.Utility.DESCRIPTION;
import static com.azure.data.appconfiguration.implementation.Utility.DISPLAY_NAME;
import static com.azure.data.appconfiguration.implementation.Utility.ENABLED;
import static com.azure.data.appconfiguration.implementation.Utility.ID;
import static com.azure.data.appconfiguration.implementation.Utility.NAME;
import static com.azure.data.appconfiguration.implementation.Utility.PARAMETERS;
import static com.azure.data.appconfiguration.implementation.Utility.URI;

/**
 * Configuration setting serialization.
 */
public final class ConfigurationSettingSerializationHelper {
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
