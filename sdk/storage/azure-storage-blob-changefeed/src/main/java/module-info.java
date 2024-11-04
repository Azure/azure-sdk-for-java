// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.blob.changefeed {
    requires transitive com.azure.storage.blob;
    requires com.azure.storage.internal.avro;
    requires com.azure.core;

    exports com.azure.storage.blob.changefeed;
    exports com.azure.storage.blob.changefeed.models;
}
