// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.azure.data.appconfiguration.implementation.Utility.URI;

/**
 * {@link SecretReferenceConfigurationSetting} model. It represents a configuration setting that references as
 * KeyVault secret.
 */
@Fluent
public final class SecretReferenceConfigurationSetting extends ConfigurationSetting {
    private static final ClientLogger LOGGER = new ClientLogger(SecretReferenceConfigurationSetting.class);

    private String secretId;
    private static final String SECRET_REFERENCE_CONTENT_TYPE
        = "application/vnd.microsoft.appconfig.keyvaultref+json;charset=utf-8";

    // The flag to indicate if the 'value' field is valid. It is a temporary field to store the flag.
    // If the 'value' field is not valid, we will throw an exception when user try to access the strongly-typed
    // properties.
    private boolean isValidSecretReferenceValue;
    private final Map<String, Object> parsedProperties = new LinkedHashMap<>(1);

    /**
     * The constructor for a secret reference configuration setting.
     *
     * @param key A key name for this configuration setting.
     * @param secretId A uri value that used to in the JSON value of setting. e.x., {"uri":"{secretId}"}.
     */
    public SecretReferenceConfigurationSetting(String key, String secretId) {
        isValidSecretReferenceValue = true;

        this.secretId = secretId;
        super.setKey(key);
        super.setValue("{\"uri\":\"" + secretId + "\"}");
        super.setContentType(SECRET_REFERENCE_CONTENT_TYPE);
    }

    /**
     * Get the secret ID value of this configuration setting.
     *
     * @return the secret ID value of this configuration setting.
     * @throws IllegalArgumentException if the setting's {@code value} is an invalid JSON format.
     */
    public String getSecretId() {
        checkValid();
        return secretId;
    }

    /**
     * Set the secret ID value of this configuration setting.
     *
     * @param secretId the secret ID value of this configuration setting.
     *
     * @return The updated {@link SecretReferenceConfigurationSetting} object.
     * @throws IllegalArgumentException if the setting's {@code value} is an invalid JSON format.
     */
    public SecretReferenceConfigurationSetting setSecretId(String secretId) {
        checkValid();
        this.secretId = secretId;
        return this;
    }

    /**
     * Sets the key of this setting.
     *
     * @param key The key to associate with this configuration setting.
     *
     * @return The updated {@link FeatureFlagConfigurationSetting} object.
     */
    @Override
    public SecretReferenceConfigurationSetting setKey(String key) {
        super.setKey(key);
        return this;
    }

    @Override
    public String getValue() {
        // Lazily update: Update 'value' by all latest property values when this getValue() method is called.
        String newValue = null;
        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final JsonWriter writer = JsonProviders.createWriter(outputStream);

            boolean isUriWritten = false;

            writer.writeStartObject();
            // If 'value' has value, and it is a valid JSON, we need to parse it and write it back.
            for (Map.Entry<String, Object> entry : parsedProperties.entrySet()) {
                final String name = entry.getKey();
                final Object jsonValue = entry.getValue();
                try {
                    // Try to write the known property. If it is a known property, we need to remove it from the
                    // temporary 'knownProperties' bag.
                    if (URI.equals(name)) {
                        writer.writeStringField(URI, secretId);
                        isUriWritten = true;
                    } else {
                        // Unknown extension property. We need to keep it.
                        writer.writeUntypedField(name, jsonValue);
                    }
                } catch (IOException e) {
                    throw LOGGER.logExceptionAsError(new RuntimeException(e));
                }
            }

            if (!isUriWritten) {
                writer.writeStringField(URI, secretId);
            }

            writer.writeEndObject();
            writer.flush();

            newValue = outputStream.toString(StandardCharsets.UTF_8.name());
            outputStream.close();
        } catch (IOException exception) {
            LOGGER.logExceptionAsError(
                new IllegalArgumentException("Can't parse Secret Reference configuration setting value.", exception));
        }

        super.setValue(newValue);
        return newValue;
    }

    /**
     * Sets the value of this setting.
     *
     * @param value The value to associate with this configuration setting.
     *
     * @return The updated {@link SecretReferenceConfigurationSetting} object.
     * @throws IllegalArgumentException if the setting's {@code value} is an invalid JSON format.
     */
    @Override
    public SecretReferenceConfigurationSetting setValue(String value) {
        tryParseValue(value);
        isValidSecretReferenceValue = true;
        super.setValue(value);
        return this;
    }

    /**
     * Sets the label of this configuration setting. {@link #NO_LABEL} is the default label used when this value is not
     * set.
     *
     * @param label The label of this configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    @Override
    public SecretReferenceConfigurationSetting setLabel(String label) {
        super.setLabel(label);
        return this;
    }

    /**
     * Sets the content type. By default, the content type is null.
     *
     * @param contentType The content type of this configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    @Override
    public SecretReferenceConfigurationSetting setContentType(String contentType) {
        super.setContentType(contentType);
        return this;
    }

    /**
     * Sets the ETag for this configuration setting.
     *
     * @param etag The ETag for the configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    @Override
    public SecretReferenceConfigurationSetting setETag(String etag) {
        super.setETag(etag);
        return this;
    }

    /**
     * Sets the tags for this configuration setting.
     *
     * @param tags The tags to add to this configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    @Override
    public SecretReferenceConfigurationSetting setTags(Map<String, String> tags) {
        super.setTags(tags);
        return this;
    }

    private void checkValid() {
        if (!isValidSecretReferenceValue) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("The content of the " + super.getValue()
                + " property do not represent a valid secret reference configuration setting."));
        }
    }

    // Given JSON string value, try to parse it and store the parsed properties to the 'parsedProperties' field.
    // If the parsing is successful, updates the strongly-type property and preserves the unknown properties to
    // 'parsedProperties' which we will use later in getValue() to get the unknown properties.
    // Otherwise, set the flag variable 'isValidSecretReferenceValue' = false and throw an exception.
    private void tryParseValue(String value) {
        parsedProperties.clear();

        try (JsonReader jsonReader = JsonProviders.createReader(value)) {
            jsonReader.readObject(reader -> {
                boolean isSecretIdUriValid = false;
                String secreteIdUri = this.secretId;

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    final String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if (URI.equals(fieldName)) {
                        final String secretIdClone = reader.getString();
                        secreteIdUri = secretIdClone;
                        parsedProperties.put(URI, secreteIdUri);
                        isSecretIdUriValid = true;
                    } else {
                        // The extension property is possible, we should not skip it.
                        parsedProperties.put(fieldName, reader.readUntyped());
                    }
                }

                // update strongly-typed property, 'secretId'.
                this.secretId = secreteIdUri;
                return isSecretIdUriValid;
            });
        } catch (IOException e) {
            isValidSecretReferenceValue = false;
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(e));
        }
    }
}
