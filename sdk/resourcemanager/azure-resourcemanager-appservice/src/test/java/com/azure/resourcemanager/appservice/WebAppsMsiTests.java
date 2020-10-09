// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.resourcemanager.appservice.models.AppServicePlan;
import com.azure.resourcemanager.appservice.models.JavaVersion;
import com.azure.resourcemanager.appservice.models.PricingTier;
import com.azure.resourcemanager.appservice.models.RemoteVisualStudioVersion;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.appservice.models.WebContainer;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WebAppsMsiTests extends AppServiceTest {
    private String rgName1 = "";
    private String webappName1 = "";
    private String vaultName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        webappName1 = generateRandomResourceName("java-webapp-", 20);
        rgName1 = generateRandomResourceName("javacsmrg", 20);
        vaultName = generateRandomResourceName("java-vault-", 20);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName1);
        try {
            resourceManager.resourceGroups().beginDeleteByName(rgName);
        } catch (Exception e) {
            // fine, RG_NAME is not created
        }
    }

    @Test
    public void canCRUDWebAppWithMsi() throws Exception {
        // Create with new app service plan
        WebApp webApp =
            appServiceManager
                .webApps()
                .define(webappName1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName1)
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
            uploadFileToWebApp(
                webApp.getPublishingProfile(),
                "appservicemsi.war",
                WebAppsMsiTests.class.getResourceAsStream("/appservicemsi.war"));

            ResourceManagerUtils.sleep(Duration.ofSeconds(30));

            Response<String> response = curl("http://" + webappName1 + "." + "azurewebsites.net/appservicemsi/");
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
        Creatable<ResourceGroup> creatableRG =
            resourceManager.resourceGroups().define(rgName).withRegion(Region.US_WEST);

        // Create an "User Assigned (External) MSI" residing in the above RG and assign reader access to the virtual
        // network
        //
        final Identity createdIdentity =
            msiManager
                .identities()
                .define(identityName1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(creatableRG)
                .withAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR)
                .create();

        // Prepare a definition for yet-to-be-created "User Assigned (External) MSI" with contributor access to the
        // resource group
        // it resides
        //
        Creatable<Identity> creatableIdentity =
            msiManager
                .identities()
                .define(identityName2)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(creatableRG)
                .withAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR);

        // Create with new app service plan
        WebApp webApp =
            appServiceManager
                .webApps()
                .define(webappName1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName1)
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
            uploadFileToWebApp(
                webApp.getPublishingProfile(),
                "appservicemsi.war",
                WebAppsMsiTests.class.getResourceAsStream("/appservicemsi.war"));

            ResourceManagerUtils.sleep(Duration.ofSeconds(30));

            Response<String> response = curl("http://" + webappName1 + "." + "azurewebsites.net/appservicemsi/");
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
