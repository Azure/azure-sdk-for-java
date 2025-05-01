// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.implementation.CertificatePolicyHelper;
import com.azure.security.keyvault.certificates.implementation.DeletedCertificateHelper;
import com.azure.security.keyvault.certificates.implementation.models.CertificateAttributes;
import com.azure.security.keyvault.certificates.implementation.models.DeletedCertificateBundle;
import com.azure.security.keyvault.certificates.implementation.models.DeletedCertificateItem;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * Deleted Certificate is the resource consisting of name, recovery id, deleted date, scheduled purge date and its
 * attributes inherited from {@link KeyVaultCertificate}.
 * It is managed by Certificate Service.
 *
 * @see CertificateAsyncClient
 * @see CertificateClient
 */
public final class DeletedCertificate extends KeyVaultCertificateWithPolicy {
    static {
        DeletedCertificateHelper.setAccessor(new DeletedCertificateHelper.DeletedCertificateAccessor() {
            @Override
            public DeletedCertificate createDeletedCertificate(DeletedCertificateItem item) {
                return new DeletedCertificate(item);
            }

            @Override
            public DeletedCertificate createDeletedCertificate(DeletedCertificateBundle bundle) {
                return new DeletedCertificate(bundle);
            }
        });
    }

    /**
     * Creates an instance of {@link DeletedCertificate}.
     */
    public DeletedCertificate() {
        super();

        this.recoveryId = null;
        this.deletedOn = null;
        this.scheduledPurgeDate = null;
    }

    private DeletedCertificate(DeletedCertificateItem item) {
        this(null, null, null, new CertificateProperties(item), null, item.getRecoveryId(), item.getDeletedDate(),
            item.getScheduledPurgeDate());
    }

    private DeletedCertificate(DeletedCertificateBundle bundle) {
        this(bundle.getCer(), bundle.getKid(), bundle.getSid(), new CertificateProperties(bundle),
            CertificatePolicyHelper.createCertificatePolicy(bundle.getPolicy()), bundle.getRecoveryId(),
            bundle.getDeletedDate(), bundle.getScheduledPurgeDate());
    }

    private DeletedCertificate(byte[] cer, String kid, String sid, CertificateProperties properties,
        CertificatePolicy policy, String recoveryId, OffsetDateTime deletedOn, OffsetDateTime scheduledPurgeDate) {

        super(cer, kid, sid, properties, policy);

        this.recoveryId = recoveryId;
        this.deletedOn = deletedOn;
        this.scheduledPurgeDate = scheduledPurgeDate;
    }

    /**
     * The url of the recovery object, used to identify and recover the deleted
     * certificate.
     */
    private final String recoveryId;

    /**
     * The time when the certificate is scheduled to be purged, in UTC.
     */
    private final OffsetDateTime scheduledPurgeDate;

    /**
     * The time when the certificate was deleted, in UTC.
     */
    private final OffsetDateTime deletedOn;

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
            .writeBinaryField("cer", getCer())
            .writeJsonField("policy", getPolicy())
            .writeStringField("recoverId", recoveryId)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link DeletedCertificate}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link DeletedCertificate} that the JSON stream represented, may return null.
     * @throws IOException If a {@link DeletedCertificate} fails to be read from the {@code jsonReader}.
     */
    public static DeletedCertificate fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String id = null;
            CertificateAttributes attributes = null;
            Map<String, String> tags = null;
            byte[] wireThumbprint = null;
            byte[] cer = null;
            String keyId = null;
            String secretId = null;
            CertificatePolicy policy = null;
            String recoveryId = null;
            OffsetDateTime deletedOn = null;
            OffsetDateTime scheduledPurgeDate = null;
            boolean certificateOrderPreserved = false;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    id = reader.getString();
                } else if ("attributes".equals(fieldName)) {
                    attributes = CertificateAttributes.fromJson(reader);
                } else if ("tags".equals(fieldName)) {
                    tags = reader.readMap(JsonReader::getString);
                } else if ("x5t".equals(fieldName)) {
                    wireThumbprint = reader.getBinary();
                } else if ("cer".equals(fieldName)) {
                    cer = reader.getBinary();
                } else if ("kid".equals(fieldName)) {
                    keyId = reader.getString();
                } else if ("sid".equals(fieldName)) {
                    secretId = reader.getString();
                } else if ("policy".equals(fieldName)) {
                    policy = CertificatePolicy.fromJson(reader);
                } else if ("recoveryId".equals(fieldName)) {
                    recoveryId = reader.getString();
                } else if ("deletedDate".equals(fieldName)) {
                    deletedOn = reader.getNullable(nonNull -> OffsetDateTime
                        .ofInstant(Instant.ofEpochMilli(nonNull.getLong() * 1000L), ZoneOffset.UTC));
                } else if ("scheduledPurgeDate".equals(fieldName)) {
                    scheduledPurgeDate = reader.getNullable(nonNull -> OffsetDateTime
                        .ofInstant(Instant.ofEpochMilli(nonNull.getLong() * 1000L), ZoneOffset.UTC));
                } else if ("preserveCertOrder".equals(fieldName)) {
                    certificateOrderPreserved = reader.getBoolean();
                } else {
                    reader.skipChildren();
                }
            }

            return new DeletedCertificate(cer, keyId, secretId,
                new CertificateProperties(id, attributes, tags, wireThumbprint, null, certificateOrderPreserved),
                policy, recoveryId, deletedOn, scheduledPurgeDate);
        });
    }
}
