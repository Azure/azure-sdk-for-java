// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.policyinsights.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Context;
import com.azure.resourcemanager.policyinsights.PolicyInsightsManager;
import com.azure.resourcemanager.policyinsights.models.PolicyEvent;
import com.azure.resourcemanager.policyinsights.models.PolicyEventsResourceType;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class PolicyEventsListQueryResultsForSubscriptionLevelPolicyAssignmentMockTests {
    @Test
    public void testListQueryResultsForSubscriptionLevelPolicyAssignment() throws Exception {
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        ArgumentCaptor<HttpRequest> httpRequest = ArgumentCaptor.forClass(HttpRequest.class);

        String responseStr =
            "{\"value\":[{\"@odata.id\":\"bpmzzn\",\"@odata.context\":\"ff\",\"timestamp\":\"2021-05-24T06:36:50Z\",\"resourceId\":\"tmhheioqa\",\"policyAssignmentId\":\"v\",\"policyDefinitionId\":\"ufuqyrx\",\"effectiveParameters\":\"lcgqlsismj\",\"isCompliant\":false,\"subscriptionId\":\"dgamquhiosrsj\",\"resourceType\":\"vfcdisyirn\",\"resourceLocation\":\"hcz\",\"resourceGroup\":\"rxzbujr\",\"resourceTags\":\"hqvwrevkhgnlnzon\",\"policyAssignmentName\":\"rpiqywncv\",\"policyAssignmentOwner\":\"szcofizeht\",\"policyAssignmentParameters\":\"gbjkvreljeamur\",\"policyAssignmentScope\":\"mlovuanashcxl\",\"policyDefinitionName\":\"jerbdkelvidizozs\",\"policyDefinitionAction\":\"ccxjm\",\"policyDefinitionCategory\":\"fdgnwncypuuwwlt\",\"policySetDefinitionId\":\"qjctzenkeif\",\"policySetDefinitionName\":\"hmkdasvfl\",\"policySetDefinitionOwner\":\"bxcudchx\",\"policySetDefinitionCategory\":\"rb\",\"policySetDefinitionParameters\":\"d\",\"managementGroupIds\":\"robwjlvizbfhf\",\"policyDefinitionReferenceId\":\"vacqpbtuodxesz\",\"complianceState\":\"belawumuaslzkwr\",\"tenantId\":\"oycqucwy\",\"principalOid\":\"hnomdrkywuh\",\"components\":[{\"id\":\"uurutlwexxwlalni\",\"type\":\"zsrzpgepq\",\"name\":\"bb\",\"timestamp\":\"2021-03-26T20:31:48Z\",\"tenantId\":\"dakchzyvl\",\"principalOid\":\"q\",\"policyDefinitionAction\":\"kcxk\",\"\":{}},{\"id\":\"xmysuxswqrntv\",\"type\":\"ijpstte\",\"name\":\"qqpwcyyuf\",\"timestamp\":\"2021-08-24T20:16:03Z\",\"tenantId\":\"nc\",\"principalOid\":\"mqspkcdqzhlctdd\",\"policyDefinitionAction\":\"qn\",\"\":{}}],\"\":{}}]}";

        Mockito.when(httpResponse.getStatusCode()).thenReturn(200);
        Mockito.when(httpResponse.getHeaders()).thenReturn(new HttpHeaders());
        Mockito
            .when(httpResponse.getBody())
            .thenReturn(Flux.just(ByteBuffer.wrap(responseStr.getBytes(StandardCharsets.UTF_8))));
        Mockito
            .when(httpResponse.getBodyAsByteArray())
            .thenReturn(Mono.just(responseStr.getBytes(StandardCharsets.UTF_8)));
        Mockito
            .when(httpClient.send(httpRequest.capture(), Mockito.any()))
            .thenReturn(
                Mono
                    .defer(
                        () -> {
                            Mockito.when(httpResponse.getRequest()).thenReturn(httpRequest.getValue());
                            return Mono.just(httpResponse);
                        }));

        PolicyInsightsManager manager =
            PolicyInsightsManager
                .configure()
                .withHttpClient(httpClient)
                .authenticate(
                    tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                    new AzureProfile("", "", AzureEnvironment.AZURE));

        PagedIterable<PolicyEvent> response =
            manager
                .policyEvents()
                .listQueryResultsForSubscriptionLevelPolicyAssignment(
                    PolicyEventsResourceType.DEFAULT,
                    "nmwmqtibx",
                    "ijddtvqc",
                    747956494,
                    "adijaeukmrsie",
                    "kpn",
                    OffsetDateTime.parse("2021-05-10T16:06:39Z"),
                    OffsetDateTime.parse("2021-02-13T23:25:17Z"),
                    "apm",
                    "dqmeqwigpibudq",
                    "yxeb",
                    Context.NONE);

        Assertions.assertEquals(OffsetDateTime.parse("2021-05-24T06:36:50Z"), response.iterator().next().timestamp());
        Assertions.assertEquals("tmhheioqa", response.iterator().next().resourceId());
        Assertions.assertEquals("v", response.iterator().next().policyAssignmentId());
        Assertions.assertEquals("ufuqyrx", response.iterator().next().policyDefinitionId());
        Assertions.assertEquals("lcgqlsismj", response.iterator().next().effectiveParameters());
        Assertions.assertEquals(false, response.iterator().next().isCompliant());
        Assertions.assertEquals("dgamquhiosrsj", response.iterator().next().subscriptionId());
        Assertions.assertEquals("vfcdisyirn", response.iterator().next().resourceType());
        Assertions.assertEquals("hcz", response.iterator().next().resourceLocation());
        Assertions.assertEquals("rxzbujr", response.iterator().next().resourceGroup());
        Assertions.assertEquals("hqvwrevkhgnlnzon", response.iterator().next().resourceTags());
        Assertions.assertEquals("rpiqywncv", response.iterator().next().policyAssignmentName());
        Assertions.assertEquals("szcofizeht", response.iterator().next().policyAssignmentOwner());
        Assertions.assertEquals("gbjkvreljeamur", response.iterator().next().policyAssignmentParameters());
        Assertions.assertEquals("mlovuanashcxl", response.iterator().next().policyAssignmentScope());
        Assertions.assertEquals("jerbdkelvidizozs", response.iterator().next().policyDefinitionName());
        Assertions.assertEquals("ccxjm", response.iterator().next().policyDefinitionAction());
        Assertions.assertEquals("fdgnwncypuuwwlt", response.iterator().next().policyDefinitionCategory());
        Assertions.assertEquals("qjctzenkeif", response.iterator().next().policySetDefinitionId());
        Assertions.assertEquals("hmkdasvfl", response.iterator().next().policySetDefinitionName());
        Assertions.assertEquals("bxcudchx", response.iterator().next().policySetDefinitionOwner());
        Assertions.assertEquals("rb", response.iterator().next().policySetDefinitionCategory());
        Assertions.assertEquals("d", response.iterator().next().policySetDefinitionParameters());
        Assertions.assertEquals("robwjlvizbfhf", response.iterator().next().managementGroupIds());
        Assertions.assertEquals("vacqpbtuodxesz", response.iterator().next().policyDefinitionReferenceId());
        Assertions.assertEquals("belawumuaslzkwr", response.iterator().next().complianceState());
        Assertions.assertEquals("oycqucwy", response.iterator().next().tenantId());
        Assertions.assertEquals("hnomdrkywuh", response.iterator().next().principalOid());
        Assertions.assertEquals("uurutlwexxwlalni", response.iterator().next().components().get(0).id());
        Assertions.assertEquals("zsrzpgepq", response.iterator().next().components().get(0).type());
        Assertions.assertEquals("bb", response.iterator().next().components().get(0).name());
        Assertions
            .assertEquals(
                OffsetDateTime.parse("2021-03-26T20:31:48Z"),
                response.iterator().next().components().get(0).timestamp());
        Assertions.assertEquals("dakchzyvl", response.iterator().next().components().get(0).tenantId());
        Assertions.assertEquals("q", response.iterator().next().components().get(0).principalOid());
        Assertions.assertEquals("kcxk", response.iterator().next().components().get(0).policyDefinitionAction());
    }
}
