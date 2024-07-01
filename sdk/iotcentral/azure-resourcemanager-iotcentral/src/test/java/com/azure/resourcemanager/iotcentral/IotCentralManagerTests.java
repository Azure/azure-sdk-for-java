// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.iotcentral;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.resourcemanager.iotcentral.models.App;
import com.azure.resourcemanager.iotcentral.models.AppSku;
import com.azure.resourcemanager.iotcentral.models.AppSkuInfo;
import com.azure.resourcemanager.iotcentral.models.NetworkAction;
import com.azure.resourcemanager.iotcentral.models.NetworkRuleSets;
import com.azure.resourcemanager.iotcentral.models.PublicNetworkAccess;
import com.azure.resourcemanager.iotcentral.models.SystemAssignedServiceIdentity;
import com.azure.resourcemanager.iotcentral.models.SystemAssignedServiceIdentityType;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Random;

public class IotCentralManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST;
    private String resourceGroupName = "rg" + randomPadding();
    private IotCentralManager iotCentralManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        iotCentralManager = IotCentralManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        resourceManager = ResourceManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        // use AZURE_RESOURCE_GROUP_NAME if run in LIVE CI
        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroupName = testResourceGroup;
        } else {
            resourceManager.resourceGroups()
                .define(resourceGroupName)
                .withRegion(REGION)
                .create();
        }
    }

    @Override
    protected void afterTest() {
        if (!testEnv) {
            resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
        }
    }

    @Test
    @LiveOnly
    public void testIotCentralApp() {
        App app = null;
        try {
            String appName = "app" + randomPadding();
            // @embedmeStart
            app = iotCentralManager.apps()
                .define(appName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withSku(new AppSkuInfo().withName(AppSku.ST2))
                .withIdentity(new SystemAssignedServiceIdentity().withType(SystemAssignedServiceIdentityType.NONE))
                .withNetworkRuleSets(new NetworkRuleSets()
                    .withApplyToDevices(false)
                    .withApplyToIoTCentral(false)
                    .withDefaultAction(NetworkAction.ALLOW)
                    .withIpRules(Collections.emptyList()))
                .withTemplate("iotc-condition@1.0.0")
                .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
                .withDisplayName(appName)
                .withSubdomain(appName)
                .create();
            // @embedmeEnd
            app.refresh();
            Assertions.assertEquals(app.name(), appName);
            Assertions.assertEquals(app.name(), iotCentralManager.apps().getById(app.id()).name());
            Assertions.assertTrue(iotCentralManager.apps().list().stream().count() > 0);
        } finally {
            if (app != null) {
                iotCentralManager.apps().deleteById(app.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }

}
