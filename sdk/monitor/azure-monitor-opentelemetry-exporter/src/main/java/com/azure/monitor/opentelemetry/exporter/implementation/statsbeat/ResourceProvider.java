// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import com.azure.monitor.opentelemetry.exporter.implementation.utils.Strings;

enum ResourceProvider {
    RP_FUNCTIONS("functions"),
    RP_APPSVC("appsvc"),
    RP_VM("vm"),
    RP_AKS("aks"),
    RP_SPRING_CLOUD("springcloud"),
    UNKNOWN("unknown");

    private final String value;

    ResourceProvider(String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }

    static ResourceProvider initResourceProvider() {
        if ("java".equals(System.getenv("FUNCTIONS_WORKER_RUNTIME"))) {
            return ResourceProvider.RP_FUNCTIONS;
        } else if (!Strings.isNullOrEmpty(System.getenv("WEBSITE_SITE_NAME"))) {
            return ResourceProvider.RP_APPSVC;
        } else if (!Strings.isNullOrEmpty(System.getenv("KUBERNETES_SERVICE_HOST"))) {
            return ResourceProvider.RP_AKS;
        } else if (!Strings.isNullOrEmpty(System.getenv("APPLICATIONINSIGHTS_SPRINGCLOUD_SERVICE_ID"))) {
            return ResourceProvider.RP_SPRING_CLOUD;
        } else {
            return ResourceProvider.UNKNOWN;
        }
    }
}
