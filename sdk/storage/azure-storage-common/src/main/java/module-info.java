// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.common {
    requires transitive com.azure.core;

    exports com.azure.storage.common;
    exports com.azure.storage.common.credentials;
    exports com.azure.storage.common.policy;

    exports com.azure.storage.common.implementation.credentials to // FIXME this should not be a long-term solution
        com.azure.storage.blob,
        com.azure.storage.blob.cryptography,
        com.azure.storage.file,
        com.azure.storage.queue;

    exports com.azure.storage.common.implementation.policy to // FIXME this should not be a long-term solution
        com.azure.storage.blob,
        com.azure.storage.blob.cryptography,
        com.azure.storage.file,
        com.azure.storage.queue;
}
