// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import java.util.Map;

/** An interface representing a backend's ability to reference a list of associated network interfaces. */
@Fluent()
public interface HasBackendNics {
    /**
     * @return a map of names of the IP configurations of network interfaces assigned to this backend, indexed by their
     *     NIC's resource id
     */
    Map<String, String> backendNicIPConfigurationNames();
}
