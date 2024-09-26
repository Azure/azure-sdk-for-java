// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.experimental {
    requires transitive com.azure.core;

    exports com.azure.core.experimental.serializer;
    exports com.azure.core.experimental.credential;
    exports com.azure.core.experimental.http;
    exports com.azure.core.experimental.models;
    exports com.azure.core.experimental.util.polling;
    exports com.azure.core.experimental.util.tracing;

    opens com.azure.core.experimental.models to com.azure.core, com.fasterxml.jackson.databind;
    opens com.azure.core.experimental.util.polling.implementation to com.azure.core, com.fasterxml.jackson.databind;

    uses com.azure.core.experimental.serializer.AvroSerializerProvider;

    provides com.azure.core.util.tracing.TracerProvider
        with com.azure.core.experimental.util.tracing.LoggingTracerProvider;
}
