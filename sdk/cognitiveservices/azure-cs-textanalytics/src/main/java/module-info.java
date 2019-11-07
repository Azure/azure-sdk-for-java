// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.cs.textanalytics {
    requires transitive com.azure.core;

    opens com.azure.cs.textanalytics.implementation to com.fasterxml.jackson.databind;
    opens com.azure.cs.textanalytics.models to com.fasterxml.jackson.databind;

    exports com.azure.cs.textanalytics;
    exports com.azure.cs.textanalytics.models;
}
