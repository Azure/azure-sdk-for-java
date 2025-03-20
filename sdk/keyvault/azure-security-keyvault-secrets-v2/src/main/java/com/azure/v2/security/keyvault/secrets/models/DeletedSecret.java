// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.secrets.models;

import com.azure.v2.security.keyvault.secrets.SecretClient;
import com.azure.v2.security.keyvault.secrets.implementation.DeletedSecretHelper;
import com.azure.v2.security.keyvault.secrets.implementation.models.SecretsModelsUtils;
import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * Deleted Secret is the resource consisting of name, recovery id, deleted date, scheduled purge date and its attributes
 * inherited from {@link KeyVaultSecret}.
 * It is managed by Secret Service.
 *
 * @see SecretClient
 */
@Metadata(properties = { MetadataProperties.FLUENT })
public final class DeletedSecret extends KeyVaultSecret {
    static {
        DeletedSecretHelper.setAccessor(new DeletedSecretHelper.DeletedSecretAccessor() {
            @Override
            public void setId(DeletedSecret deletedSecret, String id) {
                deletedSecret.properties.id = id;
            }

            @Override
            public void setVersion(DeletedSecret deletedSecret, String version) {
                deletedSecret.properties.version = version;
            }

            @Override
            public void setCreatedOn(DeletedSecret deletedSecret, OffsetDateTime createdOn) {
                deletedSecret.properties.createdOn = createdOn;
            }

            @Override
            public void setUpdatedOn(DeletedSecret deletedSecret, OffsetDateTime updatedOn) {
                deletedSecret.properties.updatedOn = updatedOn;
            }

            @Override
            public void setName(DeletedSecret deletedSecret, String name) {
                deletedSecret.properties.name = name;
            }

            @Override
            public void setRecoveryLevel(DeletedSecret deletedSecret, String recoveryLevel) {
                deletedSecret.properties.recoveryLevel = recoveryLevel;
            }

            @Override
            public void setKeyId(DeletedSecret deletedSecret, String keyId) {
                deletedSecret.properties.keyId = keyId;
            }

            @Override
            public void setManaged(DeletedSecret deletedSecret, Boolean managed) {
                deletedSecret.properties.managed = managed;
            }

            @Override
            public void setRecoverableDays(DeletedSecret deletedSecret, Integer recoverableDays) {
                deletedSecret.properties.recoverableDays = recoverableDays;
            }

            @Override
            public void setRecoveryId(DeletedSecret deletedSecret, String recoveryId) {
                deletedSecret.recoveryId = recoveryId;
            }

            @Override
            public void setScheduledPurgeDate(DeletedSecret deletedSecret, OffsetDateTime scheduledPurgeDate) {
                deletedSecret.scheduledPurgeDate = scheduledPurgeDate;
            }

            @Override
            public void setDeletedOn(DeletedSecret deletedSecret, OffsetDateTime deletedOn) {
                deletedSecret.deletedOn = deletedOn;
            }
        });
    }

    /**
     * The url of the recovery object, used to identify and recover the deleted secret.
     */
    private String recoveryId;

    /**
     * The time when the secret is scheduled to be purged, in UTC.
     */
    private OffsetDateTime scheduledPurgeDate;

    /**
     * The time when the secret was deleted, in UTC.
     */
    private OffsetDateTime deletedOn;

    /**
     * Creates a new instance of {@link DeletedSecret}.
     */
    public DeletedSecret() {
        super();
    }

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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("value", getValue())
            .writeStringField("recoveryId", recoveryId)
            .writeEndObject();
    }

    /**
     * Reads an instance of {@link DeletedSecret} from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of {@link DeletedSecret} if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the {@link DeletedSecret}.
     */
    public static DeletedSecret fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            DeletedSecret deletedSecret = new DeletedSecret();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("value".equals(fieldName)) {
                    deletedSecret.value = reader.getString();
                } else if ("id".equals(fieldName)) {
                    deletedSecret.properties.id = reader.getString();
                    SecretsModelsUtils.unpackId(deletedSecret.properties.id,
                        name -> deletedSecret.properties.name = name,
                        version -> deletedSecret.properties.version = version);
                } else if ("attributes".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    SecretProperties.deserializeAttributes(reader, deletedSecret.properties);
                } else if ("managed".equals(fieldName)) {
                    deletedSecret.properties.managed = reader.getNullable(JsonReader::getBoolean);
                } else if ("kid".equals(fieldName)) {
                    deletedSecret.properties.keyId = reader.getString();
                } else if ("contentType".equals(fieldName)) {
                    deletedSecret.properties.contentType = reader.getString();
                } else if ("tags".equals(fieldName)) {
                    deletedSecret.properties.tags = reader.readMap(JsonReader::getString);
                } else if ("recoveryId".equals(fieldName)) {
                    deletedSecret.recoveryId = reader.getString();
                } else if ("scheduledPurgeDate".equals(fieldName)) {
                    deletedSecret.scheduledPurgeDate = reader.getNullable(SecretsModelsUtils::epochToOffsetDateTime);
                } else if ("deletedDate".equals(fieldName)) {
                    deletedSecret.deletedOn = reader.getNullable(SecretsModelsUtils::epochToOffsetDateTime);
                } else {
                    reader.skipChildren();
                }
            }

            return deletedSecret;
        });
    }
}
