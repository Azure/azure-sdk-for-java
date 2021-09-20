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
    exports com.azure.core.models;
    exports com.azure.core.util;
    exports com.azure.core.util.logging;
    exports com.azure.core.util.paging;
    exports com.azure.core.util.polling;
    exports com.azure.core.util.serializer;
    exports com.azure.core.util.tracing;

    // exporting some packages specifically for Jackson
    opens com.azure.core.http to com.fasterxml.jackson.databind;
    opens com.azure.core.models to com.fasterxml.jackson.databind;
    opens com.azure.core.util to com.fasterxml.jackson.databind;
    opens com.azure.core.util.logging to com.fasterxml.jackson.databind;
    opens com.azure.core.util.serializer to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.logging to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.serializer to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.jackson to com.fasterxml.jackson.databind;
    opens com.azure.core.http.rest to com.fasterxml.jackson.databind;

    // Service Provider Interfaces
    uses com.azure.core.http.HttpClientProvider;
    uses com.azure.core.http.policy.BeforeRetryPolicyProvider;
    uses com.azure.core.http.policy.AfterRetryPolicyProvider;
    uses com.azure.core.util.serializer.JsonSerializerProvider;
    uses com.azure.core.util.serializer.MemberNameConverterProvider;
    uses com.azure.core.util.tracing.Tracer;
}
