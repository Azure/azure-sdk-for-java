/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for CertificateState.
 */
public enum CertificateState {
    /** Enum value active. */
    ACTIVE("active"),

    /** Enum value deleting. */
    DELETING("deleting"),

    /** Enum value deletefailed. */
    DELETEFAILED("deletefailed");

    /** The actual serialized value for a CertificateState instance. */
    private String value;

    CertificateState(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a CertificateState instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a CertificateState instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed CertificateState object, or null if unable to parse.
     */
    @JsonCreator
    public static CertificateState fromValue(String value) {
        CertificateState[] items = CertificateState.values();
        for (CertificateState item : items) {
            if (item.toValue().equals(value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return toValue();
    }
}
