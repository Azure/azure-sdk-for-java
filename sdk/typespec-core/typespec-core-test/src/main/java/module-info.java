// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.typespec.core.test {
    requires transitive com.typespec.core;

    requires com.fasterxml.jackson.dataformat.xml;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
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

    exports com.typespec.core.test;
    exports com.typespec.core.test.annotation;
    exports com.typespec.core.test.http;
    exports com.typespec.core.test.models;
    exports com.typespec.core.test.policy;
    exports com.typespec.core.test.utils;

    opens com.typespec.core.test to com.fasterxml.jackson.databind, org.junit.platform.commons;
    opens com.typespec.core.test.models to com.fasterxml.jackson.databind;
    opens com.typespec.core.test.annotation to org.junit.platform.commons;
    opens com.typespec.core.test.implementation to com.fasterxml.jackson.databind, com.typespec.core,
        org.junit.platform.commons;
    opens com.typespec.core.test.implementation.entities to com.fasterxml.jackson.databind, com.typespec.core;

    uses com.typespec.core.http.HttpClientProvider;
}
