// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.servicefabricmanagedclusters.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ApplicationResource;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.FailureAction;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.ManagedIdentityType;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.RollingUpgradeMode;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class ApplicationsGetWithResponseMockTests {
    @Test
    public void testGetWithResponse() throws Exception {
        String responseStr
            = "{\"properties\":{\"managedIdentities\":[{\"name\":\"iuxxpshneekulfg\",\"principalId\":\"lqubkwdlen\"},{\"name\":\"d\",\"principalId\":\"utujba\"},{\"name\":\"pjuohminyfl\",\"principalId\":\"orwmduvwpklv\"}],\"provisioningState\":\"mygdxpgpqch\",\"version\":\"zepn\",\"parameters\":{\"axconfozauo\":\"crxgibb\",\"nuuepzlrp\":\"sukokwbqplhl\",\"nnrwrbiork\":\"wzsoldweyuqdunv\",\"xmsivfomiloxggdu\":\"alywjhhgdn\"},\"upgradePolicy\":{\"applicationHealthPolicy\":{\"considerWarningAsError\":true,\"maxPercentUnhealthyDeployedApplications\":133601107,\"defaultServiceTypeHealthPolicy\":{\"maxPercentUnhealthyServices\":1357028156,\"maxPercentUnhealthyPartitionsPerService\":1264679400,\"maxPercentUnhealthyReplicasPerPartition\":569162481},\"serviceTypeHealthPolicyMap\":{\"hvcyyysfg\":{\"maxPercentUnhealthyServices\":1581273229,\"maxPercentUnhealthyPartitionsPerService\":1778919230,\"maxPercentUnhealthyReplicasPerPartition\":886290907},\"ubiipuipwoqonma\":{\"maxPercentUnhealthyServices\":1377717439,\"maxPercentUnhealthyPartitionsPerService\":2137619748,\"maxPercentUnhealthyReplicasPerPartition\":355548039},\"nizshqvcim\":{\"maxPercentUnhealthyServices\":219234320,\"maxPercentUnhealthyPartitionsPerService\":914015459,\"maxPercentUnhealthyReplicasPerPartition\":167629289}}},\"forceRestart\":true,\"rollingUpgradeMonitoringPolicy\":{\"failureAction\":\"Rollback\",\"healthCheckWaitDuration\":\"mblrrilbywd\",\"healthCheckStableDuration\":\"smiccwrwfscj\",\"healthCheckRetryTimeout\":\"n\",\"upgradeTimeout\":\"nszqujiz\",\"upgradeDomainTimeout\":\"voqyt\"},\"instanceCloseDelayDuration\":1821183361408913568,\"upgradeMode\":\"UnmonitoredAuto\",\"upgradeReplicaSetCheckTimeout\":2398197739518116547,\"recreateApplication\":false}},\"tags\":{\"hjoxo\":\"tp\"},\"identity\":{\"principalId\":\"sks\",\"tenantId\":\"iml\",\"type\":\"UserAssigned\",\"userAssignedIdentities\":{\"fgfb\":{\"principalId\":\"cgxxlxs\",\"clientId\":\"gcvizqzdwlvwlyou\"},\"g\":{\"principalId\":\"ubdyhgk\",\"clientId\":\"in\"},\"mmqtgqqqxhr\":{\"principalId\":\"zfttsttktlahb\",\"clientId\":\"ctxtgzukxi\"},\"azivjlfrqttbajl\":{\"principalId\":\"rxcpjuisavo\",\"clientId\":\"dzf\"}}},\"location\":\"tnwxy\",\"id\":\"pidkqqfkuvscxkdm\",\"name\":\"igovi\",\"type\":\"rxkpmloazuruoc\"}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        ServiceFabricManagedClustersManager manager = ServiceFabricManagedClustersManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureCloud.AZURE_PUBLIC_CLOUD));

        ApplicationResource response = manager.applications()
            .getWithResponse("jvewzcjznmwcp", "guaadraufactkahz", "v", com.azure.core.util.Context.NONE)
            .getValue();

        Assertions.assertEquals("tp", response.tags().get("hjoxo"));
        Assertions.assertEquals(ManagedIdentityType.USER_ASSIGNED, response.identity().type());
        Assertions.assertEquals("tnwxy", response.location());
        Assertions.assertEquals("iuxxpshneekulfg", response.managedIdentities().get(0).name());
        Assertions.assertEquals("lqubkwdlen", response.managedIdentities().get(0).principalId());
        Assertions.assertEquals("zepn", response.version());
        Assertions.assertEquals("crxgibb", response.parameters().get("axconfozauo"));
        Assertions.assertTrue(response.upgradePolicy().applicationHealthPolicy().considerWarningAsError());
        Assertions.assertEquals(133601107,
            response.upgradePolicy().applicationHealthPolicy().maxPercentUnhealthyDeployedApplications());
        Assertions.assertEquals(1357028156,
            response.upgradePolicy()
                .applicationHealthPolicy()
                .defaultServiceTypeHealthPolicy()
                .maxPercentUnhealthyServices());
        Assertions.assertEquals(1264679400,
            response.upgradePolicy()
                .applicationHealthPolicy()
                .defaultServiceTypeHealthPolicy()
                .maxPercentUnhealthyPartitionsPerService());
        Assertions.assertEquals(569162481,
            response.upgradePolicy()
                .applicationHealthPolicy()
                .defaultServiceTypeHealthPolicy()
                .maxPercentUnhealthyReplicasPerPartition());
        Assertions.assertEquals(1581273229,
            response.upgradePolicy()
                .applicationHealthPolicy()
                .serviceTypeHealthPolicyMap()
                .get("hvcyyysfg")
                .maxPercentUnhealthyServices());
        Assertions.assertEquals(1778919230,
            response.upgradePolicy()
                .applicationHealthPolicy()
                .serviceTypeHealthPolicyMap()
                .get("hvcyyysfg")
                .maxPercentUnhealthyPartitionsPerService());
        Assertions.assertEquals(886290907,
            response.upgradePolicy()
                .applicationHealthPolicy()
                .serviceTypeHealthPolicyMap()
                .get("hvcyyysfg")
                .maxPercentUnhealthyReplicasPerPartition());
        Assertions.assertTrue(response.upgradePolicy().forceRestart());
        Assertions.assertEquals(FailureAction.ROLLBACK,
            response.upgradePolicy().rollingUpgradeMonitoringPolicy().failureAction());
        Assertions.assertEquals("mblrrilbywd",
            response.upgradePolicy().rollingUpgradeMonitoringPolicy().healthCheckWaitDuration());
        Assertions.assertEquals("smiccwrwfscj",
            response.upgradePolicy().rollingUpgradeMonitoringPolicy().healthCheckStableDuration());
        Assertions.assertEquals("n",
            response.upgradePolicy().rollingUpgradeMonitoringPolicy().healthCheckRetryTimeout());
        Assertions.assertEquals("nszqujiz", response.upgradePolicy().rollingUpgradeMonitoringPolicy().upgradeTimeout());
        Assertions.assertEquals("voqyt",
            response.upgradePolicy().rollingUpgradeMonitoringPolicy().upgradeDomainTimeout());
        Assertions.assertEquals(1821183361408913568L, response.upgradePolicy().instanceCloseDelayDuration());
        Assertions.assertEquals(RollingUpgradeMode.UNMONITORED_AUTO, response.upgradePolicy().upgradeMode());
        Assertions.assertEquals(2398197739518116547L, response.upgradePolicy().upgradeReplicaSetCheckTimeout());
        Assertions.assertFalse(response.upgradePolicy().recreateApplication());
    }
}
