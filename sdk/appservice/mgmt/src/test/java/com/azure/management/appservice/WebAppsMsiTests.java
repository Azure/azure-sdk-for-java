/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import com.azure.core.http.rest.Response;
import com.azure.management.RestClient;
import com.azure.management.graphrbac.BuiltInRole;
import com.azure.management.msi.Identity;
import com.azure.management.msi.implementation.MSIManager;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class WebAppsMsiTests extends AppServiceTest {
    private MSIManager msiManager;
    private String RG_NAME_1 = "";
    private String WEBAPP_NAME_1 = "";
    private String VAULT_NAME = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        WEBAPP_NAME_1 = generateRandomResourceName("java-webapp-", 20);
        RG_NAME_1 = generateRandomResourceName("javacsmrg", 20);
        VAULT_NAME = generateRandomResourceName("java-vault-", 20);
        this.msiManager = MSIManager.authenticate(restClient, defaultSubscription, sdkContext);

        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(RG_NAME_1);
        try {
            resourceManager.resourceGroups().beginDeleteByName(RG_NAME);
        } catch (Exception e) {
            // fine, RG_NAME is not created
        }
    }

    @Test
    public void canCRUDWebAppWithMsi() throws Exception {
        // Create with new app service plan
        WebApp webApp = appServiceManager.webApps().define(WEBAPP_NAME_1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME_1)
                .withNewWindowsPlan(PricingTier.BASIC_B1)
                .withRemoteDebuggingEnabled(RemoteVisualStudioVersion.VS2019)
                .withSystemAssignedManagedServiceIdentity()
                .withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR)
                .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
                .withWebContainer(WebContainer.TOMCAT_8_0_NEWEST)
                .create();
        Assertions.assertNotNull(webApp);
        Assertions.assertEquals(Region.US_WEST, webApp.region());
        AppServicePlan plan = appServiceManager.appServicePlans().getById(webApp.appServicePlanId());
        Assertions.assertNotNull(plan);
        Assertions.assertEquals(Region.US_WEST, plan.region());
        Assertions.assertEquals(PricingTier.BASIC_B1, plan.pricingTier());
        Assertions.assertNotNull(webApp.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(webApp.systemAssignedManagedServiceIdentityTenantId());

        if (!isPlaybackMode()) {
            // Check availability of environment variables
            uploadFileToWebApp(webApp.getPublishingProfile(), "appservicemsi.war", WebAppsMsiTests.class.getResourceAsStream("/appservicemsi.war"));

            SdkContext.sleep(30000);

            Response<String> response = curl("http://" + WEBAPP_NAME_1 + "." + "azurewebsites.net/appservicemsi/");
            Assertions.assertEquals(200, response.getStatusCode());
            String body = response.getValue();
            Assertions.assertNotNull(body);
            Assertions.assertTrue(body.contains(webApp.resourceGroupName()));
            Assertions.assertTrue(body.contains(webApp.id()));
        }
    }

    @Test
    public void canCRUDWebAppWithUserAssignedMsi() throws Exception {

        String identityName1 = generateRandomResourceName("msi-id", 15);
        String identityName2 = generateRandomResourceName("msi-id", 15);

        // Prepare a definition for yet-to-be-created resource group
        //
        Creatable<ResourceGroup> creatableRG = resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(Region.US_WEST);

        // Create an "User Assigned (External) MSI" residing in the above RG and assign reader access to the virtual network
        //
        final Identity createdIdentity = msiManager.identities()
                .define(identityName1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(creatableRG)
                .withAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR)
                .create();

        // Prepare a definition for yet-to-be-created "User Assigned (External) MSI" with contributor access to the resource group
        // it resides
        //
        Creatable<Identity> creatableIdentity = msiManager.identities()
                .define(identityName2)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(creatableRG)
                .withAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR);

        // Create with new app service plan
        WebApp webApp = appServiceManager.webApps().define(WEBAPP_NAME_1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME_1)
                .withNewWindowsPlan(PricingTier.BASIC_B1)
                .withRemoteDebuggingEnabled(RemoteVisualStudioVersion.VS2019)
                .withSystemAssignedManagedServiceIdentity()
                .withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR)
                .withJavaVersion(JavaVersion.JAVA_8_NEWEST)
                .withWebContainer(WebContainer.TOMCAT_8_0_NEWEST)
                .withUserAssignedManagedServiceIdentity()
                .withNewUserAssignedManagedServiceIdentity(creatableIdentity)
                .withExistingUserAssignedManagedServiceIdentity(createdIdentity)
                .create();
        Assertions.assertNotNull(webApp);
        Assertions.assertEquals(Region.US_WEST, webApp.region());
        AppServicePlan plan = appServiceManager.appServicePlans().getById(webApp.appServicePlanId());
        Assertions.assertNotNull(plan);
        Assertions.assertEquals(Region.US_WEST, plan.region());
        Assertions.assertEquals(PricingTier.BASIC_B1, plan.pricingTier());
        Assertions.assertNotNull(webApp.systemAssignedManagedServiceIdentityPrincipalId());
        Assertions.assertNotNull(webApp.systemAssignedManagedServiceIdentityTenantId());
        Set<String> identityIds = webApp.userAssignedManagedServiceIdentityIds();
        Assertions.assertNotNull(identityIds);
        Assertions.assertEquals(identityIds.size(), 2);
        Assertions.assertTrue(setContainsValue(identityIds, identityName1));
        Assertions.assertTrue(setContainsValue(identityIds, identityName2));

        if (!isPlaybackMode()) {
            // Check availability of environment variables
            uploadFileToWebApp(webApp.getPublishingProfile(), "appservicemsi.war", WebAppsMsiTests.class.getResourceAsStream("/appservicemsi.war"));

            SdkContext.sleep(30000);

            Response<String> response = curl("http://" + WEBAPP_NAME_1 + "." + "azurewebsites.net/appservicemsi/");
            Assertions.assertEquals(200, response.getStatusCode());
            String body = response.getValue();
            Assertions.assertNotNull(body);
            Assertions.assertTrue(body.contains(webApp.resourceGroupName()));
            Assertions.assertTrue(body.contains(webApp.id()));
        }
    }

    boolean setContainsValue(Set<String> stringSet, String value) {
        boolean found = false;
        for (String setContent : stringSet) {
            if (setContent.contains(value)) {
                found = true;
                break;
            }
        }

        return found;
    }
}