// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.data.schemaregistry.client {
    requires transitive com.azure.core;

    exports com.azure.data.schemaregistry.client;

    opens com.azure.data.schemaregistry.client to com.fasterxml.jackson.databind;
    opens com.azure.data.schemaregistry.client.rest to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.data.schemaregistry.client.models to com.fasterxml.jackson.databind, com.azure.core;
}
