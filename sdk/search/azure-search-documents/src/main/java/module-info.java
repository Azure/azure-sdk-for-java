// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.search.documents {
    requires transitive com.azure.core;
    requires jakarta.activation;

    opens com.azure.search.documents to com.fasterxml.jackson.databind;
    opens com.azure.search.documents.indexes to com.fasterxml.jackson.databind;
    opens com.azure.search.documents.implementation to com.fasterxml.jackson.databind;
    opens com.azure.search.documents.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.search.documents.indexes.implementation to com.fasterxml.jackson.databind;
    opens com.azure.search.documents.indexes.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.search.documents.implementation.util to com.fasterxml.jackson.databind;

    exports com.azure.search.documents;
    exports com.azure.search.documents.indexes;
    exports com.azure.search.documents.indexes.models;
    exports com.azure.search.documents.models;
    exports com.azure.search.documents.options;
    exports com.azure.search.documents.util;
    opens com.azure.search.documents.indexes.models to com.azure.core, com.fasterxml.jackson.databind;
    opens com.azure.search.documents.models to com.azure.core, com.fasterxml.jackson.databind;
}
