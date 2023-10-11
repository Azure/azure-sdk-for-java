// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.typespec.core {
    requires com.typespec.json;
    requires transitive reactor.core;
    requires transitive org.reactivestreams;
    requires transitive org.slf4j;

    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;

    requires java.xml;
    requires transitive com.fasterxml.jackson.datatype.jsr310;

    // public API surface area
    exports com.typespec.core.annotation;
    exports com.typespec.core.client.traits;
    exports com.typespec.core.credential;
    exports com.typespec.core.cryptography;
    exports com.typespec.core.exception;
    exports com.typespec.core.http;
    exports com.typespec.core.http.policy;
    exports com.typespec.core.http.rest;
    exports com.typespec.core.models;
    exports com.typespec.core.util;
    exports com.typespec.core.util.builder;
    exports com.typespec.core.util.io;
    exports com.typespec.core.util.logging;
    exports com.typespec.core.util.paging;
    exports com.typespec.core.util.polling;
    exports com.typespec.core.util.serializer;
    exports com.typespec.core.util.tracing;
    exports com.typespec.core.util.metrics;

    exports com.typespec.core.implementation to com.typespec.core.serializer.json.jackson,
        com.typespec.core.serializer.json.gson,
        // export core implementation.ImplUtils to other core packages.
        com.typespec.core.experimental;

    // TODO temporary until we find final shape of ObjectMapper shimming APIs
    exports com.typespec.core.implementation.jackson to com.typespec.core.management, com.typespec.core.serializer.json.jackson;

    // export core utilities to other core packages.
    exports com.typespec.core.implementation.util to com.typespec.http.netty, com.typespec.core.http.okhttp,
        com.typespec.core.http.jdk.httpclient, com.typespec.core.serializer.json.jackson;
    exports com.typespec.core.util.polling.implementation to com.typespec.core.experimental;

    // exporting some packages specifically for Jackson
    opens com.typespec.core.credential to com.fasterxml.jackson.databind;
    opens com.typespec.core.http to com.fasterxml.jackson.databind;
    opens com.typespec.core.models to com.fasterxml.jackson.databind;
    opens com.typespec.core.util to com.fasterxml.jackson.databind;
    opens com.typespec.core.util.logging to com.fasterxml.jackson.databind;
    opens com.typespec.core.util.polling to com.fasterxml.jackson.databind;
    opens com.typespec.core.util.polling.implementation to com.fasterxml.jackson.databind;
    opens com.typespec.core.util.serializer to com.fasterxml.jackson.databind;
    opens com.typespec.core.implementation to com.fasterxml.jackson.databind;
    opens com.typespec.core.implementation.logging to com.fasterxml.jackson.databind;
    opens com.typespec.core.implementation.serializer to com.fasterxml.jackson.databind;
    opens com.typespec.core.implementation.jackson to com.fasterxml.jackson.databind;
    opens com.typespec.core.implementation.util to com.fasterxml.jackson.databind;
    opens com.typespec.core.implementation.http.rest to com.fasterxml.jackson.databind;
    opens com.typespec.core.http.rest to com.fasterxml.jackson.databind;

    // Service Provider Interfaces
    uses com.typespec.core.http.HttpClientProvider;
    uses com.typespec.core.http.policy.BeforeRetryPolicyProvider;
    uses com.typespec.core.http.policy.AfterRetryPolicyProvider;
    uses com.typespec.core.util.serializer.JsonSerializerProvider;
    uses com.typespec.core.util.serializer.MemberNameConverterProvider;
    uses com.typespec.core.util.tracing.Tracer;
    uses com.typespec.core.util.metrics.MeterProvider;
    uses com.typespec.core.util.tracing.TracerProvider;
}
