// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.experimental {
    requires transitive com.azure.core;

    exports com.azure.core.experimental.geojson;
    exports com.azure.core.experimental.serializer;
    exports com.azure.core.experimental.implementation;
    exports com.azure.core.experimental.http;
    exports com.azure.core.experimental.http.policy;

    opens com.azure.core.experimental.geojson to com.fasterxml.jackson.databind;

    uses com.azure.core.experimental.serializer.AvroSerializerProvider;
}
