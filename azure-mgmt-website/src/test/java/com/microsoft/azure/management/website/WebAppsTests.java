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
import org.junit.Test;

import java.io.File;

public class WebAppsTests extends AppServiceTestBase {
    private static final String RG_NAME = "javacsmrg319";
    private static final String WEBAPP_NAME = "java-webapp-319";
    private static ResourceGroup resourceGroup;

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        //resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    public void canCRUDWebApp() throws Exception {
        Domain domain = appServiceManager.domains().getByGroup(RG_NAME, "javatestpr319.com");
        WebApp webApp = appServiceManager.webApps().define(WEBAPP_NAME)
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(RG_NAME)
                .withExistingAppServicePlan("java-plan-323")
                .withManagedHostNameBindings(domain, "@")
                .defineNewSSLBindingForHostName("javatestpr319.com")
                    .withPfxCertificateToUpload(new File("/Users/jianghlu/Documents/code/certs/javatestpr319_com.pfx"), "StrongPass!123")
                    .withSniSSL()
                    .attach()
                .create();

        Assert.assertNotNull(webApp);
    }
}