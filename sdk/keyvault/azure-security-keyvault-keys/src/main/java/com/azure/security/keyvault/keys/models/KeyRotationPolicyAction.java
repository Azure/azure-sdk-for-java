// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines the types of key rotation policy actions that can be executed.
 */
public enum KeyRotationPolicyAction {
    ROTATE("rotate"),
    NOTIFY("notify");

    /**
     * The serialized value for a {@link KeyRotationPolicyAction} instance.
     */
    private final String value;

    KeyRotationPolicyAction(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a {@link KeyRotationPolicyAction} instance.
     *
     * @param value The serialized value to parse.
     * @return The parsed {@link KeyRotationPolicyAction} object, or {@code null} if unable to parse.
     */
    @JsonCreator
    public static KeyRotationPolicyAction fromString(String value) {
        KeyRotationPolicyAction[] items = KeyRotationPolicyAction.values();

        for (KeyRotationPolicyAction item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }

        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
