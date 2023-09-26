// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.kubernetesconfiguration;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.containerservice.ContainerServiceManager;
import com.azure.resourcemanager.containerservice.models.*;
import com.azure.resourcemanager.kubernetesconfiguration.fluent.models.ExtensionInner;
import com.azure.resourcemanager.kubernetesconfiguration.models.*;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class SourceControlConfigurationManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST;
    private String resourceGroupName = "rg" + randomPadding();
    private SourceControlConfigurationManager sourceControlConfigurationManager;
    private ContainerServiceManager containerServiceManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        sourceControlConfigurationManager = SourceControlConfigurationManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        containerServiceManager = ContainerServiceManager
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
    @DoNotRecord(skipInPlayback = true)
    public void testCreateKubernetesExtension() {
        Extension extension = null;
        String randomPadding = randomPadding();
        String aksName = "aks" + randomPadding;
        String extensionName = "extension" + randomPadding;
        try {
            // @embedStart
            containerServiceManager
                .kubernetesClusters()
                .define(aksName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withDefaultVersion()
                .withSystemAssignedManagedServiceIdentity()
                .defineAgentPool("agentpool")
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D8S_V3)
                .withAgentPoolVirtualMachineCount(2)
                .withOSType(OSType.LINUX)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .withAvailabilityZones(1, 2, 3)
                .withMaxPodsCount(110)
                .withOSDiskType(OSDiskType.EPHEMERAL)
                .withOSDiskSizeInGB(128)
                .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                .withKubeletDiskType(KubeletDiskType.OS)
                .attach()
                .defineAgentPool("userpool")
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D8S_V3)
                .withAgentPoolVirtualMachineCount(2)
                .withOSType(OSType.LINUX)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .withAvailabilityZones(1, 2, 3)
                .withMaxPodsCount(110)
                .withOSDiskType(OSDiskType.EPHEMERAL)
                .withOSDiskSizeInGB(128)
                .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                .withKubeletDiskType(KubeletDiskType.OS)
                .attach()
                .withDnsPrefix(aksName + "-dns")
                .create();

            extension = sourceControlConfigurationManager
                .extensions()
                .create(resourceGroupName,
                    "Microsoft.ContainerService",
                    "managedClusters",
                    aksName,
                    extensionName,
                    new ExtensionInner()
                        .withExtensionType("TraefikLabs.TraefikProxy")
                        .withAutoUpgradeMinorVersion(true)
                        .withReleaseTrain("stable")
                        .withPlan(new Plan().withName("traefik-proxy").withProduct("traefik-proxy").withPublisher("containous"))
                        .withScope(new Scope().withCluster(new ScopeCluster().withReleaseNamespace("traefik")))
                );
            // @embedEnd
            Assertions.assertEquals(extension.name(), extensionName);
            Assertions.assertTrue(sourceControlConfigurationManager.extensions()
                .list(resourceGroupName, "Microsoft.ContainerService",
                "managedClusters", aksName).stream().findAny().isPresent());

        } finally {
            if (extension != null) {
                sourceControlConfigurationManager.extensions().delete(
                    resourceGroupName,
                    "Microsoft.ContainerService",
                    "managedClusters",
                    aksName,
                    extensionName
                );
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
