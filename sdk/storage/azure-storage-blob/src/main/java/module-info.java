// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.blob {
    requires transitive com.azure.storage.common;

    requires com.fasterxml.jackson.dataformat.xml;
    requires com.azure.storage.internal.avro;

    exports com.azure.storage.blob;
    exports com.azure.storage.blob.models;
    exports com.azure.storage.blob.options;
    exports com.azure.storage.blob.sas;
    exports com.azure.storage.blob.specialized;

    // Blob batch needs to interact with the generated layer but shouldn't replicate it.
    exports com.azure.storage.blob.implementation to
        com.azure.storage.blob.cryptography,
        com.azure.storage.blob.batch,
        com.azure.storage.file.datalake;    // FIXME this should not be a long-term solution

    exports com.azure.storage.blob.implementation.models to
        com.azure.storage.blob.batch,
        com.azure.storage.blob.cryptography;

    exports com.azure.storage.blob.implementation.util to
        com.azure.storage.blob.cryptography,
        com.azure.storage.file.datalake,
        com.azure.storage.blob.changefeed,
        com.fasterxml.jackson.databind,
        com.azure.storage.blob.batch,
        com.azure.storage.blob.nio;

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
