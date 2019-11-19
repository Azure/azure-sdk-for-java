// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.amqp {
    requires transitive com.azure.core;
    requires transitive reactor.core;

    requires proton.j;
    requires qpid.proton.j.extensions;
    requires org.reactivestreams;

    exports com.azure.core.amqp;
    exports com.azure.core.amqp.exception;

    // FIXME this should not be a long-term solution
    exports com.azure.core.amqp.implementation to
        com.azure.messaging.eventhubs;
    exports com.azure.core.amqp.implementation.handler to
        com.azure.messaging.eventhubs;
}
