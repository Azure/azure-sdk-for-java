// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluent.models.DeploymentOperationInner;

import java.time.OffsetDateTime;

/**
 * An immutable client-side representation of a deployment operation.
 */
@Fluent
public interface DeploymentOperation extends
        Indexable,
        Refreshable<DeploymentOperation>,
        HasInnerModel<DeploymentOperationInner> {

    /**
     * @return the deployment operation id
     */
    String operationId();

    /**
     * @return the state of the provisioning resource being deployed
     */
    String provisioningState();

    /**
     *
     * @return the name of the current provisioning operation
     */
    ProvisioningOperation provisioningOperation();

    /**
     * @return the date and time of the operation
     */
    OffsetDateTime timestamp();

    /**
     * @return the operation status code.=
     */
    String statusCode();

    /**
     * @return the operation status message
     */
    Object statusMessage();

    /**
     * @return the target resource
     */
    TargetResource targetResource();
}
