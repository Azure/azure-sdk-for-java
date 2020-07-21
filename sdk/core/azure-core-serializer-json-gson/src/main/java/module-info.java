// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.serializer.json.gson {
    requires transitive com.azure.core;
    requires transitive com.azure.core.experimental;
    requires transitive gson;

    exports com.azure.core.serializer.json.gson;

    provides com.azure.core.experimental.serializer.JsonSerializerProvider
        with com.azure.core.serializer.json.gson.GsonJsonSerializerProvider;
}
