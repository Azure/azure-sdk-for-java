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
    WAITINGONCERTIFICATEORDER("WaitingOnCertificateOrder"),

    /** Enum value Succeeded. */
    SUCCEEDED("Succeeded"),

    /** Enum value CertificateOrderFailed. */
    CERTIFICATEORDERFAILED("CertificateOrderFailed"),

    /** Enum value OperationNotPermittedOnKeyVault. */
    OPERATIONNOTPERMITTEDONKEYVAULT("OperationNotPermittedOnKeyVault"),

    /** Enum value AzureServiceUnauthorizedToAccessKeyVault. */
    AZURESERVICEUNAUTHORIZEDTOACCESSKEYVAULT("AzureServiceUnauthorizedToAccessKeyVault"),

    /** Enum value KeyVaultDoesNotExist. */
    KEYVAULTDOESNOTEXIST("KeyVaultDoesNotExist"),

    /** Enum value KeyVaultSecretDoesNotExist. */
    KEYVAULTSECRETDOESNOTEXIST("KeyVaultSecretDoesNotExist"),

    /** Enum value UnknownError. */
    UNKNOWNERROR("UnknownError"),

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
