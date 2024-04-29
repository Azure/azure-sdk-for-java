// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


module com.azure.cosmos.kafka.connect {

    requires transitive com.azure.cosmos;
    requires kafka.clients;
    requires connect.api;
    requires json.path;
    requires com.azure.identity;

    // public API surface area
    exports com.azure.cosmos.kafka.connect;
    uses com.azure.core.util.tracing.Tracer;
}
