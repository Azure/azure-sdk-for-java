// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.search {
    requires transitive com.azure.core;
    requires org.apache.commons.lang3;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens com.azure.search to com.fasterxml.jackson.databind;
    opens com.azure.search.models to com.fasterxml.jackson.databind;
    opens com.azure.search.implementation to com.fasterxml.jackson.databind;

    exports com.azure.search;
    exports com.azure.search.common;
    exports com.azure.search.models;
}
