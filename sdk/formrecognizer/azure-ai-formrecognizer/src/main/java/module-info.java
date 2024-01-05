// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.ai.formrecognizer {
    requires transitive com.azure.core;
    requires com.azure.json;

    exports com.azure.ai.formrecognizer;

    exports com.azure.ai.formrecognizer.documentanalysis;
    exports com.azure.ai.formrecognizer.documentanalysis.administration;
    exports com.azure.ai.formrecognizer.documentanalysis.administration.models;
    exports com.azure.ai.formrecognizer.documentanalysis.models;

    exports com.azure.ai.formrecognizer.models;
    exports com.azure.ai.formrecognizer.training;
    exports com.azure.ai.formrecognizer.training.models;

    opens com.azure.ai.formrecognizer.documentanalysis.implementation to com.azure.core;
    opens com.azure.ai.formrecognizer.documentanalysis.implementation.models to com.azure.core;

    opens com.azure.ai.formrecognizer.implementation to com.azure.core;
    opens com.azure.ai.formrecognizer.models to com.azure.core;
    opens com.azure.ai.formrecognizer.training.models to com.azure.core;
    opens com.azure.ai.formrecognizer.implementation.models to com.azure.core;
    opens com.azure.ai.formrecognizer.documentanalysis.administration.models to com.azure.core;
    opens com.azure.ai.formrecognizer.documentanalysis.models to com.azure.core;
}
