// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.test {
    requires transitive com.azure.core;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires reactor.test;

    exports com.azure.core.test;
    exports com.azure.core.test.annotation;
    exports com.azure.core.test.http;
    exports com.azure.core.test.models;
    exports com.azure.core.test.policy;
    exports com.azure.core.test.utils;

    exports com.azure.core.test.implementation to com.azure.http.netty, com.azure.core.http.jdk.httpclient,
        com.azure.core.http.okhttp, org.junit.platform.commons;

    opens com.azure.core.test to com.fasterxml.jackson.databind, org.junit.platform.commons;
    opens com.azure.core.test.annotation to org.junit.platform.commons;
    opens com.azure.core.test.implementation to com.fasterxml.jackson.databind, com.azure.core,
        org.junit.platform.commons;
    opens com.azure.core.test.implementation.entities to com.fasterxml.jackson.databind, com.azure.core;

    uses com.azure.core.http.HttpClientProvider;
}
