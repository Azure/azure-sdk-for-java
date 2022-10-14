// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.messaging.servicebus {
    requires transitive com.azure.core.amqp;

    requires com.fasterxml.jackson.dataformat.xml;

    exports com.azure.messaging.servicebus;
    exports com.azure.messaging.servicebus.administration;
    exports com.azure.messaging.servicebus.administration.models;
    exports com.azure.messaging.servicebus.models;

    opens com.azure.messaging.servicebus.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.messaging.servicebus.administration.models to com.fasterxml.jackson.databind, com.azure.core;
    exports com.azure.messaging.servicebus.implementation.instrumentation;

    uses com.azure.core.util.tracing.Tracer;
}
