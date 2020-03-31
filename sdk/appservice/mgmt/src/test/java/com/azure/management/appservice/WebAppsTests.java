/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import com.azure.core.http.rest.PagedIterable;
import com.azure.management.RestClient;
import com.azure.management.resources.core.TestUtilities;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WebAppsTests extends AppServiceTest {
    private String RG_NAME_1 = "";
    private String RG_NAME_2 = "";
    private String WEBAPP_NAME_1 = "";
    private String WEBAPP_NAME_2 = "";
    private String WEBAPP_NAME_3 = "";
    private String APP_SERVICE_PLAN_NAME_1 = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        WEBAPP_NAME_1 = generateRandomResourceName("java-webapp-", 20);
        WEBAPP_NAME_2 = generateRandomResourceName("java-webapp-", 20);
        WEBAPP_NAME_3 = generateRandomResourceName("java-webapp-", 20);
        APP_SERVICE_PLAN_NAME_1 = generateRandomResourceName("java-asp-", 20);
        RG_NAME_1 = generateRandomResourceName("javacsmrg", 20);
        RG_NAME_2 = generateRandomResourceName("javacsmrg", 20);

        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(RG_NAME_2);
        resourceManager.resourceGroups().beginDeleteByName(RG_NAME_1);
    }

    @Test
    public void canCRUDWebApp() throws Exception {
        // Create with new app service plan
        WebApp webApp1 = appServiceManager.webApps().define(WEBAPP_NAME_1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME_1)
                .withNewWindowsPlan(APP_SERVICE_PLAN_NAME_1, PricingTier.BASIC_B1)
                .withRemoteDebuggingEnabled(RemoteVisualStudioVersion.VS2019)
                .create();
        Assertions.assertNotNull(webApp1);
        Assertions.assertEquals(Region.US_WEST, webApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(webApp1.appServicePlanId());
        Assertions.assertNotNull(plan1);
        Assertions.assertEquals(APP_SERVICE_PLAN_NAME_1, plan1.name());
        Assertions.assertEquals(Region.US_WEST, plan1.region());
        Assertions.assertEquals(PricingTier.BASIC_B1, plan1.pricingTier());

        // Create in a new group with existing app service plan
        WebApp webApp2 = appServiceManager.webApps().define(WEBAPP_NAME_2)
                .withExistingWindowsPlan(plan1)
                .withNewResourceGroup(RG_NAME_2)
                .create();
        Assertions.assertNotNull(webApp2);
        Assertions.assertEquals(Region.US_WEST, webApp1.region());

        // Get
        WebApp webApp = appServiceManager.webApps().getByResourceGroup(RG_NAME_1, webApp1.name());
        Assertions.assertEquals(webApp1.id(), webApp.id());
        webApp = appServiceManager.webApps().getById(webApp2.id());
        Assertions.assertEquals(webApp2.name(), webApp.name());

        // List
        PagedIterable<WebApp> webApps = appServiceManager.webApps().listByResourceGroup(RG_NAME_1);
        Assertions.assertEquals(1, TestUtilities.getSize(webApps));
        webApps = appServiceManager.webApps().listByResourceGroup(RG_NAME_2);
        Assertions.assertEquals(1, TestUtilities.getSize(webApps));

        // Update
        webApp1.update()
                .withNewAppServicePlan(PricingTier.STANDARD_S2)
                .withRuntimeStack(WebAppRuntimeStack.NETCORE)
                .apply();
        AppServicePlan plan2 = appServiceManager.appServicePlans().getById(webApp1.appServicePlanId());
        Assertions.assertNotNull(plan2);
        Assertions.assertEquals(Region.US_WEST, plan2.region());
        Assertions.assertEquals(PricingTier.STANDARD_S2, plan2.pricingTier());
        Assertions.assertEquals(WebAppRuntimeStack.NETCORE.runtime(), webApp1.manager().inner().webApps().listMetadata(webApp1.resourceGroupName(), webApp1.name()).properties().get("CURRENT_STACK"));

        WebApp webApp3 = appServiceManager.webApps().define(WEBAPP_NAME_3)
                .withExistingWindowsPlan(plan1)
                .withExistingResourceGroup(RG_NAME_2)
                .withRuntimeStack(WebAppRuntimeStack.NET)
                .withNetFrameworkVersion(NetFrameworkVersion.V4_6)
                .create();
        Assertions.assertEquals(WebAppRuntimeStack.NET.runtime(), webApp3.manager().inner().webApps().listMetadata(webApp3.resourceGroupName(), webApp3.name()).properties().get("CURRENT_STACK"));
    }
}