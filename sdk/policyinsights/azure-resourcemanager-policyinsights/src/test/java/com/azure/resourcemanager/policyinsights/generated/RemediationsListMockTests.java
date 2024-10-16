// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.policyinsights.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.policyinsights.PolicyInsightsManager;
import com.azure.resourcemanager.policyinsights.models.Remediation;
import com.azure.resourcemanager.policyinsights.models.ResourceDiscoveryMode;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class RemediationsListMockTests {
    @Test
    public void testList() throws Exception {
        String responseStr
            = "{\"value\":[{\"properties\":{\"policyAssignmentId\":\"zxmdew\",\"policyDefinitionReferenceId\":\"sxkrpl\",\"resourceDiscoveryMode\":\"ExistingNonCompliant\",\"provisioningState\":\"ejwwviyoyps\",\"createdOn\":\"2021-03-21T06:31:37Z\",\"lastUpdatedOn\":\"2021-02-25T20:04:53Z\",\"filters\":{\"locations\":[\"xs\",\"wjh\",\"kbiwetpozyc\",\"qiqyhgfsetzlexbs\"]},\"deploymentStatus\":{\"totalDeployments\":805982929,\"successfulDeployments\":269107120,\"failedDeployments\":1279554044},\"statusMessage\":\"ziu\",\"correlationId\":\"bzkkd\",\"resourceCount\":564052271,\"parallelDeployments\":1974407220,\"failureThreshold\":{\"percentage\":32.192505}},\"id\":\"selpkpbaf\",\"name\":\"afhlbyl\",\"type\":\"cbevxrhyzdfw\"}]}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        PolicyInsightsManager manager = PolicyInsightsManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        PagedIterable<Remediation> response
            = manager.remediations().list(1495529407, "wfbcyaykmmfzsbf", com.azure.core.util.Context.NONE);

        Assertions.assertEquals("zxmdew", response.iterator().next().policyAssignmentId());
        Assertions.assertEquals("sxkrpl", response.iterator().next().policyDefinitionReferenceId());
        Assertions.assertEquals(ResourceDiscoveryMode.EXISTING_NON_COMPLIANT,
            response.iterator().next().resourceDiscoveryMode());
        Assertions.assertEquals("xs", response.iterator().next().filters().locations().get(0));
        Assertions.assertEquals(564052271, response.iterator().next().resourceCount());
        Assertions.assertEquals(1974407220, response.iterator().next().parallelDeployments());
        Assertions.assertEquals(32.192505F, response.iterator().next().failureThreshold().percentage());
    }
}
