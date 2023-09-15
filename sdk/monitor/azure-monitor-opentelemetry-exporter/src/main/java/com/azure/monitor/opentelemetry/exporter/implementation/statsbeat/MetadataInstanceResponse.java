// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Metadata instance response from the Azure Metadata Service.
 */
public class MetadataInstanceResponse {

    private final String vmId;

    private final String subscriptionId;

    private final String osType;

    private final String resourceGroupName;

    @JsonCreator
    MetadataInstanceResponse(
        @JsonProperty("vmId") String vmId,
        @JsonProperty("subscriptionId") String subscriptionId,
        @JsonProperty("osType") String osType,
        @JsonProperty("resourceGroupName") String resourceGroupName) {
        this.vmId = vmId;
        this.subscriptionId = subscriptionId;
        this.osType = osType;
        this.resourceGroupName = resourceGroupName;
    }

    String getVmId() {
        return vmId;
    }

    String getSubscriptionId() {
        return subscriptionId;
    }

    String getOsType() {
        return osType;
    }

    String getResourceGroupName() {
        return resourceGroupName;
    }
}
