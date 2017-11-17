/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.model;

import java.util.Map;

import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * An interface representing a backend's ability to reference a list of associated network interfaces.
 */
@Fluent()
public interface HasBackendNics  {
    /**
     * @return a map of names of the IP configurations of network interfaces assigned to this backend,
     * indexed by their NIC's resource id
     */
    Map<String, String> backendNicIPConfigurationNames();
}
