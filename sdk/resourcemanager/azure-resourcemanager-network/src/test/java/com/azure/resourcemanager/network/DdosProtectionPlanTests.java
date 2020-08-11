// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.models.DdosProtectionPlan;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DdosProtectionPlanTests extends NetworkManagementTest {

    @Test
    public void canCRUDDdosProtectionPlan() throws Exception {
        String ppName = sdkContext.randomResourceName("ddosplan", 15);

        DdosProtectionPlan pPlan =
            networkManager
                .ddosProtectionPlans()
                .define(ppName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withNewResourceGroup(rgName)
                .withTag("tag1", "value1")
                .create();
        Assertions.assertEquals("value1", pPlan.tags().get("tag1"));

        PagedIterable<DdosProtectionPlan> ppList = networkManager.ddosProtectionPlans().list();
        Assertions.assertTrue(TestUtilities.getSize(ppList) > 0);

        ppList = networkManager.ddosProtectionPlans().listByResourceGroup(rgName);
        Assertions.assertTrue(TestUtilities.getSize(ppList) > 0);

        networkManager.ddosProtectionPlans().deleteById(pPlan.id());
        ppList = networkManager.ddosProtectionPlans().listByResourceGroup(rgName);
        Assertions.assertTrue(TestUtilities.isEmpty(ppList));
    }
}
