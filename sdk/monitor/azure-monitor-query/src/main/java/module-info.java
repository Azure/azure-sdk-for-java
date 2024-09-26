// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * Module definition for azure-monitor-query.
 */
module com.azure.monitor.query {
    requires transitive com.azure.core;

    exports com.azure.monitor.query;
    exports com.azure.monitor.query.models;

    opens com.azure.monitor.query.models to com.azure.core;
    opens com.azure.monitor.query.implementation.logs.models to com.azure.core;
    opens com.azure.monitor.query.implementation.metrics.models to com.azure.core;
    opens com.azure.monitor.query.implementation.metricsdefinitions.models to com.azure.core;
    opens com.azure.monitor.query.implementation.metricsnamespaces.models to com.azure.core;
    opens com.azure.monitor.query.implementation.metricsbatch.models to com.azure.core;
}
