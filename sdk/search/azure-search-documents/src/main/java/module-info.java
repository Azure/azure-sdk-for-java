// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.search.documents {
    requires transitive com.azure.core;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires jakarta.activation;

    opens com.azure.search.documents to com.fasterxml.jackson.databind;
    opens com.azure.search.documents.models to com.fasterxml.jackson.databind;
    opens com.azure.search.documents.implementation to com.fasterxml.jackson.databind;
    opens com.azure.search.documents.implementation.models to com.fasterxml.jackson.databind;

    exports com.azure.search.documents.implementation.util to com.fasterxml.jackson.databind;
    exports com.azure.search.documents;
    exports com.azure.search.documents.models;
    exports com.azure.search.documents.util;
}
