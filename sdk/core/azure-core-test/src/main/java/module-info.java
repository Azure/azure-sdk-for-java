// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.test {
    requires transitive com.azure.core;

    requires org.junit.jupiter.api;
    requires reactor.test;

    exports com.azure.core.test;
    exports com.azure.core.test.http;
    exports com.azure.core.test.models;
    exports com.azure.core.test.policy;
    exports com.azure.core.test.utils;
    exports com.azure.core.test.implementation;

    opens com.azure.core.test.implementation to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.core.test.implementation.entities to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.core.test to com.fasterxml.jackson.databind, org.junit.platform.commons;

    uses com.azure.core.http.HttpClientProvider;
}
