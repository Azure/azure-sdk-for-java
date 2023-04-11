// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.serializer.json.jackson {
    requires com.azure.json;
    requires transitive com.azure.core;

    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;

    requires com.fasterxml.jackson.annotation;

    exports com.azure.core.serializer.json.jackson;

    provides com.azure.core.util.serializer.MemberNameConverterProvider
        with com.azure.core.serializer.json.jackson.JacksonJsonSerializerProvider;
    provides com.azure.core.util.serializer.JsonSerializerProvider
        with com.azure.core.serializer.json.jackson.JacksonJsonSerializerProvider;
}
