// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.implementation.DeletedKeyHelper;
import com.azure.security.keyvault.keys.implementation.KeyVaultKeyHelper;
import com.azure.security.keyvault.keys.implementation.KeyVaultKeysUtils;

import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * Deleted Key is the resource consisting of name, recovery id, deleted date, scheduled purge date and its attributes
 * inherited from {@link KeyVaultKey}.
 * It is managed by Key Service.
 *
 * @see KeyClient
 * @see KeyAsyncClient
 */
public final class DeletedKey extends KeyVaultKey {
    static {
        DeletedKeyHelper.setAccessor(new DeletedKeyHelper.DeletedKeyAccessor() {
            @Override
            public void setRecoveryId(DeletedKey deletedKey, String recoveryId) {
                deletedKey.recoveryId = recoveryId;
            }

            @Override
            public void setScheduledPurgeDate(DeletedKey deletedKey, OffsetDateTime scheduledPurgeDate) {
                deletedKey.scheduledPurgeDate = scheduledPurgeDate;
            }

            @Override
            public void setDeletedOn(DeletedKey deletedKey, OffsetDateTime deletedOn) {
                deletedKey.deletedOn = deletedOn;
            }
        });
    }

    /**
     * The url of the recovery object, used to identify and recover the deleted
     * key.
     */
    private String recoveryId;

    /**
     * The time when the key is scheduled to be purged, in UTC.
     */
    private OffsetDateTime scheduledPurgeDate;

    /**
     * The time when the key was deleted, in UTC.
     */
    private OffsetDateTime deletedOn;

    /**
     * Get the recoveryId identifier.
     *
     * @return the recoveryId identifier.
     */
    public String getRecoveryId() {
        return this.recoveryId;
    }

    /**
     * Get the scheduled purge UTC time.
     *
     * @return the scheduledPurgeDate UTC time.
     */
    public OffsetDateTime getScheduledPurgeDate() {
        return scheduledPurgeDate;
    }

    /**
     * Get the deleted UTC time.
     *
     * @return the deletedDate UTC time.
     */
    public OffsetDateTime getDeletedOn() {
        return this.deletedOn;
    }

    /**
     * Get the key value.
     *
     * @return the key value
     */
    public JsonWebKey getKey() {
        return super.getKey();
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeJsonField("key", getKey())
            .writeStringField("recoveryId", recoveryId)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link DeletedKey}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return An instance of {@link DeletedKey} that the JSON stream represented, may return null.
     * @throws IOException If a {@link DeletedKey} fails to be read from the {@code jsonReader}.
     */
    public static DeletedKey fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            DeletedKey key = new DeletedKey();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("key".equals(fieldName)) {
                    KeyVaultKeyHelper.setKey(key, JsonWebKey.fromJson(reader));
                    KeyVaultKeysUtils.unpackId(key.getKey().getId(), name -> key.properties.name = name,
                        version -> key.properties.version = version);
                } else if ("attributes".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("enabled".equals(fieldName)) {
                            key.properties.enabled = reader.getNullable(JsonReader::getBoolean);
                        } else if ("exportable".equals(fieldName)) {
                            key.properties.exportable = reader.getNullable(JsonReader::getBoolean);
                        } else if ("nbf".equals(fieldName)) {
                            key.properties.notBefore = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                        } else if ("exp".equals(fieldName)) {
                            key.properties.expiresOn = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                        } else if ("created".equals(fieldName)) {
                            key.properties.createdOn = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                        } else if ("updated".equals(fieldName)) {
                            key.properties.updatedOn = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                        } else if ("recoveryLevel".equals(fieldName)) {
                            key.properties.recoveryLevel = reader.getString();
                        } else if ("recoverableDays".equals(fieldName)) {
                            key.properties.recoverableDays = reader.getNullable(JsonReader::getInt);
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else if ("tags".equals(fieldName)) {
                    key.properties.setTags(reader.readMap(JsonReader::getString));
                } else if ("managed".equals(fieldName)) {
                    key.properties.managed = reader.getNullable(JsonReader::getBoolean);
                } else if ("release_policy".equals(fieldName)) {
                    key.properties.setReleasePolicy(KeyReleasePolicy.fromJson(reader));
                } else if ("recoveryId".equals(fieldName)) {
                    key.recoveryId = reader.getString();
                } else if ("scheduledPurgeDate".equals(fieldName)) {
                    key.scheduledPurgeDate = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                } else if ("deletedDate".equals(fieldName)) {
                    key.deletedOn = reader.getNullable(KeyVaultKeysUtils::epochToOffsetDateTime);
                } else {
                    reader.skipChildren();
                }
            }

            return key;
        });
    }
}
