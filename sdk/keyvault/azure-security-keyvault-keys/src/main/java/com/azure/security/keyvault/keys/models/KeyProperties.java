// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.implementation.KeyPropertiesHelper;
import com.azure.security.keyvault.keys.implementation.KeyVaultKeysUtils;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * {@link KeyProperties} is the resource containing all the properties of the key except its {@link JsonWebKey}
 * material. It is managed by the Key Service.
 *
 * @see KeyClient
 * @see KeyAsyncClient
 */
@Fluent
public class KeyProperties implements JsonSerializable<KeyProperties> {
    static {
        KeyPropertiesHelper.setAccessor(new KeyPropertiesHelper.KeyPropertiesAccessor() {
            @Override
            public void setCreatedOn(KeyProperties keyProperties, OffsetDateTime createdOn) {
                keyProperties.createdOn = createdOn;
            }

            @Override
            public void setUpdatedOn(KeyProperties keyProperties, OffsetDateTime updatedOn) {
                keyProperties.updatedOn = updatedOn;
            }

            @Override
            public void setRecoveryLevel(KeyProperties keyProperties, String recoveryLevel) {
                keyProperties.recoveryLevel = recoveryLevel;
            }

            @Override
            public void setName(KeyProperties keyProperties, String name) {
                keyProperties.name = name;
            }

            @Override
            public void setVersion(KeyProperties keyProperties, String version) {
                keyProperties.version = version;
            }

            @Override
            public void setId(KeyProperties keyProperties, String id) {
                keyProperties.id = id;
            }

            @Override
            public void setManaged(KeyProperties keyProperties, Boolean managed) {
                keyProperties.managed = managed;
            }

            @Override
            public void setRecoverableDays(KeyProperties keyProperties, Integer recoverableDays) {
                keyProperties.recoverableDays = recoverableDays;
            }

            @Override
            public void setHsmPlatform(KeyProperties keyProperties, String hsmPlatform) {
                keyProperties.hsmPlatform = hsmPlatform;
            }
        });
    }
    /**
     * Determines whether the object is enabled.
     */
    Boolean enabled;

    /**
     * Indicates if the private key can be exported.
     */
    Boolean exportable;

    /**
     * Not before date in UTC.
     */
    OffsetDateTime notBefore;

    /**
     * The key version.
     */
    String version;

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
     * Reflects the deletion recovery level currently in effect for keys in the current vault. If it contains
     * 'Purgeable', the key can be permanently deleted by a privileged user; otherwise, only the system can purge the
     * key, at the end of the retention interval. Possible values include: 'Purgeable', 'Recoverable+Purgeable',
     * 'Recoverable', 'Recoverable+ProtectedSubscription'.
     */
    String recoveryLevel;

    /**
     * The key name.
     */
    String name;

    /**
     * Key identifier.
     */
    String id;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    private Map<String, String> tags;

    /**
     * True if the key's lifetime is managed by key vault. If this is a key backing a certificate, then managed will
     * be true.
     */
    Boolean managed;

    /**
     * The number of days a key is retained before being deleted for a soft delete-enabled Key Vault.
     */
    Integer recoverableDays;

    /**
     * The policy rules under which the key can be exported.
     */
    KeyReleasePolicy releasePolicy;

    /**
     * The underlying HSM Platform the key was generated with.
     */
    private String hsmPlatform;

    /**
     * Creates a new instance of {@link KeyProperties}.
     */
    public KeyProperties() {
    }

    /**
     * Gets the number of days a key is retained before being deleted for a soft delete-enabled Key Vault.
     *
     * @return The recoverable days.
     */
    public Integer getRecoverableDays() {
        return recoverableDays;
    }

    /**
     * Get the policy rules under which the key can be exported.
     *
     * @return The policy rules under which the key can be exported.
     */
    public KeyReleasePolicy getReleasePolicy() {
        return this.releasePolicy;
    }

    /**
     * Set the policy rules under which the key can be exported.
     *
     * @param releasePolicy The policy rules to set.
     *
     * @return The updated {@link KeyProperties} object.
     */
    public KeyProperties setReleasePolicy(KeyReleasePolicy releasePolicy) {
        this.releasePolicy = releasePolicy;

        return this;
    }

    /**
     * Get the key recovery level.
     *
     * @return The key recovery level.
     */
    public String getRecoveryLevel() {
        return this.recoveryLevel;
    }

    /**
     * Get the key name.
     *
     * @return The name of the key.
     */
    public String getName() {
        return this.name;
    }


    /**
     * Get the enabled value.
     *
     * @return The enabled value.
     */
    public Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set a value that indicates if the key is enabled.
     *
     * @param enabled The enabled value to set.
     *
     * @return The updated {@link KeyProperties} object.
     */
    public KeyProperties setEnabled(Boolean enabled) {
        this.enabled = enabled;

        return this;
    }

    /**
     * Get a flag that indicates if the private key can be exported.
     *
     * @return A flag that indicates if the private key can be exported.
     */
    public Boolean isExportable() {
        return this.exportable;
    }

