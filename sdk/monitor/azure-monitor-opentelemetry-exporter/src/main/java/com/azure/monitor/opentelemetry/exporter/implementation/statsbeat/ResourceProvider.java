// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

enum ResourceProvider {
    RP_FUNCTIONS("functions"),
    RP_APPSVC("appsvc"),
    RP_VM("vm"),
    RP_AKS("aks"),
    UNKNOWN("unknown");

    private final String value;

    ResourceProvider(String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }
}
