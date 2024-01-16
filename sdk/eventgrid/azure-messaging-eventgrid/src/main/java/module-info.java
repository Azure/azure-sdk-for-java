// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.messaging.eventgrid {
    requires transitive com.azure.core;
    exports com.azure.messaging.eventgrid;
    exports com.azure.messaging.eventgrid.systemevents;

    opens com.azure.messaging.eventgrid.implementation.models to com.fasterxml.jackson.databind;
    opens com.azure.messaging.eventgrid.systemevents to com.fasterxml.jackson.databind;
}
