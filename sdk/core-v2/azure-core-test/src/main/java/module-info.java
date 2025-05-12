// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.test {
    requires transitive io.clientcore.core;
    requires transitive com.azure.v2.core;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires org.junit.jupiter.params;
    requires org.junit.platform.commons;
    requires java.management;
    requires java.net.http;
    requires ant;

    exports com.azure.v2.core.test;
    exports com.azure.v2.core.test.annotation;
    exports com.azure.v2.core.test.http;
    exports com.azure.v2.core.test.junitextensions;
    exports com.azure.v2.core.test.models;
    exports com.azure.v2.core.test.policy;
    exports com.azure.v2.core.test.utils;

    opens com.azure.v2.core.test.implementation to com.azure.v2.core, org.junit.platform.commons;
    opens com.azure.v2.core.test to com.azure.v2.core, org.junit.platform.commons;
    opens com.azure.v2.core.test.junitextensions to com.azure.v2.core, org.junit.platform.commons;
    opens com.azure.v2.core.test.annotation to com.azure.v2.core, org.junit.platform.commons;

    uses io.clientcore.core.http.client.HttpClientProvider;
}
