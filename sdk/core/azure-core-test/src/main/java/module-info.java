// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.test {
    requires transitive com.azure.core;
    requires transitive com.azure.core.http.jdk.httpclient;
    requires transitive com.azure.http.netty;
    requires transitive com.azure.core.http.okhttp;
    requires transitive com.azure.core.http.vertx;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires org.junit.jupiter.params;
    requires org.junit.platform.commons;
    requires java.management;
    requires java.net.http;
    requires reactor.netty.http;
    requires reactor.netty.core;
    requires io.netty.codec.http;
    requires ant;

    exports com.azure.core.test;
    exports com.azure.core.test.annotation;
    exports com.azure.core.test.http;
    exports com.azure.core.test.junitextensions;
    exports com.azure.core.test.models;
    exports com.azure.core.test.policy;
    exports com.azure.core.test.utils;

    opens com.azure.core.test.implementation to com.azure.core, org.junit.platform.commons;
    opens com.azure.core.test to com.azure.core, org.junit.platform.commons;
    opens com.azure.core.test.junitextensions to com.azure.core, org.junit.platform.commons;
    opens com.azure.core.test.annotation to com.azure.core, org.junit.platform.commons;

    uses com.azure.core.http.HttpClientProvider;
}
