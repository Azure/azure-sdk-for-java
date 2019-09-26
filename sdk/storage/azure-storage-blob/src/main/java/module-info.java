// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.blob {
    requires transitive com.azure.core;
    requires transitive com.azure.storage.common;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.databind;

    exports com.azure.storage.blob;
    exports com.azure.storage.blob.models;
    exports com.azure.storage.blob.specialized;

    opens com.azure.storage.blob.models to
        com.fasterxml.jackson.databind,
        com.azure.core;
    opens com.azure.storage.blob.implementation to
        com.fasterxml.jackson.databind,
        com.azure.core;
}