    /**
     * Set a flag that indicates if the private key can be exported.
     *
     * @param exportable A flag that indicates if the private key can be exported.
     *
     * @return The updated {@link KeyProperties} object.
     */
    public KeyProperties setExportable(Boolean exportable) {
        this.exportable = exportable;

        return this;
    }

    /**
     * Get the {@link OffsetDateTime key's notBefore time} in UTC.
     *
     * @return The {@link OffsetDateTime key's notBefore time} in UTC.
     */
    public OffsetDateTime getNotBefore() {
        return notBefore;
    }

    /**
     * Set the {@link OffsetDateTime key's notBefore time} in UTC.
     *
     * @param notBefore The {@link OffsetDateTime key's notBefore time} in UTC.
     *
     * @return The updated {@link KeyProperties} object.
     */
    public KeyProperties setNotBefore(OffsetDateTime notBefore) {
        this.notBefore = notBefore;

        return this;
    }

    /**
     * Get the {@link OffsetDateTime key expiration time} in UTC.
     *
     * @return The {@link OffsetDateTime key expiration time} in UTC.
     */
    public OffsetDateTime getExpiresOn() {
        return this.expiresOn;
    }

    /**
     * Set the {@link OffsetDateTime key expiration time} in UTC.
     *
     * @param expiresOn The {@link OffsetDateTime key expiration time} in UTC.
     *
     * @return The updated {@link KeyProperties} object.
     */
    public KeyProperties setExpiresOn(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;

        return this;
    }

    /**
     * Get the {@link OffsetDateTime time at which key was created} in UTC.
     *
     * @return The {@link OffsetDateTime time at which key was created} in UTC.
     */
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    /**
     * Get the {@link OffsetDateTime time at which key was last updated} in UTC.
     *
     * @return The {@link OffsetDateTime time at which key was last updated} in UTC.
     */
    public OffsetDateTime getUpdatedOn() {
        return updatedOn;
    }

    /**
     * Get the key identifier.
     *
     * @return The key identifier.
     */
    public String getId() {
        return this.id;
    }


    /**
     * Get the tags associated with the key.
     *
     * @return The tag names and values.
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Set the tags to be associated with the key.
     *
     * @param tags The tags to set.
     *
     * @return The updated {@link KeyProperties} object.
     */
    public KeyProperties setTags(Map<String, String> tags) {
        this.tags = tags;

        return this;
    }

    /**
     * Get the managed value.
     *
     * @return The managed value.
     */
    public Boolean isManaged() {
        return this.managed;
    }

    /**
     * Get the version of the key.
     *
     * @return The version of the key.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Get the underlying HSM Platform the key was generated with.
     *
     * @return The key's underlying HSM Platform.
     */
    public String getHsmPlatform() {
        return hsmPlatform;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("kid", id)
            .writeMapField("tags", tags, JsonWriter::writeString)
            .writeJsonField("release_policy", releasePolicy)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link KeyProperties}.
    *
    * @param jsonReader The {@link JsonReader} being read.
    * @return An instance of {@link KeyProperties} that the JSON stream represented, may return null.
    * @throws IOException If a {@link KeyProperties} fails to be read from the {@code jsonReader}.
    */
    public static KeyProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            KeyProperties properties = new KeyProperties();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("kid".equals(fieldName)) {
                    properties.id = reader.getString();
                    KeyVaultKeysUtils.unpackId(properties.id, name -> properties.name = name,
                        version -> properties.version = version);
                } else if ("tags".equals(fieldName)) {
                    properties.tags = reader.readMap(JsonReader::getString);
                } else if ("immutable".equals(fieldName)) {
                    properties.managed = reader.getNullable(JsonReader::getBoolean);
                } else if ("recoverableDays".equals(fieldName)) {
                    properties.recoverableDays = reader.getNullable(JsonReader::getInt);
                } else if ("release_policy".equals(fieldName)) {
                    properties.releasePolicy = KeyReleasePolicy.fromJson(reader);
                } else if ("attributes".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("enabled".equals(fieldName)) {
                            properties.enabled = reader.getNullable(JsonReader::getBoolean);
                        } else if ("exportable".equals(fieldName)) {
                            properties.exportable = reader.getNullable(JsonReader::getBoolean);
                        } else if ("nbf".equals(fieldName)) {
                            properties.notBefore = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                        } else if ("exp".equals(fieldName)) {
                            properties.expiresOn = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                        } else if ("created".equals(fieldName)) {
                            properties.createdOn = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                        } else if ("updated".equals(fieldName)) {
                            properties.updatedOn = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                        } else if ("recoveryLevel".equals(fieldName)) {
                            properties.recoveryLevel = reader.getString();
                        } else if ("recoverableDays".equals(fieldName)) {
                            properties.recoverableDays = reader.getNullable(JsonReader::getInt);
                        } else if ("hsmPlatform".equals(fieldName)) {
                            properties.hsmPlatform = reader.getString();
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else {
                    reader.skipChildren();
                }
            }

            return properties;
        });
    }
}
