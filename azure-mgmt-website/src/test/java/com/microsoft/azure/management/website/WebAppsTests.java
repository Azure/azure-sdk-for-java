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

public class WebAppsTests extends AppServiceTestBase {
    private static final String RG_NAME = "javacsmrg319";
    private static final String WEBAPP_NAME = "orange-webapp-319";
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
        AppServiceDomain domain = appServiceManager.domains().getByGroup(RG_NAME, "graph-webapp-319.com");
        WebApp webApp = appServiceManager.webApps().define(WEBAPP_NAME)
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(RG_NAME)
                .withExistingAppServicePlan("java-plan-323")
                .withManagedHostnameBindings(domain, "pineapple")
                .defineSslBinding()
                    .forHostname("pineapple.graph-webapp-319.com")
                    .withReadyToUseAppServiceCertificateOrder(appServiceManager.certificateOrders().getByGroup(RG_NAME, "graphwildcert"))
//                    .withNewKeyVault("orangecertvault")
                    .withSniBasedSsl()
                    .attach()
                .withRemoteDebuggingEnabled(RemoteVisualStudioVersion.VS2013)
                .create();

        Assert.assertNotNull(webApp);

        DeploymentSlot slot = webApp.deploymentSlots().define("newslot2")
                .withConfigurationFromDeploymentSlot(webApp.deploymentSlots().getByName("newslot")) // use name
                .defineHostnameBinding()
                    .withAzureManagedDomain(domain)
                    .withSubDomain("newslot2")
                    .withDnsRecordType(CustomHostNameDnsRecordType.CNAME)
                    .attach()
                .withAutoSwapSlotName("newslot")
                .create();

        Assert.assertNotNull(slot);
    }
}