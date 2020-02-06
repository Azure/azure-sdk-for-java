/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.mgmt;

import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.implementation.ResourceManager;

public class ResourceGroupTool {

    private ResourceGroups groups;
    
    public ResourceGroupTool(Access access) {
        groups = ResourceManager
                .authenticate(access.credentials())
                .withSubscription(access.subscription())
                .resourceGroups();
    }
    
    public void deleteGroup(String name) {
        groups.deleteByName(name);
    }
}
