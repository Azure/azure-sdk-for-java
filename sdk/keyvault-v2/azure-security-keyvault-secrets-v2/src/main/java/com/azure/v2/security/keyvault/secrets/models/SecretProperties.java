// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.secrets.models;

import com.azure.v2.security.keyvault.secrets.SecretClient;
import com.azure.v2.security.keyvault.secrets.implementation.SecretPropertiesHelper;
import com.azure.v2.security.keyvault.secrets.implementation.models.SecretsModelsUtils;
import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * SecretProperties is the resource containing all the properties of the secret except its value.
 * It is managed by the Secret Service.
 *
 *  @see SecretClient
 */
@Metadata(properties = { MetadataProperties.FLUENT })
public class SecretProperties implements JsonSerializable<SecretProperties> {
    static {
        SecretPropertiesHelper.setAccessor(new SecretPropertiesHelper.SecretPropertiesAccessor() {
            @Override
            public void setId(SecretProperties properties, String id) {
                properties.id = id;
            }

            @Override
            public void setVersion(SecretProperties properties, String version) {
                properties.version = version;
            }

            @Override
            public void setCreatedOn(SecretProperties properties, OffsetDateTime createdOn) {
                properties.createdOn = createdOn;
            }

            @Override
            public void setUpdatedOn(SecretProperties properties, OffsetDateTime updatedOn) {
                properties.updatedOn = updatedOn;
            }

            @Override
            public void setName(SecretProperties properties, String name) {
                properties.name = name;
            }

            @Override
            public void setRecoveryLevel(SecretProperties properties, String recoveryLevel) {
                properties.recoveryLevel = recoveryLevel;
            }

            @Override
            public void setKeyId(SecretProperties properties, String keyId) {
                properties.keyId = keyId;
            }

            @Override
            public void setManaged(SecretProperties properties, Boolean managed) {
                properties.managed = managed;
            }

            @Override
            public void setRecoverableDays(SecretProperties properties, Integer recoverableDays) {
                properties.recoverableDays = recoverableDays;
            }
        });
    }

    /**
     * The secret id.
     */
    String id;

    /**
     * The secret version.
     */
    String version;

    /**
     * Determines whether the object is enabled.
     */
    Boolean enabled;

    /**
     * Not before date in UTC.
     */
    OffsetDateTime notBefore;

    /**
     * Expiry date in UTC.
     */
    OffsetDateTime expiresOn;

    /**
     * Creation time in UTC.
     */
    OffsetDateTime createdOn;

    /**
     * Last updated time in UTC.
     */
    OffsetDateTime updatedOn;

    /**
     * The secret name.
     */
    String name;

    /**
     * Reflects the deletion recovery level currently in effect for secrets in
     * the current vault. If it contains 'Purgeable', the secret can be
     * permanently deleted by a privileged user; otherwise, only the system can
     * purge the secret, at the end of the retention interval. Possible values
     * include: 'Purgeable', 'Recoverable+Purgeable', 'Recoverable',
     * 'Recoverable+ProtectedSubscription'.
     */
    String recoveryLevel;

    /**
     * The content type of the secret.
     */
    String contentType;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    Map<String, String> tags;

    /**
     * If this is a secret backing a KV certificate, then this field specifies
     * the corresponding key backing the KV certificate.
     */
    String keyId;

    /**
     * True if the secret's lifetime is managed by key vault. If this is a
     * secret backing a certificate, then managed will be true.
     */
    Boolean managed;

    /**
     * The number of days a secret is retained before being deleted for a soft delete-enabled Key Vault.
     */
    Integer recoverableDays;

    SecretProperties(String secretName) {
        this.name = secretName;
    }

    /**
     * Creates empty instance of SecretProperties.
     */
    public SecretProperties() {
    }

    /**
     * Get the secret name.
     *
     * @return the name of the secret.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the recovery level of the secret.
    
     * @return the recoveryLevel of the secret.
     */
    public String getRecoveryLevel() {
        return recoveryLevel;
    }

