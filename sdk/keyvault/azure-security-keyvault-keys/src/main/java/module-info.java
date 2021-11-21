// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.security.keyvault.keys {
    requires transitive com.azure.core;

    requires java.xml.crypto;

    exports com.azure.security.keyvault.keys;
    exports com.azure.security.keyvault.keys.cryptography;
    exports com.azure.security.keyvault.keys.cryptography.models;
    exports com.azure.security.keyvault.keys.models;

    opens com.azure.security.keyvault.keys to com.fasterxml.jackson.databind;
    opens com.azure.security.keyvault.keys.cryptography to com.fasterxml.jackson.databind;
    opens com.azure.security.keyvault.keys.cryptography.models to com.fasterxml.jackson.databind;
    opens com.azure.security.keyvault.keys.implementation to com.fasterxml.jackson.databind;
    opens com.azure.security.keyvault.keys.implementation.models to com.fasterxml.jackson.databind;
    opens com.azure.security.keyvault.keys.models to com.fasterxml.jackson.databind;
    exports com.azure.security.keyvault.keys.implementation;
}
