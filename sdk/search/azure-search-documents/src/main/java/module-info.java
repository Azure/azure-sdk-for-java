// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.search.documents {
    requires transitive com.azure.json;
    requires transitive com.azure.core;

    opens com.azure.search.documents.models to com.azure.core;
    opens com.azure.search.documents.implementation.models to com.azure.core;

    opens com.azure.search.documents.indexes.models to com.azure.core;
    opens com.azure.search.documents.indexes.implementation.models to com.azure.core;

    exports com.azure.search.documents;
    exports com.azure.search.documents.indexes;
    exports com.azure.search.documents.indexes.models;
    exports com.azure.search.documents.models;
    exports com.azure.search.documents.options;
    exports com.azure.search.documents.util;
}
