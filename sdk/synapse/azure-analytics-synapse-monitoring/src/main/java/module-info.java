// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.analytics.synapse.monitoring {
    requires transitive com.azure.core;

    exports com.azure.analytics.synapse.monitoring;
    exports com.azure.analytics.synapse.monitoring.models;

    opens com.azure.analytics.synapse.monitoring.models to com.fasterxml.jackson.databind;

}
