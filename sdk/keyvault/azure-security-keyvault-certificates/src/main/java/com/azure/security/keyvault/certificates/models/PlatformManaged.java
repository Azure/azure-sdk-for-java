// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Properties of the platform managed certificate.
 *
 * <p><strong>Warning: this feature is currently intended for internal (first-party) Key Vault use only.</strong>
 * It is exposed in the preview service version {@code 2026-03-01-preview} and is not generally available.
 * Third-party calls that supply a {@link CertificatePolicy} configured via
 * {@link CertificatePolicy#forPlatformManaged(PlatformManaged)} will be rejected by the service. The shape
 * accepted under {@link #setMetadata(Map)} and the values accepted for {@link #setCertificateUsage(String)}
 * are owned by the Key Vault service and may change without notice. Do not use this type in production code.
 */
@Fluent
public final class PlatformManaged implements JsonSerializable<PlatformManaged> {
    /*
     * The intended usage of the certificate.
     */
    private String certificateUsage;

    /*
     * JSON-formatted platform managed metadata.
     */
    private Map<String, Object> metadata;

    /**
     * Creates an instance of {@link PlatformManaged}.
     *
     * @param certificateUsage The intended usage of the certificate.
     */
    public PlatformManaged(String certificateUsage) {
        this.certificateUsage = Objects.requireNonNull(certificateUsage, "'certificateUsage' cannot be null.");
    }

    private PlatformManaged() {
    }

    /**
     * Get the certificate usage.
     *
     * @return the certificate usage.
     */
    public String getCertificateUsage() {
        return this.certificateUsage;
    }

    /**
     * Set the certificate usage.
     *
     * @param certificateUsage the certificate usage.
     * @return the updated PlatformManaged object itself.
     */
    public PlatformManaged setCertificateUsage(String certificateUsage) {
        this.certificateUsage = Objects.requireNonNull(certificateUsage, "'certificateUsage' cannot be null.");
        return this;
    }

    /**
     * Get the platform managed metadata.
     *
     * @return the platform managed metadata.
     */
    public Map<String, Object> getMetadata() {
        return this.metadata;
    }

    /**
     * Set the platform managed metadata.
     *
     * @param metadata the platform managed metadata.
     * @return the updated PlatformManaged object itself.
     */
    public PlatformManaged setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("certificateUsage", this.certificateUsage);
        if (this.metadata != null) {
            jsonWriter.writeUntypedField("metadata", this.metadata);
        }
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of PlatformManaged from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of PlatformManaged if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the PlatformManaged.
     * @throws IllegalStateException If the required {@code certificateUsage} property is missing.
     */
    public static PlatformManaged fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            PlatformManaged deserializedPlatformManaged = new PlatformManaged();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("certificateUsage".equals(fieldName)) {
                    deserializedPlatformManaged.certificateUsage = reader.getString();
                } else if ("metadata".equals(fieldName)) {
                    deserializedPlatformManaged.metadata = reader.readMap(JsonReader::readUntyped);
                } else {
                    reader.skipChildren();
                }
            }

            if (deserializedPlatformManaged.certificateUsage == null) {
                throw new IllegalStateException("Missing required property 'certificateUsage' on PlatformManaged.");
            }

            return deserializedPlatformManaged;
        });
    }
}
