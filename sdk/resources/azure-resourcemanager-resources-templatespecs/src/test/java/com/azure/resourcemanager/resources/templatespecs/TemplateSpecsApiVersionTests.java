// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.templatespecs;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.core.test.http.MockHttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class TemplateSpecsApiVersionTests {
    @Test
    public void templateSpecsAndVersionsUseStableApiVersion() {
        List<String> requests = new ArrayList<>();
        HttpClient httpClient = request -> {
            requests.add(request.getUrl().toString());
            return Mono.just(new MockHttpResponse(request, 200, "{}".getBytes(StandardCharsets.UTF_8)));
        };
        TemplateSpecsManager manager = TemplateSpecsManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("test-token", OffsetDateTime.MAX)),
                new AzureProfile("", "subscription", AzureCloud.AZURE_PUBLIC_CLOUD));

        manager.templateSpecs().getByResourceGroup("group", "spec");
        manager.templateSpecVersions().get("group", "spec", "version");

        Assertions.assertEquals(2, requests.size());
        for (String request : requests) {
            Assertions.assertTrue(request.contains("api-version=2022-02-01"), request);
        }
    }
}
