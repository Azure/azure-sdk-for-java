// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.monitor.dataingestion {
    requires transitive com.azure.core;
    requires com.azure.identity;
    exports com.azure.monitor.logsingestion;

    opens com.azure.monitor.logsingestion to com.fasterxml.jackson.databind;

}
