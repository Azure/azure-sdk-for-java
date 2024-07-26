// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.data.schemaregistry.jsonschema {
    requires transitive com.azure.core;
    requires transitive com.azure.data.schemaregistry;

    exports com.azure.data.schemaregistry.jsonschema;

    opens com.azure.data.schemaregistry.jsonschema to com.azure.core;
}
