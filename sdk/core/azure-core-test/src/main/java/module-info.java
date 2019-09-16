// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.test {
    requires transitive com.azure.core;
    requires com.fasterxml.jackson.annotation;
    requires junit;
    requires org.slf4j;
    requires reactor.core;

    exports com.azure.core.test;
    exports com.azure.core.test.http;
    exports com.azure.core.test.models;
    exports com.azure.core.test.policy;
    exports com.azure.core.test.utils;
    exports com.azure.core.test.implementation;

    opens com.azure.core.test.implementation to
        com.fasterxml.jackson.databind,
        com.azure.core;
}
