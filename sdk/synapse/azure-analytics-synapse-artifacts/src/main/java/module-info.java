// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.analytics.synapse.artifacts {
    requires transitive com.azure.core;

    exports com.azure.analytics.synapse.artifacts;
    exports com.azure.analytics.synapse.artifacts.models;

    opens com.azure.analytics.synapse.artifacts.models to com.fasterxml.jackson.databind;

}
