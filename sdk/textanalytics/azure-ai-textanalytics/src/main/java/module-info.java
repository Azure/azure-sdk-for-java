// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.ai.textanalytics {
    requires transitive com.azure.core;
    requires com.azure.json;

    exports com.azure.ai.textanalytics;
    exports com.azure.ai.textanalytics.models;
    exports com.azure.ai.textanalytics.util;

    opens com.azure.ai.textanalytics.implementation to com.azure.core;
    opens com.azure.ai.textanalytics.models to com.azure.core;
    opens com.azure.ai.textanalytics.implementation.models to com.azure.core;
}
