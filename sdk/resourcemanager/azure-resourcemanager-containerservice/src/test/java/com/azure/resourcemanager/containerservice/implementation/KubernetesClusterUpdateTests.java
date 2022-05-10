// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerservice.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Context;
import com.azure.resourcemanager.containerservice.ContainerServiceManagementTest;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.AgentPoolType;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.containerservice.models.ManagedClusterServicePrincipalProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;

public class KubernetesClusterUpdateTests extends ContainerServiceManagementTest {

    private static class ValidationPipeline implements HttpPipelinePolicy {

        private static final String HEADER_NAME = "validation-header";
        private int countOfPut = 0;

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext, HttpPipelineNextPolicy httpPipelineNextPolicy) {
            HttpRequest request = httpPipelineCallContext.getHttpRequest();
            if (request.getHttpMethod() == HttpMethod.PUT && request.getHeaders().get(HEADER_NAME) != null) {
                ++countOfPut;
            }
            return httpPipelineNextPolicy.process();
        }

        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_CALL;
        }
    }

    private final ValidationPipeline validationPipeline = new ValidationPipeline();

    @Override
    protected HttpPipeline buildHttpPipeline(
        TokenCredential credential,
        AzureProfile profile,
        HttpLogOptions httpLogOptions,
        List<HttpPipelinePolicy> policies,
        HttpClient httpClient) {

        policies.add(validationPipeline);
        return super.buildHttpPipeline(credential, profile, httpLogOptions, policies, httpClient);
    }

    @Test
    public void testKubernetesClusterUpdate() {
        String aksName = generateRandomResourceName("aks", 15);
        String dnsPrefix = generateRandomResourceName("dns", 10);
        String agentPoolName = generateRandomResourceName("ap0", 10);
        String agentPoolName1 = generateRandomResourceName("ap1", 10);

        KubernetesCluster kubernetesCluster = containerServiceManager.kubernetesClusters().define(aksName)
            .withRegion(Region.US_CENTRAL)
            .withExistingResourceGroup(rgName)
            .withDefaultVersion()
            .withSystemAssignedManagedServiceIdentity()
            .defineAgentPool(agentPoolName)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_D2_V2)
                .withAgentPoolVirtualMachineCount(1)
                .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .withAutoScaling(1, 3)
                .attach()
            .withDnsPrefix("mp1" + dnsPrefix)
            .create();
        Assertions.assertTrue(kubernetesCluster.agentPools().get(agentPoolName).isAutoScalingEnabled());

        KubernetesCluster.Update clusterUpdate = kubernetesCluster.update();
        Assertions.assertFalse(isClusterModifiedDuringUpdate(kubernetesCluster));

        clusterUpdate = clusterUpdate
            .defineAgentPool(agentPoolName1)
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_A2_V2)
                .withAgentPoolVirtualMachineCount(1)
                .attach();
        Assertions.assertFalse(isClusterModifiedDuringUpdate(kubernetesCluster));
        Assertions.assertEquals(2, kubernetesCluster.innerModel().agentPoolProfiles().size());

        clusterUpdate = clusterUpdate
            .updateAgentPool(agentPoolName)
                .withoutAutoScaling()
                .parent();
        Assertions.assertTrue(isClusterModifiedDuringUpdate(kubernetesCluster));

        clusterUpdate = clusterUpdate
            .updateAgentPool(agentPoolName)
                .withAutoScaling(1, 3)
                .parent();
        Assertions.assertFalse(isClusterModifiedDuringUpdate(kubernetesCluster));
        Assertions.assertEquals(2, kubernetesCluster.innerModel().agentPoolProfiles().size());

        // this should not send PUT to ManagedClustersClient
        kubernetesCluster = clusterUpdate.apply(
            new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY,
                new HttpHeaders().set(ValidationPipeline.HEADER_NAME, "createOrUpdate")));
        Assertions.assertEquals(2, kubernetesCluster.agentPools().size());
        Assertions.assertEquals(1, validationPipeline.countOfPut);

        clusterUpdate = clusterUpdate
            .withoutAgentPool(agentPoolName1);
        Assertions.assertFalse(isClusterModifiedDuringUpdate(kubernetesCluster));
        Assertions.assertEquals(1, kubernetesCluster.innerModel().agentPoolProfiles().size());

        clusterUpdate = clusterUpdate
            .updateAgentPool(agentPoolName)
                .withoutAutoScaling()
                .parent();
        Assertions.assertTrue(isClusterModifiedDuringUpdate(kubernetesCluster));

        kubernetesCluster = clusterUpdate.apply();
        Assertions.assertEquals(1, kubernetesCluster.agentPools().size());
        Assertions.assertFalse(kubernetesCluster.agentPools().get(agentPoolName).isAutoScalingEnabled());
    }

    private boolean isClusterModifiedDuringUpdate(KubernetesCluster cluster) {
        KubernetesClusterImpl clusterImpl = (KubernetesClusterImpl) cluster;
        ManagedClusterServicePrincipalProfile servicePrincipalProfile = cluster.innerModel().servicePrincipalProfile();
        try {
            cluster.innerModel().withServicePrincipalProfile(null); // servicePrincipalProfile is null in update
            return clusterImpl.isClusterModifiedDuringUpdate(clusterImpl.innerModel());
        } finally {
            cluster.innerModel().withServicePrincipalProfile(servicePrincipalProfile);
        }
    }
}
