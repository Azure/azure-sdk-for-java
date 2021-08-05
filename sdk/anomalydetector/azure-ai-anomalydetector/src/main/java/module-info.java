// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.ai.anomalydetector {
    requires transitive com.azure.core;

    exports com.azure.ai.anomalydetector;
    exports com.azure.ai.anomalydetector.models;

    opens com.azure.ai.anomalydetector.models to com.fasterxml.jackson.databind;

}
