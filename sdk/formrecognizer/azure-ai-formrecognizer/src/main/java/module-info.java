// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.ai.formrecognizer {
    requires transitive com.azure.core;

    exports com.azure.ai.formrecognizer;
    exports com.azure.ai.formrecognizer.models;
    exports com.azure.ai.formrecognizer.training;
    exports com.azure.ai.formrecognizer.training.models;

    opens com.azure.ai.formrecognizer.implementation to com.fasterxml.jackson.databind;
    opens com.azure.ai.formrecognizer.models to com.fasterxml.jackson.databind;
    opens com.azure.ai.formrecognizer.training.models to com.fasterxml.jackson.databind;
    opens com.azure.ai.formrecognizer.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
}
