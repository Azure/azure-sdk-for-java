// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core {
    requires transitive reactor.core;
    requires transitive org.reactivestreams;
    requires transitive org.slf4j;

    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;

    requires transitive com.fasterxml.jackson.dataformat.xml;
    requires transitive com.fasterxml.jackson.datatype.jsr310;

    // public API surface area
    exports com.azure.core.annotation;
    exports com.azure.core.credential;
    exports com.azure.core.cryptography;
    exports com.azure.core.exception;
    exports com.azure.core.http;
    exports com.azure.core.http.policy;
    exports com.azure.core.http.rest;
    exports com.azure.core.util;
    exports com.azure.core.util.logging;
    exports com.azure.core.util.paging;
    exports com.azure.core.util.polling;
    exports com.azure.core.util.serializer;
    exports com.azure.core.util.tracing;

    // exporting some packages specifically for Jackson
    opens com.azure.core.http to com.fasterxml.jackson.databind;
    opens com.azure.core.util to com.fasterxml.jackson.databind;
    opens com.azure.core.util.logging to com.fasterxml.jackson.databind;
    opens com.azure.core.util.serializer to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.logging to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.serializer to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.serializer.jsonwrapper to com.fasterxml.jackson.databind;

    // Exports HttpProviders#getAllHttpClients API to azure-core-test module
    exports com.azure.core.implementation.http to com.azure.core.test;

    // Exports JsonSerializer to azure-core-serializer-json-gson and azure-core-serializer-json-jackson
    exports com.azure.core.implementation.serializer to com.azure.core.serializer.json.gson,
        com.azure.core.serializer.json.jackson;

    // Service Provider Interfaces
    uses com.azure.core.util.tracing.Tracer;
    uses com.azure.core.http.HttpClientProvider;
    uses com.azure.core.http.policy.BeforeRetryPolicyProvider;
    uses com.azure.core.http.policy.AfterRetryPolicyProvider;
    uses com.azure.core.implementation.serializer.jsonwrapper.spi.JsonPlugin;

    // indicate JacksonPlugin provides a service implementation for JsonPlugin
    provides com.azure.core.implementation.serializer.jsonwrapper.spi.JsonPlugin
        with com.azure.core.implementation.serializer.jsonwrapper.jacksonwrapper.JacksonPlugin;
}
