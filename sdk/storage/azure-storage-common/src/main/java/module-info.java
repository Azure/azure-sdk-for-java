// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.common {
    requires transitive com.azure.core;

    exports com.azure.storage.common;
    exports com.azure.storage.common.credentials;
    exports com.azure.storage.common.policy;

    exports com.azure.storage.common.implementation.credentials to
        com.azure.storage.blob,
        com.azure.storage.blob.cryptography;

    exports com.azure.storage.common.implementation.policy to
        com.azure.storage.blob,
        com.azure.storage.blob.cryptography;
}
