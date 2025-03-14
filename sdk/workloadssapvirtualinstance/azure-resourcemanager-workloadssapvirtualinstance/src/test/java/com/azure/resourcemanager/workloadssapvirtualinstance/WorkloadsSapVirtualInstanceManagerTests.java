// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.workloadssapvirtualinstance;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.msi.MsiManager;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class WorkloadsSapVirtualInstanceManagerTests extends TestProxyTestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST2;
    private String resourceGroupName = "rg" + randomPadding();
    private WorkloadsSapVirtualInstanceManager workloadsSapVirtualInstanceManager;
    private NetworkManager networkManager;
    private MsiManager msiManager;
    private AuthorizationManager authorizationManager;
    private ResourceManager resourceManager;
    private ResourceGroup resourceGroup;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        resourceManager = ResourceManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        authorizationManager = AuthorizationManager.configure()
            .withPolicy(new ProviderRegistrationPolicy(resourceManager))
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        msiManager = MsiManager.configure()
            .withPolicy(new ProviderRegistrationPolicy(resourceManager))
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        networkManager = NetworkManager.configure()
            .withPolicy(new ProviderRegistrationPolicy(resourceManager))
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        workloadsSapVirtualInstanceManager = WorkloadsSapVirtualInstanceManager.configure()
            .withPolicy(new ProviderRegistrationPolicy(resourceManager))
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        // use AZURE_RESOURCE_GROUP_NAME if run in LIVE CI
        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroupName = testResourceGroup;
        } else {
            resourceManager.resourceGroups().define(resourceGroupName).withRegion(REGION).create();
        }
    }

    @Test
    @LiveOnly
    public void test() {
        String vnetName = "vnet" + randomPadding();
        String sNetName = "sub" + randomPadding();
        String msiName = "msi" + randomPadding();

        resourceGroup = resourceManager.resourceGroups().getByName(resourceGroupName);

        Identity identity = msiManager.identities()
            .define(msiName)
            .withRegion(REGION)
            .withExistingResourceGroup(resourceGroupName)
            .create();

        authorizationManager.roleAssignments()
            .define("assign" + randomPadding())
            .forObjectId(identity.principalId())
            .withBuiltInRole(BuiltInRole.fromString("Azure Center for SAP solutions service role"))
            .withResourceScope(resourceGroup)
            .create();

        networkManager.networks()
            .define(vnetName)
            .withRegion(REGION)
            .withExistingResourceGroup(resourceGroupName)
            .withAddressSpace("10.0.0.0/16")
            .defineSubnet(sNetName)
            .withAddressPrefix("10.0.0.0/24")
            .disableNetworkPoliciesOnPrivateEndpoint()
            .attach()
            .create();
    }

    @Override
    protected void afterTest() {
        if (!testEnv) {
            resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
