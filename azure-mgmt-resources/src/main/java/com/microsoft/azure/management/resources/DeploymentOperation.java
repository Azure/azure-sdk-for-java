/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.DeploymentOperationInner;
import org.joda.time.DateTime;

/**
 * An immutable client-side representation of a deployment operation.
 */
@LangDefinition(ContainerName="~/")
public interface DeploymentOperation extends
        Indexable,
        Refreshable<DeploymentOperation>,
        Wrapper<DeploymentOperationInner> {

    /**
     * @return the deployment operation id
     */
    String operationId();

    /**
     *
     * @return the state of the provisioning resource being deployed
     */
    String provisioningState();

    /**
     * @return the date and time of the operation
     */
    DateTime timestamp();

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
