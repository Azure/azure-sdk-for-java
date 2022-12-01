// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.blob.batch {
    requires transitive com.azure.storage.blob;

    requires com.fasterxml.jackson.dataformat.xml;

    exports com.azure.storage.blob.batch;
    exports com.azure.storage.blob.batch.options;
}
