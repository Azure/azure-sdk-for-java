// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.serializer.json.gson {
    requires transitive com.azure.core;
    requires transitive com.azure.json;
    requires transitive com.google.gson;

    exports com.azure.core.serializer.json.gson;
    exports com.azure.core.serializer.json.gson.models;

    provides com.azure.json.JsonProvider with com.azure.core.serializer.json.gson.GsonJsonProvider;
    provides com.azure.core.util.serializer.MemberNameConverterProvider
        with com.azure.core.serializer.json.gson.GsonJsonSerializerProvider;
    provides com.azure.core.util.serializer.JsonSerializerProvider
        with com.azure.core.serializer.json.gson.GsonJsonSerializerProvider;
}
