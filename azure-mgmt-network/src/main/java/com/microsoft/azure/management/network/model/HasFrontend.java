/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.model;

import com.microsoft.azure.management.network.Frontend;

/**
 * An interface representing a model's ability to references a load balancer frontend.
 */
public interface HasFrontend  {
    /**
     * @return the associated frontend
     */
    Frontend frontend();
}
