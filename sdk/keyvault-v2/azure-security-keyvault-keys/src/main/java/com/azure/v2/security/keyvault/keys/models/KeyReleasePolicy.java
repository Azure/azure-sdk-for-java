// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.models;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

/**
 * A model that represents the policy rules under which the key can be exported.
 */
@Metadata(properties = { MetadataProperties.FLUENT })
public final class KeyReleasePolicy implements JsonSerializable<KeyReleasePolicy> {
    /**
     * The policy rules under which the key can be released. Encoded based on the {@link KeyReleasePolicy#contentType}.
     * <p>
     * For more information regarding the release policy grammar for Azure Key Vault, please refer to:
     * - https://aka.ms/policygrammarkeys for Azure Key Vault release policy grammar.
     * - https://aka.ms/policygrammarmhsm for Azure Managed HSM release policy grammar.
     */
    private final BinaryData encodedPolicy;

    /*
     * Content type and version of key release policy.
     */
    private String contentType;

    /*
     * Defines the mutability state of the policy. Once marked immutable on the service side, this flag cannot be reset
     * and the policy cannot be changed under any circumstances.
     */
    private Boolean immutable;

    KeyReleasePolicy(BinaryData encodedPolicy, boolean ignored) {
        this.encodedPolicy = null;
    }

    /**
     * Creates an instance of {@link KeyReleasePolicy}.
     *
     * @param encodedPolicy The policy rules under which the key can be released. Encoded based on the
     * {@link KeyReleasePolicy#contentType}.
     * <p>
     * For more information regarding the release policy grammar for Azure Key Vault, please refer to:
     * - https://aka.ms/policygrammarkeys for Azure Key Vault release policy grammar.
     * - https://aka.ms/policygrammarmhsm for Azure Managed HSM release policy grammar.
     */
    public KeyReleasePolicy(BinaryData encodedPolicy) {
        Objects.requireNonNull(encodedPolicy, "'encodedPolicy' cannot be null.");

        this.encodedPolicy = encodedPolicy;
    }

    /**
     * Get a blob encoding the policy rules under which the key can be released.
     *
     * @return encodedPolicy The policy rules under which the key can be released. Encoded based on the
     * {@link KeyReleasePolicy#contentType}.
     * <p>
     * For more information regarding the release policy grammar for Azure Key Vault, please refer to:
     * - https://aka.ms/policygrammarkeys for Azure Key Vault release policy grammar.
     * - https://aka.ms/policygrammarmhsm for Azure Managed HSM release policy grammar.
     */
    public BinaryData getEncodedPolicy() {
        return encodedPolicy;
    }

    /**
     * Get the content type and version of key release policy.
     *
     * @return The content type and version of key release policy.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Set the content type and version of key release policy.
     *
     * <p>The service default is "application/json; charset=utf-8".</p>
     *
     * @param contentType The content type and version of key release policy to set.
     *
     * @return The updated {@link KeyReleasePolicy} object.
     */
    public KeyReleasePolicy setContentType(String contentType) {
        this.contentType = contentType;

        return this;
    }

    /**
     * Get a value indicating if the policy is immutable. Once marked immutable on the service side, this flag cannot
     * be reset and the policy cannot be changed under any circumstances.
     *
     * @return If the {@link KeyReleasePolicy} is immutable.
     */
    public Boolean isImmutable() {
        return this.immutable;
    }

    /**
     * Get a value indicating if the policy is immutable. Defines the mutability state of the policy. Once marked
     * immutable on the service side, this flag cannot be reset and the policy cannot be changed under any
     * circumstances.
     *
     * @param immutable The immutable value to set.
     * @return The updated {@link KeyReleasePolicy} object.
     */
    public KeyReleasePolicy setImmutable(Boolean immutable) {
        this.immutable = immutable;

        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();

        String encodedPolicyText;
        if (encodedPolicy == null) {
            encodedPolicyText = null;
        } else {
            byte[] bytes = encodedPolicy.toBytes();

            if (bytes == null) {
                encodedPolicyText = null;
            } else if (bytes.length == 0) {
                encodedPolicyText = "";
            } else {
                encodedPolicyText = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            }
        }

        jsonWriter.writeStringField("data", encodedPolicyText);
        jsonWriter.writeStringField("contentType", contentType);
        jsonWriter.writeBooleanField("immutable", immutable);

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link KeyReleasePolicy}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return An instance of {@link KeyReleasePolicy} that the JSON stream represented, may return null.
     * @throws IOException If a {@link KeyReleasePolicy} fails to be read from the {@code jsonReader}.
     */
    public static KeyReleasePolicy fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            BinaryData encodedPolicy = null;
            String contentType = null;
            Boolean immutable = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("data".equals(fieldName)) {
                    encodedPolicy
                        = reader.getNullable(nonNullReader -> BinaryData.fromString(nonNullReader.getString()));
                } else if ("contentType".equals(fieldName)) {
                    contentType = reader.getString();
                } else if ("immutable".equals(fieldName)) {
                    immutable = reader.getNullable(JsonReader::getBoolean);
                } else {
                    reader.skipChildren();
                }
            }

            return new KeyReleasePolicy(encodedPolicy, false).setContentType(contentType).setImmutable(immutable);
        });
    }
}
