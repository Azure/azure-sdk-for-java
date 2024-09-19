// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.messaging.eventhubs {
    requires transitive com.azure.core;
    requires transitive com.azure.core.amqp;

    requires com.microsoft.azure.qpid.protonj.extensions;

    exports com.azure.messaging.eventhubs;
    exports com.azure.messaging.eventhubs.models;

    opens com.azure.messaging.eventhubs;
    opens com.azure.messaging.eventhubs.models;

    opens com.azure.messaging.eventhubs.implementation;
    opens com.azure.messaging.eventhubs.implementation.instrumentation to com.azure.core;
    uses com.azure.core.util.tracing.Tracer;
}
