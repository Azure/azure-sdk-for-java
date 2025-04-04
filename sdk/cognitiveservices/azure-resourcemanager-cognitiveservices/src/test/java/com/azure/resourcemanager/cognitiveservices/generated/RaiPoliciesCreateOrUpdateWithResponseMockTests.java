// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cognitiveservices.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager;
import com.azure.resourcemanager.cognitiveservices.models.ContentLevel;
import com.azure.resourcemanager.cognitiveservices.models.CustomBlocklistConfig;
import com.azure.resourcemanager.cognitiveservices.models.RaiPolicy;
import com.azure.resourcemanager.cognitiveservices.models.RaiPolicyContentFilter;
import com.azure.resourcemanager.cognitiveservices.models.RaiPolicyContentSource;
import com.azure.resourcemanager.cognitiveservices.models.RaiPolicyMode;
import com.azure.resourcemanager.cognitiveservices.models.RaiPolicyProperties;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class RaiPoliciesCreateOrUpdateWithResponseMockTests {
    @Test
    public void testCreateOrUpdateWithResponse() throws Exception {
        String responseStr
            = "{\"etag\":\"gl\",\"tags\":{\"ryc\":\"ahvmywhsbrc\"},\"properties\":{\"type\":\"UserManaged\",\"mode\":\"Asynchronous_filter\",\"basePolicyName\":\"aqaj\",\"contentFilters\":[{\"name\":\"zptdmkrrbh\",\"enabled\":false,\"severityThreshold\":\"Medium\",\"blocking\":false,\"source\":\"Completion\"},{\"name\":\"ybpmf\",\"enabled\":false,\"severityThreshold\":\"Low\",\"blocking\":true,\"source\":\"Prompt\"}],\"customBlocklists\":[{\"source\":\"Prompt\",\"blocklistName\":\"ifkdschlzvf\",\"blocking\":true},{\"source\":\"Prompt\",\"blocklistName\":\"jwg\",\"blocking\":true},{\"source\":\"Prompt\",\"blocklistName\":\"kgf\",\"blocking\":true}]},\"id\":\"ogmhmjpj\",\"name\":\"cdf\",\"type\":\"dqwty\"}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        CognitiveServicesManager manager = CognitiveServicesManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        RaiPolicy response = manager.raiPolicies()
            .define("ajsvk")
            .withExistingAccount("rexkxbhxvucn", "lgmnhjevdyzn")
            .withTags(mapOf("onqqlmgn", "qfhefkwabsol", "exhvuqbozoolz", "qxsjxte", "nx", "ocarkuzlbcnndt"))
            .withProperties(new RaiPolicyProperties().withMode(RaiPolicyMode.DEFERRED)
                .withBasePolicyName("hd")
                .withContentFilters(Arrays.asList(new RaiPolicyContentFilter().withName("ckze")
                    .withEnabled(false)
                    .withSeverityThreshold(ContentLevel.LOW)
                    .withBlocking(true)
                    .withSource(RaiPolicyContentSource.COMPLETION)))
                .withCustomBlocklists(Arrays.asList(
                    new CustomBlocklistConfig().withBlocklistName("abjkdtfohfao")
                        .withBlocking(true)
                        .withSource(RaiPolicyContentSource.COMPLETION),
                    new CustomBlocklistConfig().withBlocklistName("rsiwdyjqur")
                        .withBlocking(true)
                        .withSource(RaiPolicyContentSource.PROMPT),
                    new CustomBlocklistConfig().withBlocklistName("ueekcsue")
                        .withBlocking(false)
                        .withSource(RaiPolicyContentSource.COMPLETION),
                    new CustomBlocklistConfig().withBlocklistName("cbcbgydlqidy")
                        .withBlocking(true)
                        .withSource(RaiPolicyContentSource.PROMPT))))
            .create();

        Assertions.assertEquals("ahvmywhsbrc", response.tags().get("ryc"));
        Assertions.assertEquals(RaiPolicyMode.ASYNCHRONOUS_FILTER, response.properties().mode());
        Assertions.assertEquals("aqaj", response.properties().basePolicyName());
        Assertions.assertEquals("zptdmkrrbh", response.properties().contentFilters().get(0).name());
        Assertions.assertEquals(false, response.properties().contentFilters().get(0).enabled());
        Assertions.assertEquals(ContentLevel.MEDIUM, response.properties().contentFilters().get(0).severityThreshold());
        Assertions.assertEquals(false, response.properties().contentFilters().get(0).blocking());
        Assertions.assertEquals(RaiPolicyContentSource.COMPLETION,
            response.properties().contentFilters().get(0).source());
        Assertions.assertEquals("ifkdschlzvf", response.properties().customBlocklists().get(0).blocklistName());
        Assertions.assertEquals(true, response.properties().customBlocklists().get(0).blocking());
        Assertions.assertEquals(RaiPolicyContentSource.PROMPT,
            response.properties().customBlocklists().get(0).source());
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
