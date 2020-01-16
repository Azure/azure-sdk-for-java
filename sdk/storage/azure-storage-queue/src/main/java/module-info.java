// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.queue {
    requires transitive com.azure.core;
    requires transitive com.azure.storage.common;
    requires com.fasterxml.jackson.dataformat.xml;

    exports com.azure.storage.queue;
    exports com.azure.storage.queue.models;
    exports com.azure.storage.queue.sas;

    opens com.azure.storage.queue.models to
        com.fasterxml.jackson.databind,
        com.azure.core;
    opens com.azure.storage.queue.implementation to
        com.fasterxml.jackson.databind,
        com.azure.core;
    opens com.azure.storage.queue.implementation.models to
        com.fasterxml.jackson.databind,
        com.azure.core;
}
