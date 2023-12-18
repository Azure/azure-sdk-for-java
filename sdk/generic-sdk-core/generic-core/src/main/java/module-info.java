// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.generic.core {
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires javax.servlet.api;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.servlet;
    requires org.eclipse.jetty.util;
    requires org.eclipse.jetty.security;

    requires transitive org.slf4j;
    requires transitive com.generic.json;

    // public API surface area
    exports com.generic.core.annotation;
    exports com.generic.core.credential;
    exports com.generic.core.models;
    exports com.generic.core.http;
    exports com.generic.core.http.annotation;
    exports com.generic.core.http.exception;
    exports com.generic.core.http.client;
    exports com.generic.core.http.models;
    exports com.generic.core.util;
    exports com.generic.core.http.pipeline;
    exports com.generic.core.http.policy;
    exports com.generic.core.util.configuration;
    exports com.generic.core.util.serializer;

    exports com.generic.core.implementation.test to com.generic.core.http.okhttp;
    exports com.generic.core.implementation.util to com.generic.core.http.okhttp;

    opens com.generic.core.implementation.test to org.junit.platform.commons;

    uses com.generic.core.http.client.HttpClientProvider;
}
