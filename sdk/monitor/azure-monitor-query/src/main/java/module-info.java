// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * Module definition for azure-monitor-query.
 */
module com.azure.monitor.query {
    requires transitive com.azure.core;
    requires transitive com.azure.core.experimental;
    exports com.azure.monitor.query;
    exports com.azure.monitor.query.models;
    opens com.azure.monitor.query.log.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.monitor.query.metrics.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.monitor.query.metricsdefinitions.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.monitor.query.metricsnamespaces.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.monitor.query.models to com.fasterxml.jackson.databind, com.azure.core;
}
