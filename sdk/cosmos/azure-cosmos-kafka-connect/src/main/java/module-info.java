// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


module com.azure.cosmos.kafka.connect {

    requires transitive com.azure.cosmos;
    requires kafka.clients;


    // public API surface area
    exports com.azure.cosmos.kafka.connect;

    uses com.azure.core.util.tracing.Tracer;
}
