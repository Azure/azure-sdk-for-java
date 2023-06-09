// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.ai.textanalytics {
    requires transitive com.azure.core;

    exports com.azure.ai.textanalytics;
    exports com.azure.ai.textanalytics.models;
    exports com.azure.ai.textanalytics.util;

    opens com.azure.ai.textanalytics.implementation to com.fasterxml.jackson.databind;
    opens com.azure.ai.textanalytics.models to com.fasterxml.jackson.databind;
    opens com.azure.ai.textanalytics.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
}
