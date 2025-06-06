// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.messaging.eventgrid {
    requires transitive com.azure.core;
    requires transitive com.azure.json;

    exports com.azure.messaging.systemevents;
    exports com.azure.messaging.systemevents.systemevents;

    opens com.azure.messaging.systemevents.implementation.models to com.azure.core;
    opens com.azure.messaging.systemevents.systemevents to com.azure.core;
}
