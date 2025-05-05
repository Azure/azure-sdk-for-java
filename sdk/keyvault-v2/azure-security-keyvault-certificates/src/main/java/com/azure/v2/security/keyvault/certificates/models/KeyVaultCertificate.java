// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.certificates.models;

import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateAttributes;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static io.clientcore.core.utils.CoreUtils.arrayCopy;

/**
 * Represents a certificate with all of its properties.
 */
public class KeyVaultCertificate implements JsonSerializable<KeyVaultCertificate> {
    /**
     * CER contents of x509 certificate.
     */
    private final byte[] cer;

    /**
     * The key id.
     */
    private final String keyId;

    /**
     * The secret id.
     */
    private final String secretId;

    private CertificateProperties properties;

    KeyVaultCertificate() {
        this(null, null, null, new CertificateProperties());
    }

    KeyVaultCertificate(byte[] cer, String keyId, String secretId, CertificateProperties properties) {
        this.cer = arrayCopy(cer);
        this.keyId = keyId;
        this.secretId = secretId;
        this.properties = properties;
    }

    /**
     * Get the certificate properties.
     * @return the certificate properties.
     */
    public CertificateProperties getProperties() {
        return properties;
    }

    /**
     * Set the certificate properties
     * @param properties the certificate properties
     * @throws NullPointerException if {@code certificateProperties} is null
     * @return the updated certificate object itself.
     */
    public KeyVaultCertificate setProperties(CertificateProperties properties) {
        Objects.requireNonNull(properties, "The certificate properties cannot be null");
        properties.setName(this.properties.getName());
        this.properties = properties;
        return this;
    }

    /**
     * Get the certificate identifier
     * @return the certificate identifier
     */
    public String getId() {
        return properties.getId();
    }

    /**
     * Get the certificate name
     * @return the certificate name
     */
    public String getName() {
        return properties.getName();
    }

    /**
     * Get the key id of the certificate
     * @return the key Id.
     */
    public String getKeyId() {
        return this.keyId;
    }

    /**
     * Get the secret id of the certificate
     * @return the secret Id.
     */
    public String getSecretId() {
        return this.secretId;
    }

    /**
     * Get the cer content of the certificate
     * @return the cer content.
     */
    public byte[] getCer() {
        return arrayCopy(cer);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject().writeBinaryField("cer", cer).writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link KeyVaultCertificate}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link KeyVaultCertificate} that the JSON stream represented, may return null.
     * @throws IOException If a {@link KeyVaultCertificate} fails to be read from the {@code jsonReader}.
     */
    public static KeyVaultCertificate fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String id = null;
            CertificateAttributes attributes = null;
            Map<String, String> tags = null;
            byte[] wireThumbprint = null;
            byte[] cer = null;
            String keyId = null;
            String secretId = null;

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
                } else {
                    reader.skipChildren();
                }
            }

            return new KeyVaultCertificate(cer, keyId, secretId,
                new CertificateProperties(id, attributes, tags, wireThumbprint, null));
        });
    }
}
