// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.ai.metricsadvisor {
    requires transitive com.azure.core;

    exports com.azure.ai.metricsadvisor;
    exports com.azure.ai.metricsadvisor.models;
    exports com.azure.ai.metricsadvisor.administration;
    exports com.azure.ai.metricsadvisor.administration.models;

    opens com.azure.ai.metricsadvisor.implementation to com.fasterxml.jackson.databind;
    opens com.azure.ai.metricsadvisor.administration.models to com.fasterxml.jackson.databind;
    opens com.azure.ai.metricsadvisor.models to com.fasterxml.jackson.databind;
    opens com.azure.ai.metricsadvisor.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
}
