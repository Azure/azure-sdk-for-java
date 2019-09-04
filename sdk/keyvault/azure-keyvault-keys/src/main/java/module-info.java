// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.security.keyvault.keys {
    requires transitive com.azure.core;

    requires commons.codec;

    exports com.azure.security.keyvault.keys.cryptography;
    exports com.azure.security.keyvault.keys.cryptography.models;
    exports com.azure.security.keyvault.keys.models;
    exports com.azure.security.keyvault.keys.models.webkey;
}
