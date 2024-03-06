// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.data.schemaregistry {
    requires transitive com.azure.core;
    requires com.azure.json;

    exports com.azure.data.schemaregistry;
    exports com.azure.data.schemaregistry.models;

    opens com.azure.data.schemaregistry to com.azure.core;
    opens com.azure.data.schemaregistry.implementation.models to com.azure.core;
}
