// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.internal.avro {
    requires transitive com.azure.storage.common;
    requires com.azure.core;

    exports com.azure.storage.internal.avro.implementation
        to com.azure.storage.blob,
        com.azure.storage.blob.changefeed;

    exports com.azure.storage.internal.avro.implementation.schema
        to com.azure.storage.blob,
        com.azure.storage.blob.changefeed;

    exports com.azure.storage.internal.avro.implementation.schema.primitive
        to com.azure.storage.blob,
        com.azure.storage.blob.changefeed;
}
