// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.file.share {
    requires transitive com.azure.storage.common;

    requires com.fasterxml.jackson.dataformat.xml;

    exports com.azure.storage.file.share;
    exports com.azure.storage.file.share.models;
    exports com.azure.storage.file.share.options;
    exports com.azure.storage.file.share.sas;
    exports com.azure.storage.file.share.specialized;

    opens com.azure.storage.file.share.models to
        com.fasterxml.jackson.databind,
        com.azure.core;
    opens com.azure.storage.file.share.implementation to
        com.fasterxml.jackson.databind,
        com.azure.core;
    opens com.azure.storage.file.share.implementation.models to
        com.fasterxml.jackson.databind,
        com.azure.core;
    opens com.azure.storage.file.share.implementation.util to
        com.fasterxml.jackson.databind,
        com.azure.core;
}
