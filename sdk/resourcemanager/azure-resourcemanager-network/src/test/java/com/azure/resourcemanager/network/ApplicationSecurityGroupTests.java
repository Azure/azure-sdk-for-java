// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.models.ApplicationSecurityGroup;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.core.management.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ApplicationSecurityGroupTests extends NetworkManagementTest {

    @Test
    public void canCRUDApplicationSecurityGroup() throws Exception {
        String asgName = generateRandomResourceName("asg", 15);

        ApplicationSecurityGroup applicationSecurityGroup =
            networkManager
                .applicationSecurityGroups()
                .define(asgName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withNewResourceGroup(rgName)
                .withTag("tag1", "value1")
                .create();
        Assertions.assertEquals("value1", applicationSecurityGroup.tags().get("tag1"));

        PagedIterable<ApplicationSecurityGroup> asgList = networkManager.applicationSecurityGroups().list();
        Assertions.assertTrue(TestUtilities.getSize(asgList) > 0);

        asgList = networkManager.applicationSecurityGroups().listByResourceGroup(rgName);
        Assertions.assertTrue(TestUtilities.getSize(asgList) > 0);

        networkManager.applicationSecurityGroups().deleteById(applicationSecurityGroup.id());
        asgList = networkManager.applicationSecurityGroups().listByResourceGroup(rgName);
        Assertions.assertTrue(TestUtilities.isEmpty(asgList));
    }
}
