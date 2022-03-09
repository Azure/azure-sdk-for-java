// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

/**
 * Result of Kudu deployment.
 */
public final class KuduDeploymentResult {

    private final String deploymentId;

    /**
     * Creates a KuduDeploymentResult instance.
     *
     * @param deploymentId the deployment ID.
     */
    public KuduDeploymentResult(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    /**
     * @return the deployment ID. It can be {@code null} if tracking deployment is disabled.
     */
    public String deploymentId() {
        return deploymentId;
    }
}
