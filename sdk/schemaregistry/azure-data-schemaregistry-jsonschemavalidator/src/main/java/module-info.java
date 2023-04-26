// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.data.schemaregistry.jsonschemavalidator {
    requires transitive com.azure.core;
    requires transitive com.azure.data.schemaregistry;

    requires org.apache.avro;

    exports com.azure.data.schemaregistry.jsonschemavalidator;

    opens com.azure.data.schemaregistry.jsonschemavalidator to com.fasterxml.jackson.databind, com.azure.core;

}