    /**
     * Get the enabled value.
     *
     * @return the enabled value
     */
    public Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled The enabled value to set
     * @throws NullPointerException if {@code enabled} is null.
     * @return the SecretProperties object itself.
     */
    public SecretProperties setEnabled(Boolean enabled) {
        Objects.requireNonNull(enabled);
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the notBefore UTC time.
     *
     * @return the notBefore UTC time.
     */
    public OffsetDateTime getNotBefore() {
        return notBefore;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The notBefore UTC time to set
     * @return the SecretProperties object itself.
     */
    public SecretProperties setNotBefore(OffsetDateTime notBefore) {
        this.notBefore = notBefore;
        return this;
    }

    /**
     * Get the Secret Expiry time in UTC.
     *
     * @return the expires UTC time.
     */
    public OffsetDateTime getExpiresOn() {
        if (this.expiresOn == null) {
            return null;
        }
        return this.expiresOn;
    }

    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expiresOn The expiry time to set for the secret.
     * @return the SecretProperties object itself.
     */
    public SecretProperties setExpiresOn(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;
        return this;
    }

    /**
     * Get the the UTC time at which secret was created.
     *
     * @return the created UTC time.
     */
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    /**
     * Get the UTC time at which secret was last updated.
     *
     * @return the last updated UTC time.
     */
    public OffsetDateTime getUpdatedOn() {
        return updatedOn;
    }

    /**
     * Get the secret identifier.
     *
     * @return the secret identifier.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the content type.
     *
     * @return the content type.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Set the contentType.
     *
     * @param contentType The contentType to set
     * @return the updated SecretProperties object itself.
     */
    public SecretProperties setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Get the tags associated with the secret.
     *
     * @return the value of the tags.
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Set the tags to be associated with the secret.
     *
     * @param tags The tags to set
     * @return the updated SecretProperties object itself.
     */
    public SecretProperties setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the keyId identifier.
     *
     * @return the keyId identifier.
     */
    public String getKeyId() {
        return this.keyId;
    }

    /**
     * Get the managed value.
     *
     * @return the managed value
     */
    public Boolean isManaged() {
        return this.managed;
    }

    /**
     * Get the version of the secret.
     *
     * @return the version of the secret.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the number of days a secret is retained before being deleted for a soft delete-enabled Key Vault.
     * @return the recoverable days.
     */
    public Integer getRecoverableDays() {
        return recoverableDays;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("contentType", contentType)
            .writeMapField("tags", tags, JsonWriter::writeString)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link SecretProperties}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return An instance of {@link SecretProperties} that the JSON stream represented, may return null.
     * @throws IOException If a {@link SecretProperties} fails to be read from the {@code jsonReader}.
     */
    public static SecretProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SecretProperties secretProperties = new SecretProperties();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("contentType".equals(fieldName)) {
                    secretProperties.contentType = reader.getString();
                } else if ("tags".equals(fieldName)) {
                    secretProperties.tags = reader.readMap(JsonReader::getString);
                } else if ("kid".equals(fieldName)) {
                    secretProperties.keyId = reader.getString();
                } else if ("managed".equals(fieldName)) {
                    secretProperties.managed = reader.getNullable(JsonReader::getBoolean);
                } else if ("recoverableDays".equals(fieldName)) {
                    secretProperties.recoverableDays = reader.getNullable(JsonReader::getInt);
                } else if ("attributes".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    deserializeAttributes(reader, secretProperties);
                } else if ("id".equals(fieldName)) {
                    secretProperties.id = reader.getString();
                    SecretsModelsUtils.unpackId(secretProperties.id, name -> secretProperties.name = name,
                        version -> secretProperties.version = version);
                } else {
                    reader.skipChildren();
                }
            }

            return secretProperties;
        });
    }

    static void deserializeAttributes(JsonReader reader, SecretProperties secretProperties) throws IOException {
        while (reader.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = reader.getFieldName();
            reader.nextToken();

            if ("enabled".equals(fieldName)) {
                secretProperties.enabled = reader.getNullable(JsonReader::getBoolean);
            } else if ("nbf".equals(fieldName)) {
                secretProperties.notBefore = reader.getNullable(SecretsModelsUtils::epochToOffsetDateTime);
            } else if ("exp".equals(fieldName)) {
                secretProperties.expiresOn = reader.getNullable(SecretsModelsUtils::epochToOffsetDateTime);
            } else if ("created".equals(fieldName)) {
                secretProperties.createdOn = reader.getNullable(SecretsModelsUtils::epochToOffsetDateTime);
            } else if ("updated".equals(fieldName)) {
                secretProperties.updatedOn = reader.getNullable(SecretsModelsUtils::epochToOffsetDateTime);
            } else if ("recoveryLevel".equals(fieldName)) {
                secretProperties.recoveryLevel = reader.getString();
            } else if ("contentType".equals(fieldName)) {
                String contentType = reader.getString();
                secretProperties.contentType = contentType == null ? secretProperties.contentType : contentType;
            } else if ("keyId".equals(fieldName)) {
                String keyId = reader.getString();
                secretProperties.keyId = keyId == null ? secretProperties.keyId : keyId;
            } else if ("tags".equals(fieldName)) {
                Map<String, String> tags = reader.readMap(JsonReader::getString);
                secretProperties.tags = tags == null ? secretProperties.tags : tags;
            } else if ("managed".equals(fieldName)) {
                Boolean managed = reader.getNullable(JsonReader::getBoolean);
                secretProperties.managed = managed == null ? secretProperties.managed : managed;
            } else if ("recoverableDays".equals(fieldName)) {
                secretProperties.recoverableDays = reader.getNullable(JsonReader::getInt);
            } else if ("id".equals(fieldName)) {
                secretProperties.id = reader.getString();
                SecretsModelsUtils.unpackId(secretProperties.id, name -> secretProperties.name = name,
                    version -> secretProperties.version = version);
            } else {
                reader.skipChildren();
            }
        }
    }
}
