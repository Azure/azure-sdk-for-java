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
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.AgentPoolType;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.KubeletDiskType;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.containerservice.models.ManagedClusterAddonProfile;
import com.azure.resourcemanager.containerservice.models.OSDiskType;
import com.azure.resourcemanager.containerservice.models.OSType;
import com.azure.resourcemanager.kubernetesconfiguration.fluent.models.ExtensionInner;
import com.azure.resourcemanager.kubernetesconfiguration.models.Extension;
import com.azure.resourcemanager.kubernetesconfiguration.models.Plan;
import com.azure.resourcemanager.kubernetesconfiguration.models.Scope;
import com.azure.resourcemanager.kubernetesconfiguration.models.ScopeCluster;
import com.azure.resourcemanager.loganalytics.LogAnalyticsManager;
import com.azure.resourcemanager.loganalytics.models.Workspace;
import com.azure.resourcemanager.loganalytics.models.WorkspaceSku;
import com.azure.resourcemanager.loganalytics.models.WorkspaceSkuNameEnum;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SourceControlConfigurationManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST;
    private String resourceGroupName = "rg" + randomPadding();
    private SourceControlConfigurationManager sourceControlConfigurationManager;
    private ContainerServiceManager containerServiceManager;
    private LogAnalyticsManager logAnalyticsManager;
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

        logAnalyticsManager = LogAnalyticsManager
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
        Workspace logWorkspace = null;
        KubernetesCluster kubernetesCluster = null;
        Extension extension = null;
        String randomPadding = randomPadding();
        String workspaceName = "workspace" + randomPadding;
        String aksName = "aks" + randomPadding;
        String extensionName = "extension" + randomPadding;
        try {
            // @embedStart
            logWorkspace =  logAnalyticsManager.workspaces().define(workspaceName)
                .withRegion(REGION).withExistingResourceGroup(resourceGroupName)
                .withSku(new WorkspaceSku().withName(WorkspaceSkuNameEnum.PER_GB2018))
                .create();
            Map<String, ManagedClusterAddonProfile> addOnProfilesMap = new HashMap<>();
            addOnProfilesMap.put("azureKeyvaultSecretsProvider", new ManagedClusterAddonProfile().withEnabled(false));
            addOnProfilesMap.put("azurepolicy", new ManagedClusterAddonProfile().withEnabled(true));
            addOnProfilesMap.put("omsAgent", new ManagedClusterAddonProfile().withEnabled(true)
                .withConfig(Collections.singletonMap("logAnalyticsWorkspaceResourceID", logWorkspace.id())));
            kubernetesCluster = containerServiceManager
                .kubernetesClusters()
                .define(aksName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withDefaultVersion()
                .withSystemAssignedManagedServiceIdentity()
                .defineAgentPool("agentpool")
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D8S_V3)
                .withAgentPoolVirtualMachineCount(2)
                .withOSDiskSizeInGB(128)
                .withOSDiskType(OSDiskType.EPHEMERAL)
                .withKubeletDiskType(KubeletDiskType.OS)
                .withMaxPodsCount(110)
                .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                .withAvailabilityZones(1, 2,  3)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .withOSType(OSType.LINUX)
                .attach()
                .defineAgentPool("userpool")
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D8S_V3)
                .withAgentPoolVirtualMachineCount(2)
                .withOSDiskSizeInGB(128)
                .withOSDiskType(OSDiskType.EPHEMERAL)
                .withKubeletDiskType(KubeletDiskType.OS)
                .withMaxPodsCount(110)
                .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                .withAvailabilityZones(1, 2,  3)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .withOSType(OSType.LINUX)
                .attach()
                .withDnsPrefix(aksName + "-dns")
                .withAddOnProfiles(addOnProfilesMap)
                .enableAzureRbac()
                .create()
                .refresh();

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
            if (kubernetesCluster != null) {
                containerServiceManager.kubernetesClusters().deleteById(kubernetesCluster.id());
                if (resourceManager.resourceGroups().contain("MC_" + resourceGroupName + "_" + aksName + "_" + REGION.name())) {
                    resourceManager.resourceGroups().beginDeleteByName("MC_" + resourceGroupName + "_" + aksName + "_" + REGION.name());
                }
            }
            if (logWorkspace != null) {
                logAnalyticsManager.workspaces().deleteById(logWorkspace.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
