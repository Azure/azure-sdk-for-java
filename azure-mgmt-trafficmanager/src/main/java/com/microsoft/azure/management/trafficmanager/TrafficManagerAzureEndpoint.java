/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager;

import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * An immutable client-side representation of an Azure traffic manager profile Azure endpoint.
 */
@Fluent
public interface TrafficManagerAzureEndpoint extends TrafficManagerEndpoint {
    /**
     * @return the resource id of the target Azure resource.
     */
    String targetAzureResourceId();

    /**
     * @return the type of the target Azure resource.
     */
    TargetAzureResourceType targetResourceType();
}
