// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

open module com.azure.resourcemanager.eventgrid {
    requires com.azure.resourcemanager;
    requires com.azure.identity;
    requires com.azure.messaging.eventgrid;
    requires com.azure.messaging.eventhubs;
    requires org.junit.jupiter.engine;
    requires org.junit.jupiter.params;
    requires org.junit.jupiter.api;
    requires org.mockito;
    requires net.bytebuddy;
    requires net.bytebuddy.agent;
}
