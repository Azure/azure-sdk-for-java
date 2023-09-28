// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.client.core {
    requires com.client.json;
    requires transitive reactor.core;
    requires transitive org.reactivestreams;
    requires transitive org.slf4j;

    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;

    requires java.xml;
    requires transitive com.fasterxml.jackson.datatype.jsr310;

    // public API surface area
    exports com.client.core.annotation;
    exports com.client.core.client.traits;
    exports com.client.core.credential;
    exports com.client.core.cryptography;
    exports com.client.core.exception;
    exports com.client.core.http;
    exports com.client.core.http.policy;
    exports com.client.core.http.rest;
    exports com.client.core.models;
    exports com.client.core.util;
    exports com.client.core.util.builder;
    exports com.client.core.util.io;
    exports com.client.core.util.logging;
    exports com.client.core.util.paging;
    exports com.client.core.util.polling;
    exports com.client.core.util.serializer;
    exports com.client.core.util.tracing;
    exports com.client.core.util.metrics;

    exports com.client.core.implementation to com.client.core.serializer.json.jackson,
        com.client.core.serializer.json.gson,
        // export core implementation.ImplUtils to other core packages.
        com.client.core.experimental;

    // TODO temporary until we find final shape of ObjectMapper shimming APIs
    exports com.client.core.implementation.jackson to com.client.core.management, com.client.core.serializer.json.jackson;

    // export core utilities to other core packages.
    exports com.client.core.implementation.util to com.client.http.netty, com.client.core.http.okhttp,
        com.client.core.http.jdk.httpclient, com.client.core.serializer.json.jackson;
    exports com.client.core.util.polling.implementation to com.client.core.experimental;

    // exporting some packages specifically for Jackson
    opens com.client.core.credential to com.fasterxml.jackson.databind;
    opens com.client.core.http to com.fasterxml.jackson.databind;
    opens com.client.core.models to com.fasterxml.jackson.databind;
    opens com.client.core.util to com.fasterxml.jackson.databind;
    opens com.client.core.util.logging to com.fasterxml.jackson.databind;
    opens com.client.core.util.polling to com.fasterxml.jackson.databind;
    opens com.client.core.util.polling.implementation to com.fasterxml.jackson.databind;
    opens com.client.core.util.serializer to com.fasterxml.jackson.databind;
    opens com.client.core.implementation to com.fasterxml.jackson.databind;
    opens com.client.core.implementation.logging to com.fasterxml.jackson.databind;
    opens com.client.core.implementation.serializer to com.fasterxml.jackson.databind;
    opens com.client.core.implementation.jackson to com.fasterxml.jackson.databind;
    opens com.client.core.implementation.util to com.fasterxml.jackson.databind;
    opens com.client.core.implementation.http.rest to com.fasterxml.jackson.databind;
    opens com.client.core.http.rest to com.fasterxml.jackson.databind;

    // Service Provider Interfaces
    uses com.client.core.http.HttpClientProvider;
    uses com.client.core.http.policy.BeforeRetryPolicyProvider;
    uses com.client.core.http.policy.AfterRetryPolicyProvider;
    uses com.client.core.util.serializer.JsonSerializerProvider;
    uses com.client.core.util.serializer.MemberNameConverterProvider;
    uses com.client.core.util.tracing.Tracer;
    uses com.client.core.util.metrics.MeterProvider;
    uses com.client.core.util.tracing.TracerProvider;
}
