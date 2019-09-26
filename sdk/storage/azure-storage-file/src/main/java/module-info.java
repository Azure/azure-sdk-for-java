// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.file {
    requires transitive com.azure.core;
    requires com.azure.storage.common;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.databind;

    exports com.azure.storage.file;
    exports com.azure.storage.file.models;

    opens com.azure.storage.file.models to
        com.fasterxml.jackson.databind,
        com.azure.core;
    opens com.azure.storage.file.implementation to
        com.fasterxml.jackson.databind,
        com.azure.core;

    uses com.azure.core.http.HttpClientProvider;
}
