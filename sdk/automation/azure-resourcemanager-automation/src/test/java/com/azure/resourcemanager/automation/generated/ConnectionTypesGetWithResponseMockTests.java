// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.automation.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.automation.AutomationManager;
import com.azure.resourcemanager.automation.models.ConnectionType;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class ConnectionTypesGetWithResponseMockTests {
    @Test
    public void testGetWithResponse() throws Exception {
        String responseStr
            = "{\"id\":\"molpcqyde\",\"name\":\"vskiczd\",\"type\":\"jei\",\"properties\":{\"isGlobal\":false,\"fieldDefinitions\":{\"mdvewuyqaeohpjh\":{\"isEncrypted\":true,\"isOptional\":false,\"type\":\"xbmsgycqsx\"},\"xaex\":{\"isEncrypted\":true,\"isOptional\":true,\"type\":\"vhhdaurgho\"},\"ektm\":{\"isEncrypted\":true,\"isOptional\":false,\"type\":\"xjoezlqxrkdknko\"}},\"creationTime\":\"2021-06-25T18:13:44Z\",\"lastModifiedTime\":\"2021-03-27T07:21:58Z\",\"description\":\"zamicb\"}}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        AutomationManager manager = AutomationManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        ConnectionType response = manager.connectionTypes()
            .getWithResponse("lusnawmhhgzotfr", "yrgkoekv", "wxxyxhighctx", com.azure.core.util.Context.NONE)
            .getValue();

        Assertions.assertEquals(false, response.isGlobal());
        Assertions.assertEquals(OffsetDateTime.parse("2021-03-27T07:21:58Z"), response.lastModifiedTime());
        Assertions.assertEquals("zamicb", response.description());
    }
}
