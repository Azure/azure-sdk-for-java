// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


module com.azure.encryption.cosmos {

    requires transitive com.azure.cosmos;
    requires cryptography;
    requires azure.key.vault.keystoreprovider;

    // public API surface area
    exports com.azure.encryption.cosmos;
    exports com.azure.encryption.cosmos.models;
    exports com.azure.encryption.cosmos.util;
    exports com.azure.encryption.cosmos.keyprovider;
}
