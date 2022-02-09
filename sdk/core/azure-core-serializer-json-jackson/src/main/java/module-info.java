// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.serializer.json.jackson {
    requires transitive com.azure.core;

    exports com.azure.core.serializer.json.jackson;

    provides com.azure.core.util.serializer.MemberNameConverterProvider
        with com.azure.core.serializer.json.jackson.JacksonJsonSerializerProvider;
    provides com.azure.core.util.serializer.JsonSerializerProvider
        with com.azure.core.serializer.json.jackson.JacksonJsonSerializerProvider;
}
