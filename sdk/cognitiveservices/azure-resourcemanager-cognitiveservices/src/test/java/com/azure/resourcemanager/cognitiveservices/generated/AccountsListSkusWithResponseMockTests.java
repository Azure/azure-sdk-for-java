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
import com.azure.resourcemanager.cognitiveservices.models.AccountSkuListResult;
import com.azure.resourcemanager.cognitiveservices.models.SkuTier;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class AccountsListSkusWithResponseMockTests {
    @Test
    public void testListSkusWithResponse() throws Exception {
        String responseStr
            = "{\"value\":[{\"resourceType\":\"lgg\",\"sku\":{\"name\":\"bemzqkzszuwi\",\"tier\":\"Free\",\"size\":\"xxhljfpgpic\",\"family\":\"nzhrgmqgjsxvpqcb\",\"capacity\":1162167874}}]}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        CognitiveServicesManager manager = CognitiveServicesManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        AccountSkuListResult response = manager.accounts()
            .listSkusWithResponse("kfvxcnq", "xqpswok", com.azure.core.util.Context.NONE)
            .getValue();

        Assertions.assertEquals("lgg", response.value().get(0).resourceType());
        Assertions.assertEquals("bemzqkzszuwi", response.value().get(0).sku().name());
        Assertions.assertEquals(SkuTier.FREE, response.value().get(0).sku().tier());
        Assertions.assertEquals("xxhljfpgpic", response.value().get(0).sku().size());
        Assertions.assertEquals("nzhrgmqgjsxvpqcb", response.value().get(0).sku().family());
        Assertions.assertEquals(1162167874, response.value().get(0).sku().capacity());
    }
}
