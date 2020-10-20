// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Metrics Advisor supported by this client library.
 */
public enum MetricsAdvisorServiceVersion implements ServiceVersion {
    // TODO: confirm and change
    V2_0("v2.0");

    private final String version;

    MetricsAdvisorServiceVersion(String version) {
        this.version = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link MetricsAdvisorServiceVersion}
     */
    public static MetricsAdvisorServiceVersion getLatest() {
        return V2_0;
    }

}
