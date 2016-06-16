/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for KeyVaultSecretStatus.
 */
public enum KeyVaultSecretStatus {
    /** Enum value Initialized. */
    INITIALIZED("Initialized"),

    /** Enum value WaitingOnCertificateOrder. */
    WAITING_ON_CERTIFICATE_ORDER("WaitingOnCertificateOrder"),

    /** Enum value Succeeded. */
    SUCCEEDED("Succeeded"),

    /** Enum value CertificateOrderFailed. */
    CERTIFICATE_ORDER_FAILED("CertificateOrderFailed"),

    /** Enum value OperationNotPermittedOnKeyVault. */
    OPERATION_NOT_PERMITTED_ON_KEY_VAULT("OperationNotPermittedOnKeyVault"),

    /** Enum value AzureServiceUnauthorizedToAccessKeyVault. */
    AZURE_SERVICE_UNAUTHORIZED_TO_ACCESS_KEY_VAULT("AzureServiceUnauthorizedToAccessKeyVault"),

    /** Enum value KeyVaultDoesNotExist. */
    KEY_VAULT_DOES_NOT_EXIST("KeyVaultDoesNotExist"),

    /** Enum value KeyVaultSecretDoesNotExist. */
    KEY_VAULT_SECRET_DOES_NOT_EXIST("KeyVaultSecretDoesNotExist"),

    /** Enum value UnknownError. */
    UNKNOWN_ERROR("UnknownError"),

    /** Enum value Unknown. */
    UNKNOWN("Unknown");

    /** The actual serialized value for a KeyVaultSecretStatus instance. */
    private String value;

    KeyVaultSecretStatus(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a KeyVaultSecretStatus instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a KeyVaultSecretStatus instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed KeyVaultSecretStatus object, or null if unable to parse.
     */
    @JsonCreator
    public static KeyVaultSecretStatus fromValue(String value) {
        KeyVaultSecretStatus[] items = KeyVaultSecretStatus.values();
        for (KeyVaultSecretStatus item : items) {
            if (item.toValue().equalsIgnoreCase(value)) {
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
