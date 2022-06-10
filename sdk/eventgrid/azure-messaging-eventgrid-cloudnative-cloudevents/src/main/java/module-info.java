// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.messaging.eventgrid.cloudnative.cloudevents {
    requires transitive com.azure.messaging.eventgrid;
    requires transitive io.cloudevents.api;
    exports com.azure.messaging.eventgrid.cloudnative.cloudevents;
}
