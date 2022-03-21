// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroup;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.CredentialResult;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.List;

public class KubernetesAadTests extends ResourceManagerTestBase {

    private AzureResourceManager azureResourceManager;
    private String rgName;

    private final Region region = Region.US_WEST3;

    @Override
    protected HttpPipeline buildHttpPipeline(
        TokenCredential credential,
        AzureProfile profile,
        HttpLogOptions httpLogOptions,
        List< HttpPipelinePolicy > policies,
        HttpClient httpClient) {
        return HttpPipelineProvider.buildHttpPipeline(
            credential,
            profile,
            null,
            httpLogOptions,
            null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS),
            policies,
            httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        ResourceManagerUtils.InternalRuntimeContext internalContext = new ResourceManagerUtils.InternalRuntimeContext();
        internalContext.setIdentifierFunction(name -> new TestIdentifierProvider(testResourceNamer));
        azureResourceManager = buildManager(AzureResourceManager.class, httpPipeline, profile);
        setInternalContext(internalContext, azureResourceManager);

        rgName = generateRandomResourceName("rg", 8);
    }

    @Override
    protected void cleanUpResources() {
        try {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        } catch (Exception e) {
        }
    }

    @Test
    public void testKubernetesClusterAadIntegration() {
        String clientId = this.clientIdFromFile();

        final String groupName = generateRandomResourceName("group", 16);

        final String aksName = generateRandomResourceName("aks", 15);
        final String dnsPrefix = generateRandomResourceName("dns", 10);
        final String agentPoolName = generateRandomResourceName("ap0", 10);

        ActiveDirectoryGroup group = null;
        try {
            ServicePrincipal servicePrincipal = azureResourceManager.accessManagement().servicePrincipals()
                .getByName(clientId);

            // Azure AD integration with AAD group
            group = azureResourceManager.accessManagement().activeDirectoryGroups()
                .define(groupName)
                .withEmailAlias(groupName)
                .withMember(servicePrincipal)
                .create();

            // create
            KubernetesCluster kubernetesCluster = azureResourceManager
                .kubernetesClusters()
                .define(aksName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withDefaultVersion()
                .withSystemAssignedManagedServiceIdentity()
                .withAzureActiveDirectoryGroup(group.id())
                .disableLocalAccounts()
                .defineAgentPool(agentPoolName)
                    .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V3)
                    .withAgentPoolVirtualMachineCount(1)
                    .withAgentPoolMode(AgentPoolMode.SYSTEM)
                    .withOSDiskSizeInGB(30)
                    .attach()
                .withDnsPrefix("mp1" + dnsPrefix)
                .create();

            Assertions.assertEquals(1, kubernetesCluster.azureActiveDirectoryGroupIds().size());
            Assertions.assertEquals(group.id(), kubernetesCluster.azureActiveDirectoryGroupIds().get(0));
            Assertions.assertFalse(kubernetesCluster.isLocalAccountsEnabled());
            Assertions.assertFalse(kubernetesCluster.isAzureRbacEnabled());

            List<CredentialResult> credentialResults = kubernetesCluster.userKubeConfigs();
            Assertions.assertFalse(credentialResults.isEmpty());

            kubernetesCluster.update()
                .enableLocalAccounts()
                .apply();

            Assertions.assertTrue(kubernetesCluster.isLocalAccountsEnabled());

            azureResourceManager.kubernetesClusters().deleteById(kubernetesCluster.id());


            // create and then update
            kubernetesCluster = azureResourceManager
                .kubernetesClusters()
                .define(aksName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withDefaultVersion()
                .withSystemAssignedManagedServiceIdentity()
                .defineAgentPool(agentPoolName)
                    .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V3)
                    .withAgentPoolVirtualMachineCount(1)
                    .withAgentPoolMode(AgentPoolMode.SYSTEM)
                    .withOSDiskSizeInGB(30)
                    .attach()
                .withDnsPrefix("mp1" + dnsPrefix)
                .create();

            Assertions.assertEquals(0, kubernetesCluster.azureActiveDirectoryGroupIds().size());
            Assertions.assertTrue(kubernetesCluster.isLocalAccountsEnabled());

            kubernetesCluster.update()
                .disableLocalAccounts()
                .apply();

            Assertions.assertEquals(0, kubernetesCluster.azureActiveDirectoryGroupIds().size());
            Assertions.assertFalse(kubernetesCluster.isLocalAccountsEnabled());

            // failed to get credential due to lack of permission
            KubernetesCluster kubernetesCluster1 = kubernetesCluster;
            Assertions.assertThrows(ManagementException.class, () -> {
                List<CredentialResult> credentialResults1 = kubernetesCluster1.userKubeConfigs();
            });

            kubernetesCluster.update()
                .withAzureActiveDirectoryGroup(group.id())
                .apply();

            Assertions.assertEquals(1, kubernetesCluster.azureActiveDirectoryGroupIds().size());
            Assertions.assertEquals(group.id(), kubernetesCluster.azureActiveDirectoryGroupIds().get(0));
            Assertions.assertFalse(kubernetesCluster.isLocalAccountsEnabled());

            // admin group
            credentialResults = kubernetesCluster.userKubeConfigs();
            Assertions.assertFalse(credentialResults.isEmpty());
        } finally {
            if (group != null) {
                azureResourceManager.accessManagement().activeDirectoryGroups().deleteById(group.id());
            }
        }
    }
}
