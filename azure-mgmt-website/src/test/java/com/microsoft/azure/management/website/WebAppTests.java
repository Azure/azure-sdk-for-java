/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class WebAppTests extends AppServiceTestBase {
    private static final String RG_NAME = "javacsmrg219";
    private static final String WEBAPP_NAME = "java-webapp-219";
    private static ResourceGroup resourceGroup;

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        //resourceManager.resourceGroups().delete(RG_NAME);
    }

    @Ignore
    public void canCreateWebApp() throws Exception {
        WebApp webApp = appServiceManager.sites().define(WEBAPP_NAME)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME)
                .withNewAppServicePlan("java-webapp-plan-219", AppServicePricingTier.STANDARD_S1)
                .create();
        Assert.assertNotNull(webApp);
    }

    @Test
    public void canUpdateWebApp() throws Exception {
        WebApp webApp = appServiceManager.sites().getByGroup(RG_NAME, WEBAPP_NAME);
        webApp.update()
                .defineHostNameBinding("javatest.quarterquota.net")
                .withHostNameType(HostNameType.VERIFIED)
                .withHostNameDnsRecordType(CustomHostNameDnsRecordType.A)
                .attach()
                .enableSniSsl("javatest.quarterquota.net", "56DD1FF5F5FD02D829B6BC47DC5B733CA93492CF")
                .apply();
    }
}