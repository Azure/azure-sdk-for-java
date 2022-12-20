// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.serializer.avro.apache {
    requires transitive com.azure.core;
    requires transitive com.azure.core.experimental;
    requires transitive org.apache.avro;

    exports com.azure.core.serializer.avro.apache;

    provides com.azure.core.experimental.serializer.AvroSerializerProvider
        with com.azure.core.serializer.avro.apache.ApacheAvroSerializerProvider;
}
