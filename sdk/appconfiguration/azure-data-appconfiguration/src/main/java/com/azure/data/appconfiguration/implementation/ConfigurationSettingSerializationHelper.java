// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
     * Serialize the strongly-type property, {@code secretId} of {@link SecretReferenceConfigurationSetting} into a JSON
     * format string, which is the {@code value} of {@link SecretReferenceConfigurationSetting}.
     *
     * @param setting the {@link SecretReferenceConfigurationSetting}.
     * @return a JSON format string that represents the {@code value} of {@link SecretReferenceConfigurationSetting}.
     * @throws IOException if {@link JsonWriter} can not be created.
     */
    public static String writeSecretReferenceConfigurationSetting(SecretReferenceConfigurationSetting setting)
        throws IOException {
        // The setting's value is expected to be a JSON string for the SecretReferenceConfigurationSetting,
        // so it is better to use another JSON generator to constructor the value as JSON string, flush into the
        // StringWriter.
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField(URI, setting.getSecretId());
            jsonWriter.writeEndObject();
            jsonWriter.flush();

            return outputStream.toString(StandardCharsets.UTF_8.name());
        }
    }

    /**
     * Serialize the strong-type properties, such as {@code featureId} of {@link FeatureFlagConfigurationSetting} into a
     * JSON format string, which is the {@code value} of {@link FeatureFlagConfigurationSetting}.
     *
     * @param setting the {@link FeatureFlagConfigurationSetting}.
     * @return a JSON format string that represents the {@code value} of {@link FeatureFlagConfigurationSetting}.
     * @throws IOException if {@link JsonWriter} can not be created.
     */
    public static String writeFeatureFlagConfigurationSetting(FeatureFlagConfigurationSetting setting)
        throws IOException {
        // The setting's value is expected to be a JSON string for the FeatureFlagConfigurationSetting,
        // so it is better to use another JSON generator to constructor the value as JSON string, flush into the
        // StringWriter.
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            jsonWriter.writeStartObject();

            jsonWriter.writeStringField(ID, setting.getFeatureId());
            jsonWriter.writeStringField(DESCRIPTION, setting.getDescription());
            jsonWriter.writeStringField(DISPLAY_NAME, setting.getDisplayName());
            jsonWriter.writeBooleanField(ENABLED, setting.isEnabled());

            jsonWriter.writeStartObject(CONDITIONS);
            jsonWriter.writeArrayField(CLIENT_FILTERS, setting.getClientFilters(), (writer, filter) -> {
                writer.writeStartObject();
                writer.writeStringField(NAME, filter.getName());
                writer.writeMapField(PARAMETERS, filter.getParameters(), JsonWriter::writeUntyped);
                writer.writeEndObject(); // each filter object
            });

            jsonWriter.writeEndObject();

            jsonWriter.writeEndObject();
            jsonWriter.flush();

            return outputStream.toString(StandardCharsets.UTF_8.name());
        }
    }
}
