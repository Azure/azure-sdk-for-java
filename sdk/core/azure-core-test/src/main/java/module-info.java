// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.test {
    requires transitive com.azure.core;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires org.junit.platform.commons;
    requires reactor.test;
    requires java.management;
    requires java.net.http;
    requires javax.servlet.api;
    requires reactor.netty.http;
    requires reactor.netty.core;
    requires io.netty.codec.http;
    requires org.apache.commons.compress;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.servlet;
    requires org.eclipse.jetty.util;
    requires org.eclipse.jetty.security;

    exports com.azure.core.test;
    exports com.azure.core.test.annotation;
    exports com.azure.core.test.http;
    exports com.azure.core.test.junitextensions;
    exports com.azure.core.test.models;
    exports com.azure.core.test.policy;
    exports com.azure.core.test.utils;

    opens com.azure.core.test.models to com.fasterxml.jackson.databind;
    opens com.azure.core.test.implementation
        to com.fasterxml.jackson.databind, com.azure.core, org.junit.platform.commons;
    opens com.azure.core.test.implementation.entities to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.core.test to com.azure.core, com.fasterxml.jackson.databind, org.junit.platform.commons;
    opens com.azure.core.test.junitextensions
        to com.azure.core, com.fasterxml.jackson.databind, org.junit.platform.commons;
    opens com.azure.core.test.annotation to com.azure.core, com.fasterxml.jackson.databind, org.junit.platform.commons;

    uses com.azure.core.http.HttpClientProvider;
}
