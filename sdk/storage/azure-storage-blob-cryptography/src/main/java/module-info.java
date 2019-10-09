// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.blob.cryptography {
    requires transitive com.azure.core;
    requires transitive com.azure.storage.common;
    requires com.azure.security.keyvault.keys;
    requires com.azure.storage.blob;
    requires com.fasterxml.jackson.dataformat.xml;

    exports com.azure.storage.blob.specialized.cryptography;
}
