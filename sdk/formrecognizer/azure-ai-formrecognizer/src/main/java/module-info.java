// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.ai.formrecognizer {
    requires transitive com.azure.core;

    exports com.azure.ai.formrecognizer;
    exports com.azure.ai.formrecognizer.documentanalysis.models;
    exports com.azure.ai.formrecognizer.documentanalysis.administration;
    exports com.azure.ai.formrecognizer.documentanalysis.administration.models;
    exports com.azure.ai.formrecognizer.documentanalysis;

    exports com.azure.ai.formrecognizer.models;
    exports com.azure.ai.formrecognizer.training;
    exports com.azure.ai.formrecognizer.training.models;

    opens com.azure.ai.formrecognizer.documentanalysis.implementation to com.fasterxml.jackson.databind;
    opens com.azure.ai.formrecognizer.documentanalysis.implementation.models to com.fasterxml.jackson.databind, com.azure.core;

    opens com.azure.ai.formrecognizer.implementation to com.fasterxml.jackson.databind;
    opens com.azure.ai.formrecognizer.models to com.fasterxml.jackson.databind;
    opens com.azure.ai.formrecognizer.training.models to com.fasterxml.jackson.databind;
    opens com.azure.ai.formrecognizer.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.ai.formrecognizer.documentanalysis.administration.models to com.azure.core, com.fasterxml.jackson.databind;
    opens com.azure.ai.formrecognizer.documentanalysis.models to com.azure.core, com.fasterxml.jackson.databind;
}
