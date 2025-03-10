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
import com.azure.resourcemanager.automation.models.Certificate;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class CertificatesGetWithResponseMockTests {
    @Test
    public void testGetWithResponse() throws Exception {
        String responseStr
            = "{\"properties\":{\"thumbprint\":\"vjb\",\"expiryTime\":\"2021-05-11T18:13:33Z\",\"isExportable\":false,\"creationTime\":\"2021-03-08T05:22:54Z\",\"lastModifiedTime\":\"2021-06-16T00:31:48Z\",\"description\":\"uiyuosnu\"},\"id\":\"dtelvhyibdrqrs\",\"name\":\"hbuubpy\",\"type\":\"owtjo\"}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        AutomationManager manager = AutomationManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        Certificate response = manager.certificates()
            .getWithResponse("lyzgsnor", "jgmn", "jotvmrxkhl", com.azure.core.util.Context.NONE)
            .getValue();

        Assertions.assertEquals("uiyuosnu", response.description());
    }
}
