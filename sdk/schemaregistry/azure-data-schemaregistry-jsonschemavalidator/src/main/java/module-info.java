// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.data.schemaregistry.jsonschemavalidator {
    requires transitive com.azure.core;
    requires transitive com.azure.data.schemaregistry;

    exports com.azure.data.schemaregistry.jsonschemavalidator;

    opens com.azure.data.schemaregistry.jsonschemavalidator to com.fasterxml.jackson.databind, com.azure.core;
    exports com.azure.data.schemaregistry.jsonschemavalidator.models;
    opens com.azure.data.schemaregistry.jsonschemavalidator.models to com.azure.core, com.fasterxml.jackson.databind;

}
