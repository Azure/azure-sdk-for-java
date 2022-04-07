// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.serializeradapter.jackson {
    requires transitive com.azure.core;

    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;

    requires transitive com.fasterxml.jackson.dataformat.xml;
    requires transitive com.fasterxml.jackson.datatype.jsr310;

    exports com.azure.core.serializeradapter.jackson;

    // TODO temporary until we find final shape of ObjectMapper shimming APIs
    exports com.azure.core.serializeradapter.jackson.implementation to com.azure.core.management,
        com.azure.core.serializer.json.jackson;

    opens com.azure.core.serializeradapter.jackson to com.fasterxml.jackson.databind;
    opens com.azure.core.serializeradapter.jackson.implementation to com.fasterxml.jackson.databind;
}
