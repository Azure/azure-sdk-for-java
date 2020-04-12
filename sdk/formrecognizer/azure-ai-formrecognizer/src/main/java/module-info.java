// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.ai.formrecognizer {
    requires transitive com.azure.core;
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;

    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.datatype.jsr310;

    exports com.azure.ai.formrecognizer;
    exports com.azure.ai.formrecognizer.models;

    opens com.azure.ai.formrecognizer.implementation to com.fasterxml.jackson.databind;
    opens com.azure.ai.formrecognizer.models to com.fasterxml.jackson.databind;
    opens com.azure.ai.formrecognizer.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
}
