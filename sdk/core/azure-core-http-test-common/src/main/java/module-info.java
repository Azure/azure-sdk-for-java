// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
module com.azure.core.http.test.common {
    exports com.azure.core.http.test.common;
    exports com.azure.core.http.test.common.models;

    requires transitive com.azure.core;

    requires reactor.test;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires org.junit.jupiter.params;
    requires javax.servlet.api;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.util;
    requires org.eclipse.jetty.servlet;

    opens com.azure.core.http.test.common.models to com.azure.core;
}
