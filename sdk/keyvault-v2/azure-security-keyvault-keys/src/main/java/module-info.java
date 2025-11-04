// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.v2.security.keyvault.keys {
    requires transitive com.azure.v2.core;

    exports com.azure.v2.security.keyvault.keys;
    exports com.azure.v2.security.keyvault.keys.models;
    exports com.azure.v2.security.keyvault.keys.cryptography;
    exports com.azure.v2.security.keyvault.keys.cryptography.models;
}
