// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.blob {
    requires transitive com.azure.core;
    requires transitive com.azure.storage.common;
    requires com.fasterxml.jackson.dataformat.xml;

    exports com.azure.storage.blob;
    exports com.azure.storage.blob.models;
    exports com.azure.storage.blob.specialized;

    // Blob batch needs to interact with the generated layer but shouldn't replicate it.
    exports com.azure.storage.blob.implementation to com.azure.storage.blob.batch;
    exports com.azure.storage.blob.implementation.models to com.azure.storage.blob.batch;

    opens com.azure.storage.blob.models to
        com.fasterxml.jackson.databind,
        com.azure.core;
    opens com.azure.storage.blob.implementation to
        com.fasterxml.jackson.databind,
        com.azure.core;
    opens com.azure.storage.blob.implementation.models to
        com.fasterxml.jackson.databind,
        com.azure.core;
}
