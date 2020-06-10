// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.data.schemaregistry {
    requires transitive com.azure.core;

    exports com.azure.data.schemaregistry;
    exports com.azure.data.schemaregistry.client;

    opens com.azure.data.schemaregistry to com.fasterxml.jackson.databind, com.azure.core;
}
