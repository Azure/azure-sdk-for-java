// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.streamanalytics.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager;
import com.azure.resourcemanager.streamanalytics.models.Transformation;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class TransformationsCreateOrReplaceWithResponseMockTests {
    @Test
    public void testCreateOrReplaceWithResponse() throws Exception {
        String responseStr
            = "{\"properties\":{\"streamingUnits\":1920410421,\"validStreamingUnits\":[601869733],\"query\":\"xwlmdjr\",\"etag\":\"fgbvfvpdbo\"},\"name\":\"cizsjqlhkrribdei\",\"type\":\"ipqkghvxndzwm\",\"id\":\"efajpj\"}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        StreamAnalyticsManager manager = StreamAnalyticsManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        Transformation response = manager.transformations()
            .define("qvujzraehtwdwrf")
            .withExistingStreamingjob("vk", "r")
            .withName("khevxccedc")
            .withStreamingUnits(767551661)
            .withValidStreamingUnits(Arrays.asList(1010479001, 670463751, 403427600, 66676019))
            .withQuery("h")
            .withIfMatch("cxnavv")
            .withIfNoneMatch("xqi")
            .create();

        Assertions.assertEquals("efajpj", response.id());
        Assertions.assertEquals("cizsjqlhkrribdei", response.name());
        Assertions.assertEquals(1920410421, response.streamingUnits());
        Assertions.assertEquals(601869733, response.validStreamingUnits().get(0));
        Assertions.assertEquals("xwlmdjr", response.query());
    }
}
