// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.serializer.avro.jackson {
    requires transitive com.azure.core;
    requires transitive com.fasterxml.jackson.dataformat.avro;

    exports com.azure.core.serializer.avro.jackson;

    provides com.azure.core.util.serializer.AvroSerializerProvider
        with com.azure.core.serializer.avro.jackson.JacksonAvroSerializerProvider;
}
