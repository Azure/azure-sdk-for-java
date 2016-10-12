/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager;

/**
 * An immutable client-side representation of an Azure traffic manager profile nested profile endpoint.
 */
public interface TrafficManagerNestedProfileEndpoint extends TrafficManagerEndpoint {
    /**
     * @return the nested traffic manager profile resource id
     */
    String nestedProfileId();

    /**
     * @return the number of child endpoints to be online to consider nested profile as healthy
     */
    int minChildEndpoints();
}
