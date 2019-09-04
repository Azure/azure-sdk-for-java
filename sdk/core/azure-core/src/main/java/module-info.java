// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core {
    requires java.xml;

    requires com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.datatype.jsr310;

    requires transitive reactor.core;
    requires org.reactivestreams;

    requires org.slf4j;

    // public API surface area
    exports com.azure.core;
    exports com.azure.core.annotation;
    exports com.azure.core.credentials;
    exports com.azure.core.exception;
    exports com.azure.core.http;
    exports com.azure.core.http.policy;
    exports com.azure.core.http.rest;
    exports com.azure.core.util;
    exports com.azure.core.util.configuration;
    exports com.azure.core.util.logging;
    exports com.azure.core.util.polling;
    exports com.azure.core.util.tracing;

    // exporting some packages specifically for Jackson
    opens com.azure.core.http to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.serializer to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.serializer.jackson to com.fasterxml.jackson.databind;
    opens com.azure.core.implementation.util to com.fasterxml.jackson.databind;

    // exporting some packages for internal use only
    exports com.azure.core.implementation to
        com.azure.security.keyvault.keys,       // FIXME this should not be a long-term solution
        com.azure.security.keyvault.secrets;    // FIXME this should not be a long-term solution
    exports com.azure.core.implementation.http to
        com.azure.test,
        com.azure.identity,                     // FIXME this should not be a long-term solution
        com.azure.storage.common;               // FIXME this should not be a long-term solution
    exports com.azure.core.implementation.serializer to
        com.azure.test,
        com.azure.identity;                     // FIXME this should not be a long-term solution
    exports com.azure.core.implementation.serializer.jackson to
        com.azure.test,
        com.azure.identity;                     // FIXME this should not be a long-term solution
    exports com.azure.core.implementation.util to
        com.azure.test,
        com.azure.amqp,                         // FIXME this should not be a long-term solution
        com.azure.messaging.eventhubs,          // FIXME this should not be a long-term solution
        com.azure.identity,                     // FIXME this should not be a long-term solution
        com.azure.security.keyvault.keys,       // FIXME this should not be a long-term solution
        com.azure.security.keyvault.secrets,    // FIXME this should not be a long-term solution
        com.azure.storage.common,               // FIXME this should not be a long-term solution
        com.azure.tracing.opentelemetry;        // FIXME this should not be a long-term solution

    // service provider interfaces
    uses com.azure.core.util.tracing.Tracer;
    uses com.azure.core.http.HttpClientProvider;
    uses com.azure.core.http.policy.BeforeRetryPolicyProvider;
    uses com.azure.core.http.policy.AfterRetryPolicyProvider;
}
