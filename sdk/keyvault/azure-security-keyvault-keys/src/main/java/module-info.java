// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.security.keyvault.keys {
    requires transitive com.azure.core;

    exports com.azure.security.keyvault.keys;
    exports com.azure.security.keyvault.keys.models;
    exports com.azure.security.keyvault.keys.cryptography;
    exports com.azure.security.keyvault.keys.cryptography.models;

    opens com.azure.security.keyvault.keys to com.azure.core;
    opens com.azure.security.keyvault.keys.models to com.azure.core;
    opens com.azure.security.keyvault.keys.implementation.models to com.azure.core;
    opens com.azure.security.keyvault.keys.cryptography.models to com.azure.core;
}
