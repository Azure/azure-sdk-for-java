// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.util.Map;

import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonDeserializer.readSecretReferenceConfigurationSettingValue;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.writeSecretReferenceConfigurationSetting;

/**
 * {@link SecretReferenceConfigurationSetting} model. It represents a configuration setting that references as
 * KeyVault secret.
 */
@Fluent
public final class SecretReferenceConfigurationSetting extends ConfigurationSetting {
    private static final ClientLogger LOGGER = new ClientLogger(SecretReferenceConfigurationSetting.class);

    private String secretId;
    private static final String SECRET_REFERENCE_CONTENT_TYPE =
        "application/vnd.microsoft.appconfig.keyvaultref+json;charset=utf-8";

    /**
     * The constructor for a secret reference configuration setting.
     *
     * @param key A key name for this configuration setting.
     * @param secretId A uri value that used to in the JSON value of setting. e.x., {"uri":"{secretId}"}.
     */
    public SecretReferenceConfigurationSetting(String key, String secretId) {
        this.secretId = secretId;
        super.setKey(key);
        super.setValue("{\"uri\":\"" + secretId + "\"}");
        super.setContentType(SECRET_REFERENCE_CONTENT_TYPE);
    }

    /**
     * Get the secret ID value of this configuration setting.
     *
     * @return the secret ID value of this configuration setting.
     */
    public String getSecretId() {
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
        this.secretId = secretId;
        updateSettingValue();
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
        super.setValue(value);
        // update strongly-typed properties.
        final SecretReferenceConfigurationSetting updatedSetting = readSecretReferenceConfigurationSettingValue(
            super.getKey(), value);
        this.secretId = updatedSetting.getSecretId();
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

    private void updateSettingValue() {
        try {
            super.setValue(writeSecretReferenceConfigurationSetting(this));
        } catch (IOException exception) {
            LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Can't parse Secret Reference configuration setting value.", exception));
        }
    }
}
