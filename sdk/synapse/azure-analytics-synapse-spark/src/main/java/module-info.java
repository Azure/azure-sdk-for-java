// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.analytics.synapse.spark {
    requires transitive com.azure.core;

    exports com.azure.analytics.synapse.spark;
    exports com.azure.analytics.synapse.spark.models;

    opens com.azure.analytics.synapse.spark.models to com.fasterxml.jackson.databind;

}
