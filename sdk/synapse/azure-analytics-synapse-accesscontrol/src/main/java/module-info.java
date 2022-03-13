// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.analytics.synapse.accesscontrol {
    requires transitive com.azure.core;

    exports com.azure.analytics.synapse.accesscontrol;
    exports com.azure.analytics.synapse.accesscontrol.models;

    opens com.azure.analytics.synapse.accesscontrol.models to com.fasterxml.jackson.databind;

}
