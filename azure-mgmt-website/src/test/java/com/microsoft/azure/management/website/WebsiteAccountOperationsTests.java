/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class WebsiteAccountOperationsTests extends WebsiteManagementTestBase {
    private static final String RG_NAME = "javacsmrg219";
    private static final String WEBAPP_NAME = "java-webapp-219";
    private static ResourceGroup resourceGroup;

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManager.resourceGroups().delete(RG_NAME);
    }

    @Test
    public void canCreateWebApp() throws Exception {
        Site site = websiteManager.sites().define(WEBAPP_NAME)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME)
                .withExistingAppServicePlan("Default1")
                .create();
    }
}
