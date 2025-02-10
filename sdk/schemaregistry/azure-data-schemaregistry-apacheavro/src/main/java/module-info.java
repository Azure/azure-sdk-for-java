// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.data.schemaregistry.apacheavro {
    requires transitive com.azure.core;
    requires transitive com.azure.data.schemaregistry;

    requires org.apache.avro;

    exports com.azure.data.schemaregistry.apacheavro;

    opens com.azure.data.schemaregistry.apacheavro to com.azure.core;

}
