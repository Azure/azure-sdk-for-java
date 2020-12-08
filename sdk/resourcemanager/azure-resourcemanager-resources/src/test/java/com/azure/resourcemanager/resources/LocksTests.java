// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.resources.models.LockLevel;
import com.azure.resourcemanager.resources.models.ManagementLock;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LocksTests extends ResourceManagementTest {

    @Test
    public void canCreateLock() {
        String rgName = generateRandomResourceName("rgloc", 15);
        String lockName = generateRandomResourceName("lock", 15);

        ResourceGroup resourceGroup = resourceClient.resourceGroups().define(rgName)
            .withRegion(Region.US_WEST)
            .create();

        ManagementLock lock = resourceClient.managementLocks().define(lockName)
            .withLockedResourceGroup(resourceGroup)
            .withLevel(LockLevel.CAN_NOT_DELETE)
            .create();

        Assertions.assertNotNull(lock);
        Assertions.assertEquals(resourceGroup.id(), lock.lockedResourceId());

        try {
            resourceClient.resourceGroups().deleteByName(rgName);
        } catch (ManagementException ex) {
            Assertions.assertEquals("ScopeLocked", ex.getValue().getCode());
        }

        resourceClient.managementLocks().deleteById(lock.id());
        resourceClient.resourceGroups().deleteByName(rgName);
    }
}
