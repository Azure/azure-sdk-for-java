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
 * The signing/verification algorithm identifier.
 */
public final class SignatureAlgorithm implements ExpandableEnum<String>, JsonSerializable<SignatureAlgorithm> {
    private static final Map<String, SignatureAlgorithm> VALUES = new ConcurrentHashMap<>();

    private static final Function<String, SignatureAlgorithm> NEW_INSTANCE = SignatureAlgorithm::new;

    /**
     * RSASSA-PSS using SHA-256 and MGF1 with SHA-256, as described in https://tools.ietf.org/html/rfc7518.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static final SignatureAlgorithm PS256 = fromValue("PS256");

    /**
     * RSASSA-PSS using SHA-384 and MGF1 with SHA-384, as described in https://tools.ietf.org/html/rfc7518.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static final SignatureAlgorithm PS384 = fromValue("PS384");

    /**
     * RSASSA-PSS using SHA-512 and MGF1 with SHA-512, as described in https://tools.ietf.org/html/rfc7518.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static final SignatureAlgorithm PS512 = fromValue("PS512");

    /**
     * RSASSA-PKCS1-v1_5 using SHA-256, as described in https://tools.ietf.org/html/rfc7518.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static final SignatureAlgorithm RS256 = fromValue("RS256");

    /**
     * RSASSA-PKCS1-v1_5 using SHA-384, as described in https://tools.ietf.org/html/rfc7518.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static final SignatureAlgorithm RS384 = fromValue("RS384");

    /**
     * RSASSA-PKCS1-v1_5 using SHA-512, as described in https://tools.ietf.org/html/rfc7518.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static final SignatureAlgorithm RS512 = fromValue("RS512");

    /**
     * Reserved.
     */
    /*@Metadata(properties = { MetadataProperties.GENERATED })
    public static final SignatureAlgorithm RSNULL = fromValue("RSNULL");*/

    /**
     * ECDSA using P-256 and SHA-256, as described in https://tools.ietf.org/html/rfc7518.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static final SignatureAlgorithm ES256 = fromValue("ES256");

    /**
     * ECDSA using P-384 and SHA-384, as described in https://tools.ietf.org/html/rfc7518.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static final SignatureAlgorithm ES384 = fromValue("ES384");

    /**
     * ECDSA using P-521 and SHA-512, as described in https://tools.ietf.org/html/rfc7518.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static final SignatureAlgorithm ES512 = fromValue("ES512");

    /**
     * ECDSA using P-256K and SHA-256, as described in https://tools.ietf.org/html/rfc7518.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static final SignatureAlgorithm ES256K = fromValue("ES256K");

    private final String value;

    private SignatureAlgorithm(String value) {
        this.value = value;
    }

    /**
     * Creates or finds a SignatureAlgorithm.
     *
     * @param value a value to look for.
     * @return the corresponding SignatureAlgorithm.
     * @throws IllegalArgumentException if value is null.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static SignatureAlgorithm fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("'value' cannot be null.");
        }
        return VALUES.computeIfAbsent(value, NEW_INSTANCE);
    }

    /**
     * Gets known SignatureAlgorithm values.
     *
     * @return Known SignatureAlgorithm values.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static Collection<SignatureAlgorithm> values() {
        return new ArrayList<>(VALUES.values());
    }

    /**
     * Gets the value of the SignatureAlgorithm instance.
     *
     * @return the value of the SignatureAlgorithm instance.
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
     * Reads an instance of SignatureAlgorithm from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of SignatureAlgorithm if the JsonReader was pointing to an instance of it, or null
     * if the JsonReader was pointing to JSON null.
     * @throws IOException If an error occurs while reading the SignatureAlgorithm.
     * @throws IllegalStateException If unexpected JSON token is found.
     */
    @Metadata(properties = { MetadataProperties.GENERATED })
    public static SignatureAlgorithm fromJson(JsonReader jsonReader) throws IOException {
        JsonToken nextToken = jsonReader.nextToken();
        if (nextToken == JsonToken.NULL) {
            return null;
        }
        if (nextToken != JsonToken.STRING) {
            throw new IllegalStateException(
                String.format("Unexpected JSON token for %s deserialization: %s", JsonToken.STRING, nextToken));
        }
        return SignatureAlgorithm.fromValue(jsonReader.getString());
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
