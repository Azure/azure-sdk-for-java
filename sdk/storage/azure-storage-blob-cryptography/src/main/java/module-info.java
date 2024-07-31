// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.blob.cryptography {
    requires transitive com.azure.storage.blob;

    exports com.azure.storage.blob.specialized.cryptography;

    opens com.azure.storage.blob.specialized.cryptography to com.azure.core;
}
