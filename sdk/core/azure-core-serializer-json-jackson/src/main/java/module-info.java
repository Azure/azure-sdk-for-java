// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.serializer.json.jackson {
    requires transitive com.azure.core;

    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;

    exports com.azure.core.serializer.json.jackson;

    provides com.azure.json.JsonProvider with com.azure.core.serializer.json.jackson.JacksonJsonProvider;
    provides com.azure.core.util.serializer.MemberNameConverterProvider
        with com.azure.core.serializer.json.jackson.JacksonJsonSerializerProvider;
    provides com.azure.core.util.serializer.JsonSerializerProvider
        with com.azure.core.serializer.json.jackson.JacksonJsonSerializerProvider;
}
