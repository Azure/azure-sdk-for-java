// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.devopsinfrastructure.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.devopsinfrastructure.DevOpsInfrastructureManager;
import com.azure.resourcemanager.devopsinfrastructure.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.devopsinfrastructure.models.Pool;
import com.azure.resourcemanager.devopsinfrastructure.models.ProvisioningState;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class PoolsGetByResourceGroupWithResponseMockTests {
    @Test
    public void testGetByResourceGroupWithResponse() throws Exception {
        String responseStr
            = "{\"properties\":{\"provisioningState\":\"Succeeded\",\"maximumConcurrency\":1324529031,\"organizationProfile\":{\"kind\":\"OrganizationProfile\"},\"agentProfile\":{\"kind\":\"AgentProfile\",\"resourcePredictions\":{},\"resourcePredictionsProfile\":{\"kind\":\"ResourcePredictionsProfile\"}},\"fabricProfile\":{\"kind\":\"FabricProfile\"},\"devCenterProjectResourceId\":\"koymkcd\"},\"identity\":{\"principalId\":\"pkkpw\",\"tenantId\":\"eqnovvqfovl\",\"type\":\"SystemAssigned\",\"userAssignedIdentities\":{\"q\":{\"principalId\":\"uwsyrsndsytgadg\",\"clientId\":\"aeaeneqnzarrw\"},\"wwiftohqkvpuv\":{\"principalId\":\"ijfqkacewiipfp\",\"clientId\":\"ji\"}}},\"location\":\"gplsaknynf\",\"tags\":{\"iyntorzihle\":\"ljphuopxodl\"},\"id\":\"sjswsrms\",\"name\":\"yzrpzbchckqqzq\",\"type\":\"ox\"}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        DevOpsInfrastructureManager manager = DevOpsInfrastructureManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        Pool response = manager.pools()
            .getByResourceGroupWithResponse("uqgbdbutauvfbt", "uwhhmhykojoxafn", com.azure.core.util.Context.NONE)
            .getValue();

        Assertions.assertEquals("gplsaknynf", response.location());
        Assertions.assertEquals("ljphuopxodl", response.tags().get("iyntorzihle"));
        Assertions.assertEquals(ProvisioningState.SUCCEEDED, response.properties().provisioningState());
        Assertions.assertEquals(1324529031, response.properties().maximumConcurrency());
        Assertions.assertEquals("koymkcd", response.properties().devCenterProjectResourceId());
        Assertions.assertEquals(ManagedServiceIdentityType.SYSTEM_ASSIGNED, response.identity().type());
    }
}
