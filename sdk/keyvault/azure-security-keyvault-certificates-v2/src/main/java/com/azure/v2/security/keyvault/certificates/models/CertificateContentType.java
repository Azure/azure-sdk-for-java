// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.certificates.models;

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
 * Content type of the certificate when the managed secret is downloaded using a {@code SecretClient}.
 */
public final class CertificateContentType implements ExpandableEnum<String>, JsonSerializable<CertificateContentType> {
    private static final Map<String, CertificateContentType> VALUES = new ConcurrentHashMap<>();
    private static final Function<String, CertificateContentType> NEW_INSTANCE = CertificateContentType::new;

    /**
     * Static value {@code PKCS12} for {@link CertificateContentType}.
     */
    public static final CertificateContentType PKCS12 = fromValue("application/x-pkcs12");

    /**
     * Static value {@code PEM} for {@link CertificateContentType}.
     */
    public static final CertificateContentType PEM = fromValue("application/x-pem-file");

    private final String value;

    private CertificateContentType(String value) {
        this.value = value;
    }

    /**
     * Creates or finds a CertificateContentType.
     *
     * @param value a value to look for.
     * @return the corresponding CertificateContentType.
     * @throws IllegalArgumentException if value is null.
     */
    public static CertificateContentType fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("'value' cannot be null.");
        }
        return VALUES.computeIfAbsent(value, NEW_INSTANCE);
    }

    /**
     * Gets the known CertificateContentType values.
     *
     * @return Known CertificateContentType values.
     */
    public static Collection<CertificateContentType> values() {
        return new ArrayList<>(VALUES.values());
    }

    /**
     * Gets the value of the CertificateContentType instance.
     *
     * @return the value of the CertificateContentType instance.
     */
    @Override
    public String getValue() {
        return this.value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeString(getValue());
    }

    /**
     * Reads an instance of CertificateContentType from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CertificateContentType if the JsonReader was pointing to an instance of it, or null if the
     * JsonReader was pointing to JSON null.
     *
     * @throws IOException If an error occurs while reading the CertificateContentType.
     * @throws IllegalStateException If unexpected JSON token is found.
     */
    public static CertificateContentType fromJson(JsonReader jsonReader) throws IOException {
        JsonToken nextToken = jsonReader.nextToken();

        if (nextToken == JsonToken.NULL) {
            return null;
        }

        if (nextToken != JsonToken.STRING) {
            throw new IllegalStateException(
                String.format("Unexpected JSON token for %s deserialization: %s", JsonToken.STRING, nextToken));
        }

        return CertificateContentType.fromValue(jsonReader.getString());
    }

    @Override
    public String toString() {
        return Objects.toString(this.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.value);
    }
}
