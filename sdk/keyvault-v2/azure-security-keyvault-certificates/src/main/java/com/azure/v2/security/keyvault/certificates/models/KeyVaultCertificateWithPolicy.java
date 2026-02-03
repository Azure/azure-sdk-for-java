// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.certificates.models;

import com.azure.v2.security.keyvault.certificates.implementation.CertificatePolicyHelper;
import com.azure.v2.security.keyvault.certificates.implementation.KeyVaultCertificateWithPolicyHelper;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateAttributes;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateBundle;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;
import java.util.Map;

/**
 * Represents a certificate with all of its properties including {@link CertificatePolicy}.
 */
public class KeyVaultCertificateWithPolicy extends KeyVaultCertificate {
    static {
        KeyVaultCertificateWithPolicyHelper.setAccessor(KeyVaultCertificateWithPolicy::new);
    }

    /**
     * The Certificate policy.
     */
    private CertificatePolicy policy;

    KeyVaultCertificateWithPolicy() {
        super();
    }

    KeyVaultCertificateWithPolicy(CertificateBundle bundle) {
        this(bundle.getCer(), bundle.getKid(), bundle.getSid(), new CertificateProperties(bundle),
            CertificatePolicyHelper.createCertificatePolicy(bundle.getPolicy()));
    }

    KeyVaultCertificateWithPolicy(byte[] cer, String kid, String sid, CertificateProperties properties) {
        this(cer, kid, sid, properties, null);
    }

    KeyVaultCertificateWithPolicy(byte[] cer, String kid, String sid, CertificateProperties properties,
        CertificatePolicy policy) {
        super(cer, kid, sid, properties);
        this.policy = policy;
    }

    /**
     * Set the certificate properties
     * @param properties the certificate properties
     * @throws NullPointerException if {@code certificateProperties} is null
     * @return the updated certificateWithPolicy object itself.
     */
    public KeyVaultCertificateWithPolicy setProperties(CertificateProperties properties) {
        super.setProperties(properties);
        return this;
    }

    /**
     * Get the certificate policy of the certificate
     * @return the cer content.
     */
    public CertificatePolicy getPolicy() {
        return this.policy;
    }

    /**
     * Set the certificate policy of the certificate
     *
     * @param certificatePolicy the policy to set.
     * @return the certificateWithPolicy object itself.
     */
    public KeyVaultCertificateWithPolicy setPolicy(CertificatePolicy certificatePolicy) {
        this.policy = certificatePolicy;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeBinaryField("cer", getCer())
            .writeJsonField("policy", policy)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link KeyVaultCertificateWithPolicy}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link KeyVaultCertificateWithPolicy} that the JSON stream represented, may return null.
     * @throws IOException If a {@link KeyVaultCertificateWithPolicy} fails to be read from the {@code jsonReader}.
     */
    public static KeyVaultCertificateWithPolicy fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String id = null;
            CertificateAttributes attributes = null;
            Map<String, String> tags = null;
            byte[] wireThumbprint = null;
            byte[] cer = null;
            String keyId = null;
            String secretId = null;
            CertificatePolicy policy = null;

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
                } else {
                    reader.skipChildren();
                }
            }

            return new KeyVaultCertificateWithPolicy(cer, keyId, secretId,
                new CertificateProperties(id, attributes, tags, wireThumbprint, null), policy);
        });
    }
}
