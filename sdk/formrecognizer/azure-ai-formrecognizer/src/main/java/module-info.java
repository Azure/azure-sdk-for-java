// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.ai.formrecognizer {
    requires transitive com.azure.core;

    exports com.azure.ai.formrecognizer;
    exports com.azure.ai.formrecognizer.models;
    exports com.azure.ai.formrecognizer.administration;
    exports com.azure.ai.formrecognizer.administration.models;

    opens com.azure.ai.formrecognizer.implementation to com.fasterxml.jackson.databind;
    opens com.azure.ai.formrecognizer.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.ai.formrecognizer.administration.models to com.fasterxml.jackson.databind;
    opens com.azure.ai.formrecognizer.models to com.fasterxml.jackson.databind;
}
