// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.data.schemaregistry.apacheavro {
    requires transitive com.azure.data.schemaregistry;
    requires transitive com.azure.core.serializer.avro.apache;

    exports com.azure.data.schemaregistry.apacheavro;

    opens com.azure.data.schemaregistry.apacheavro to com.fasterxml.jackson.databind, com.azure.core;

}
