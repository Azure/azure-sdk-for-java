// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.annotation.Fluent;
import java.util.Map;

/** Response containing the login credentials for a container registry. */
@Fluent
public interface RegistryCredentials {
    /**
     * The admin user access key names and values which can be used to login into the container registry.
     *
     * @return the admin user access keys
     */
    Map<AccessKeyType, String> accessKeys();

    /**
     * Get the username value which can be used to login into the container registry.
     *
     * @return the username value
     */
    String username();
}
