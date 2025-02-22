// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cognitiveservices.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager;
import com.azure.resourcemanager.cognitiveservices.models.CommitmentPlan;
import com.azure.resourcemanager.cognitiveservices.models.HostingModel;
import com.azure.resourcemanager.cognitiveservices.models.SkuTier;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class CommitmentPlansListPlansBySubscriptionMockTests {
    @Test
    public void testListPlansBySubscription() throws Exception {
        String responseStr
            = "{\"value\":[{\"etag\":\"dihoyn\",\"kind\":\"xwetwkdrcyrucpc\",\"sku\":{\"name\":\"nuzdqumo\",\"tier\":\"Basic\",\"size\":\"naie\",\"family\":\"qh\",\"capacity\":1421414952},\"tags\":{\"foanniyopetx\":\"elqkaadlkn\",\"nucaephblkwqpat\":\"vcnrly\",\"uzqymtuowog\":\"bqsdtcjbctvi\"},\"location\":\"it\",\"properties\":{\"provisioningState\":\"Accepted\",\"commitmentPlanGuid\":\"b\",\"hostingModel\":\"Web\",\"planType\":\"banf\",\"current\":{\"tier\":\"scxmxeat\",\"count\":932659658,\"quota\":{\"quantity\":2580805120871115843,\"unit\":\"jyibqbna\"},\"startDate\":\"hjrmkuhmaxl\",\"endDate\":\"lfihcj\"},\"autoRenew\":false,\"next\":{\"tier\":\"nc\",\"count\":152401377,\"quota\":{\"quantity\":2348526307879631032,\"unit\":\"a\"},\"startDate\":\"gvaknokzwjjzrl\",\"endDate\":\"xldzyyfytpqsix\"},\"last\":{\"tier\":\"puj\",\"count\":1424381442,\"quota\":{\"quantity\":4999753329886050204,\"unit\":\"vsmb\"},\"startDate\":\"lzoy\",\"endDate\":\"wzdbpqvybefgv\"},\"provisioningIssues\":[\"okcvtlubses\",\"vcuartrhun\"]},\"id\":\"pirykycndzfqiv\",\"name\":\"reuykbbmnwagl\",\"type\":\"bxoeeonql\"}]}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        CognitiveServicesManager manager = CognitiveServicesManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        PagedIterable<CommitmentPlan> response
            = manager.commitmentPlans().listPlansBySubscription(com.azure.core.util.Context.NONE);

        Assertions.assertEquals("xwetwkdrcyrucpc", response.iterator().next().kind());
        Assertions.assertEquals("nuzdqumo", response.iterator().next().sku().name());
        Assertions.assertEquals(SkuTier.BASIC, response.iterator().next().sku().tier());
        Assertions.assertEquals("naie", response.iterator().next().sku().size());
        Assertions.assertEquals("qh", response.iterator().next().sku().family());
        Assertions.assertEquals(1421414952, response.iterator().next().sku().capacity());
        Assertions.assertEquals("elqkaadlkn", response.iterator().next().tags().get("foanniyopetx"));
        Assertions.assertEquals("it", response.iterator().next().location());
        Assertions.assertEquals("b", response.iterator().next().properties().commitmentPlanGuid());
        Assertions.assertEquals(HostingModel.WEB, response.iterator().next().properties().hostingModel());
        Assertions.assertEquals("banf", response.iterator().next().properties().planType());
        Assertions.assertEquals("scxmxeat", response.iterator().next().properties().current().tier());
        Assertions.assertEquals(932659658, response.iterator().next().properties().current().count());
        Assertions.assertEquals(false, response.iterator().next().properties().autoRenew());
        Assertions.assertEquals("nc", response.iterator().next().properties().next().tier());
        Assertions.assertEquals(152401377, response.iterator().next().properties().next().count());
    }
}
