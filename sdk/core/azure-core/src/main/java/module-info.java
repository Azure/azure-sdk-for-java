// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core {
    requires com.azure.json;
    requires transitive reactor.core;
    requires transitive org.reactivestreams;
    requires transitive org.slf4j;

    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;

    requires java.xml;
    requires transitive com.fasterxml.jackson.datatype.jsr310;

    // public API surface area
    exports com.azure.core.annotation;
    exports com.azure.core.client.traits;
    exports com.azure.core.credential;
    exports com.azure.core.cryptography;
    exports com.azure.core.exception;
    exports com.azure.core.http;
    exports com.azure.core.http.policy;
    exports com.azure.core.http.rest;
    exports com.azure.core.models;
    exports com.azure.core.util;
    exports com.azure.core.util.builder;
    exports com.azure.core.util.io;
    exports com.azure.core.util.logging;
    exports com.azure.core.util.paging;
    exports com.azure.core.util.polling;
    exports com.azure.core.util.serializer;
    exports com.azure.core.util.tracing;
    exports com.azure.core.util.metrics;

    exports com.azure.core.implementation to com.azure.core.serializer.json.jackson,
        com.azure.core.serializer.json.gson,
        // export core implementation.ImplUtils to other core packages.
        com.azure.core.experimental;

    // TODO temporary until we find final shape of ObjectMapper shimming APIs
    exports com.azure.core.implementation.jackson to com.azure.core.management, com.azure.core.serializer.json.jackson;

    // export core utilities to other core packages.
    exports com.azure.core.implementation.util to com.azure.http.netty, com.azure.core.http.okhttp,
        com.azure.core.http.jdk.httpclient, com.azure.core.serializer.json.jackson;
    exports com.azure.core.util.polling.implementation to com.azure.core.experimental;

    // exporting some packages specifically for Jackson
    opens com.azure.core.credential to com.fasterxml.jackson.databind;
    opens com.azure.core.http to com.fasterxml.jackson.databind;
    opens com.azure.core.models to com.fasterxml.jackson.databind;
    opens com.azure.core.util to com.fasterxml.jackson.databind;
    opens com.azure.core.util.logging to com.fasterxml.jackson.databind;
    opens com.azure.core.util.polling to com.fasterxml.jackson.databind;
    opens com.azure.core.util.polling.implementation to com.fasterxml.jackson.databind;
    opens com.azure.core.util.serializer to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.logging to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.serializer to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.jackson to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.util to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.http.rest to com.fasterxml.jackson.databind;
    opens com.azure.core.http.rest to com.fasterxml.jackson.databind;

    // Service Provider Interfaces
    uses com.azure.core.http.HttpClientProvider;
    uses com.azure.core.http.policy.BeforeRetryPolicyProvider;
    uses com.azure.core.http.policy.AfterRetryPolicyProvider;
    uses com.azure.core.util.serializer.JsonSerializerProvider;
    uses com.azure.core.util.serializer.MemberNameConverterProvider;
    uses com.azure.core.util.tracing.Tracer;
    uses com.azure.core.util.metrics.MeterProvider;
    uses com.azure.core.util.tracing.TracerProvider;
}
