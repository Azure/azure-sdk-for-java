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
 * The type of the action.
 */
public final class CertificatePolicyAction
    implements ExpandableEnum<String>, JsonSerializable<CertificatePolicyAction> {
    private static final Map<String, CertificatePolicyAction> VALUES = new ConcurrentHashMap<>();
    private static final Function<String, CertificatePolicyAction> NEW_INSTANCE = CertificatePolicyAction::new;

    /**
     * Static value EmailContacts for CertificatePolicyAction.
     */
    public static final CertificatePolicyAction EMAIL_CONTACTS = fromValue("EmailContacts");

    /**
     * Static value AutoRenew for CertificatePolicyAction.
     */
    public static final CertificatePolicyAction AUTO_RENEW = fromValue("AutoRenew");

    private final String value;

    private CertificatePolicyAction(String value) {
        this.value = value;
    }

    /**
     * Creates or finds a CertificatePolicyAction.
     *
     * @param value a value to look for.
     * @return the corresponding CertificatePolicyAction.
     * @throws IllegalArgumentException if value is null.
     */
    public static CertificatePolicyAction fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("'value' cannot be null.");
        }
        return VALUES.computeIfAbsent(value, NEW_INSTANCE);
    }

    /**
     * Gets the known CertificatePolicyAction values.
     *
     * @return Known CertificatePolicyAction values.
     */
    public static Collection<CertificatePolicyAction> values() {
        return new ArrayList<>(VALUES.values());
    }

    /**
     * Gets the value of the CertificatePolicyAction instance.
     *
     * @return the value of the CertificatePolicyAction instance.
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
     * Reads an instance of CertificatePolicyAction from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CertificatePolicyAction if the JsonReader was pointing to an instance of it, or null if the
     * JsonReader was pointing to JSON null.
     *
     * @throws IOException If an error occurs while reading the CertificatePolicyAction.
     * @throws IllegalStateException If unexpected JSON token is found.
     */
    public static CertificatePolicyAction fromJson(JsonReader jsonReader) throws IOException {
        JsonToken nextToken = jsonReader.nextToken();

        if (nextToken == JsonToken.NULL) {
            return null;
        }

        if (nextToken != JsonToken.STRING) {
            throw new IllegalStateException(
                String.format("Unexpected JSON token for %s deserialization: %s", JsonToken.STRING, nextToken));
        }

        return CertificatePolicyAction.fromValue(jsonReader.getString());
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
