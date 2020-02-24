// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.quickquery {
    requires transitive com.azure.core;
    requires transitive com.azure.storage.common;
    requires com.azure.storage.blob;
    requires com.fasterxml.jackson.dataformat.xml;

    exports com.azure.storage.quickquery;

    opens com.azure.storage.quickquery to
        com.fasterxml.jackson.databind,
        com.azure.core;
}
