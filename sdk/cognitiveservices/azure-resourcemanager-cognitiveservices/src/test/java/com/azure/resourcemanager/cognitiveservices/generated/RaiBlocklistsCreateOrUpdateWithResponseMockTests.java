// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cognitiveservices.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager;
import com.azure.resourcemanager.cognitiveservices.models.RaiBlocklist;
import com.azure.resourcemanager.cognitiveservices.models.RaiBlocklistProperties;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class RaiBlocklistsCreateOrUpdateWithResponseMockTests {
    @Test
    public void testCreateOrUpdateWithResponse() throws Exception {
        String responseStr
            = "{\"etag\":\"dqaolfylnkk\",\"tags\":{\"mfwo\":\"jvlywl\",\"zgczeu\":\"bjwhlwyjfnqzocr\",\"iekoif\":\"tgxdncaqt\"},\"properties\":{\"description\":\"yttzgixgyrih\"},\"id\":\"gm\",\"name\":\"behlqtxnr\",\"type\":\"lkndrndpgfjodh\"}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        CognitiveServicesManager manager = CognitiveServicesManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureCloud.AZURE_PUBLIC_CLOUD));

        RaiBlocklist response = manager.raiBlocklists()
            .define("uwj")
            .withExistingAccount("esq", "ggvrbnyrukoilaci")
            .withTags(mapOf("ruzythqkkwhbg", "p", "jdtvmclyymffhmj", "vellvulnxdmnitm", "jqrbrpvnm", "ddnyxfzuvrzmzqm"))
            .withProperties(new RaiBlocklistProperties().withDescription("oebojtjppg"))
            .create();

        Assertions.assertEquals("jvlywl", response.tags().get("mfwo"));
        Assertions.assertEquals("yttzgixgyrih", response.properties().description());
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
