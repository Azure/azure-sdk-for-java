// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.models;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;
import io.clientcore.core.utils.ExpandableEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * The key wrapping/unwrapping algorithm identifier.
 */
public final class KeyWrapAlgorithm implements ExpandableEnum<String>, JsonSerializable<KeyWrapAlgorithm> {
    private static final Map<String, KeyWrapAlgorithm> VALUES = new ConcurrentHashMap<>();

    private static final Function<String, KeyWrapAlgorithm> NEW_INSTANCE = KeyWrapAlgorithm::new;

    /**
     * [Not recommended] RSAES using Optimal Asymmetric Encryption Padding (OAEP), as described in
     * https://tools.ietf.org/html/rfc3447, with the default parameters specified by RFC 3447 in Section A.2.1. Those
     * default parameters are using a hash function of SHA-1 and a mask generation function of MGF1 with SHA-1.
     * Microsoft recommends using RSA_OAEP_256 or stronger algorithms for enhanced security. Microsoft does *not*
     * recommend RSA_OAEP, which is included solely for backwards compatibility. RSA_OAEP utilizes SHA1, which has known
     * collision problems.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static final KeyWrapAlgorithm RSA_OAEP = fromValue("RSA-OAEP");

    /**
     * RSAES using Optimal Asymmetric Encryption Padding with a hash function of SHA-256 and a mask generation function
     * of MGF1 with SHA-256.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static final KeyWrapAlgorithm RSA_OAEP_256 = fromValue("RSA-OAEP-256");

    /**
     * [Not recommended] RSAES-PKCS1-V1_5 key encryption, as described in https://tools.ietf.org/html/rfc3447. Microsoft
     * recommends using RSA_OAEP_256 or stronger algorithms for enhanced security. Microsoft does *not* recommend
     * RSA_1_5, which is included solely for backwards compatibility. Cryptographic standards no longer consider RSA
     * with the PKCS#1 v1.5 padding scheme secure for encryption.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static final KeyWrapAlgorithm RSA1_5 = fromValue("RSA1_5");

    /**
     * 128-bit AES key wrap.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static final KeyWrapAlgorithm A128KW = fromValue("A128KW");

    /**
     * 192-bit AES key wrap.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static final KeyWrapAlgorithm A192KW = fromValue("A192KW");

    /**
     * 256-bit AES key wrap.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static final KeyWrapAlgorithm A256KW = fromValue("A256KW");

    private final String value;

    private KeyWrapAlgorithm(String value) {
        this.value = value;
    }

    /**
     * Creates or finds a KeyWrapAlgorithm.
     *
     * @param value a value to look for.
     * @return the corresponding KeyWrapAlgorithm.
     * @throws IllegalArgumentException if value is null.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static KeyWrapAlgorithm fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("'value' cannot be null.");
        }
        return VALUES.computeIfAbsent(value, NEW_INSTANCE);
    }

    /**
     * Gets known KeyWrapAlgorithm values.
     *
     * @return Known KeyWrapAlgorithm values.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static Collection<KeyWrapAlgorithm> values() {
        return new ArrayList<>(VALUES.values());
    }

    /**
     * Gets the value of the KeyWrapAlgorithm instance.
     *
     * @return the value of the KeyWrapAlgorithm instance.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    @Override
    public String getValue() {
        return this.value;
    }

    /**
     * {@inheritDoc}
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeString(getValue());
    }

    /**
     * Reads an instance of KeyWrapAlgorithm from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of KeyWrapAlgorithm if the JsonReader was pointing to an instance of it, or null
     * if the JsonReader was pointing to JSON null.
     * @throws IOException If an error occurs while reading the KeyWrapAlgorithm.
     * @throws IllegalStateException If unexpected JSON token is found.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static KeyWrapAlgorithm fromJson(JsonReader jsonReader) throws IOException {
        JsonToken nextToken = jsonReader.nextToken();
        if (nextToken == JsonToken.NULL) {
            return null;
        }
        if (nextToken != JsonToken.STRING) {
            throw new IllegalStateException(
                String.format("Unexpected JSON token for %s deserialization: %s", JsonToken.STRING, nextToken));
        }
        return KeyWrapAlgorithm.fromValue(jsonReader.getString());
    }

    @Metadata(properties = { MetadataProperties.GENERATED })
    @Override
    public String toString() {
        return Objects.toString(this.value);
    }

    @Metadata(properties = { MetadataProperties.GENERATED })
    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Metadata(properties = { MetadataProperties.GENERATED })
    @Override
    public int hashCode() {
        return Objects.hashCode(this.value);
    }
}
