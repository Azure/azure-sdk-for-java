// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.schemaregistry.client {
    requires transitive com.azure.core;

    exports com.azure.schemaregistry.client;
    exports com.azure.schemaregistry.client.rest;
    exports com.azure.schemaregistry.client.rest.models;

    opens com.azure.schemaregistry.client to com.fasterxml.jackson.databind;
    opens com.azure.schemaregistry.client.rest to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.schemaregistry.client.models to com.fasterxml.jackson.databind, com.azure.core;
}
