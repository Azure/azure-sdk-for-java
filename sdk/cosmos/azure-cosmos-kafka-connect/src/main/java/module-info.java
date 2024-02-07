// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


module com.azure.cosmos.kafka.connect {

    requires transitive com.azure.cosmos;
    requires kafka.clients;
    requires connect.api;


    // public API surface area
    exports com.azure.cosmos.kafka.connect;
    exports com.azure.cosmos.kafka.connect.common;
    exports com.azure.cosmos.kafka.connect.source;
    exports com.azure.cosmos.kafka.connect.source.configs;

    uses com.azure.core.util.tracing.Tracer;
}
