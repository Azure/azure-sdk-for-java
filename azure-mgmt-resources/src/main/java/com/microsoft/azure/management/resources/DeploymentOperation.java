package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.api.DeploymentOperationInner;
import com.microsoft.azure.management.resources.implementation.api.TargetResource;
import org.joda.time.DateTime;

/**
 * Defines the interface for accessing a deployment operation in Azure.
 */
public interface DeploymentOperation extends
        Indexable,
        Refreshable<DeploymentOperation>,
        Wrapper<DeploymentOperationInner> {

    /***********************************************************
     * Getters
     ***********************************************************/

    /**
     * Get the deployment operation id.
     *
     * @return the deployment operation id.
     */
    String operationId();

    /**
     * Get the state of the provisioning.
     *
     * @return the state of the provisioning.
     */
    String provisioningState();

    /**
     * Get the date and time of the operation.
     *
     * @return the date and time of the operation.
     */
    DateTime timestamp();

    /**
     * Get the operation status code.
     *
     * @return the operation status code.
     */
    String statusCode();

    /**
     * Get the operation status message.
     *
     * @return the operation status message.
     */
    Object statusMessage();

    /**
     * Get the target resource.
     *
     * @return the target resource.
     */
    TargetResource targetResource();
}
