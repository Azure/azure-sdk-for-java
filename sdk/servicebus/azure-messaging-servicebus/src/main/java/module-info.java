// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.messaging.servicebus {
    requires transitive com.azure.core.amqp;

    exports com.azure.messaging.servicebus;
    exports com.azure.messaging.servicebus.models;

    opens com.azure.messaging.servicebus;
    opens com.azure.messaging.servicebus.implementation;

    uses com.azure.core.util.tracing.Tracer;
}
