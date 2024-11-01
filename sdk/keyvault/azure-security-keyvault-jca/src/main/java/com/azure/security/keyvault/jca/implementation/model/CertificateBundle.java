// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.model;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * The CertificateBundle REST model.
 */
public class CertificateBundle implements JsonSerializable<CertificateBundle> {
    /**
     * Stores the CER bytes.
     */
    private String cer;

    /**
     * Stores the Key ID.
     */
    private String kid;

    /**
     * Stores the policy.
     */
    private CertificatePolicy policy;

    /**
     * Stores the Secret ID.
     */
    private String sid;

    /**
     * Get the CER string.
     *
     * @return the CER string.
     */
    public String getCer() {
        return cer;
    }

    /**
     * Get the Key ID.
     *
     * @return the Key ID.
     */
    public String getKid() {
        return kid;
    }

    /**
     * Get the policy.
     *
     * @return the policy.
     */
    public CertificatePolicy getPolicy() {
        return policy;
    }

    /**
     * Get the Secret ID.
     *
     * @return the Secret ID.
     */
    public String getSid() {
        return sid;
    }

    /**
     * Set the CER string.
     *
     * @param cer the CER string.
     */
    public void setCer(String cer) {
        this.cer = cer;
    }

    /**
     * Set the Key ID.
     *
     * @param kid the Key ID.
     */
    public void setKid(String kid) {
        this.kid = kid;
    }

    /**
     * Set the policy.
     *
     * @param policy the policy.
     */
    public void setPolicy(CertificatePolicy policy) {
        this.policy = policy;
    }

    /**
     * Set the Secret ID.
     *
     * @param sid the Secret ID.
     */
    public void setSid(String sid) {
        this.sid = sid;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("cer", this.cer);
        jsonWriter.writeStringField("kid", this.kid);
        jsonWriter.writeJsonField("policy", this.policy);
        jsonWriter.writeStringField("sid", this.sid);

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of {@link CertificateBundle} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     *
     * @return An instance of {@link CertificateBundle} if the {@link JsonReader} was pointing to an instance of it, or
     * {@code null} if it was pointing to JSON {@code null}.
     *
     * @throws IOException If an error occurs while reading the {@link CertificateBundle}.
     */
    public static CertificateBundle fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CertificateBundle deserializedCertificateBundle = new CertificateBundle();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();

                reader.nextToken();

                if ("cer".equals(fieldName)) {
                    deserializedCertificateBundle.cer = reader.getString();
                } else if ("kid".equals(fieldName)) {
                    deserializedCertificateBundle.kid = reader.getString();
                } else if ("policy".equals(fieldName)) {
                    deserializedCertificateBundle.policy = CertificatePolicy.fromJson(reader);
                } else if ("sid".equals(fieldName)) {
                    deserializedCertificateBundle.sid = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedCertificateBundle;
        });
    }
}
