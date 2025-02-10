// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.analytics.synapse.managedprivateendpoints {
    requires transitive com.azure.core;

    exports com.azure.analytics.synapse.managedprivateendpoints;
    exports com.azure.analytics.synapse.managedprivateendpoints.models;

    opens com.azure.analytics.synapse.managedprivateendpoints.implementation to com.azure.core;
    opens com.azure.analytics.synapse.managedprivateendpoints.models to com.azure.core;
}
