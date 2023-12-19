// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.ai.metricsadvisor {
    requires transitive com.azure.core;
    requires com.azure.json;

    exports com.azure.ai.metricsadvisor;
    exports com.azure.ai.metricsadvisor.models;
    exports com.azure.ai.metricsadvisor.administration;
    exports com.azure.ai.metricsadvisor.administration.models;

    opens com.azure.ai.metricsadvisor.implementation to com.azure.core;
    opens com.azure.ai.metricsadvisor.administration.models to com.azure.core;
    opens com.azure.ai.metricsadvisor.models to com.azure.core;
    opens com.azure.ai.metricsadvisor.implementation.models to com.azure.core;
}
