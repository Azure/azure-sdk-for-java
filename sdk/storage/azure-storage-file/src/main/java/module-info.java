// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.file {
    requires transitive com.azure.core;
    requires transitive com.azure.storage.common;
    requires com.fasterxml.jackson.dataformat.xml;

    exports com.azure.storage.file;
    exports com.azure.storage.file.models;
    exports com.azure.storage.file.sas;

    opens com.azure.storage.file.models to
        com.fasterxml.jackson.databind,
        com.azure.core;
    opens com.azure.storage.file.implementation to
        com.fasterxml.jackson.databind,
        com.azure.core;
    opens com.azure.storage.file.implementation.models to
        com.fasterxml.jackson.databind,
        com.azure.core;
}
