// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.dashboard.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.dashboard.DashboardManager;
import com.azure.resourcemanager.dashboard.models.ManagedPrivateEndpointModel;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class ManagedPrivateEndpointsListMockTests {
    @Test
    public void testList() throws Exception {
        String responseStr
            = "{\"value\":[{\"properties\":{\"provisioningState\":\"Succeeded\",\"privateLinkResourceId\":\"nxxmueedndrdv\",\"privateLinkResourceRegion\":\"kwqqtchealmf\",\"groupIds\":[\"aayg\"],\"requestMessage\":\"wvgpiohg\",\"connectionState\":{\"status\":\"Approved\",\"description\":\"udxepxgyqagv\"},\"privateLinkServiceUrl\":\"mnpkukghimdblxg\",\"privateLinkServicePrivateIP\":\"mfnjh\"},\"location\":\"xw\",\"tags\":{\"yfkzik\":\"kkfoqr\"},\"id\":\"jawneaiv\",\"name\":\"wczelpci\",\"type\":\"elsfeaen\"}]}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        DashboardManager manager = DashboardManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureCloud.AZURE_PUBLIC_CLOUD));

        PagedIterable<ManagedPrivateEndpointModel> response
            = manager.managedPrivateEndpoints().list("uipiccjzk", "ivgvvcna", com.azure.core.util.Context.NONE);

        Assertions.assertEquals("xw", response.iterator().next().location());
        Assertions.assertEquals("kkfoqr", response.iterator().next().tags().get("yfkzik"));
        Assertions.assertEquals("nxxmueedndrdv", response.iterator().next().privateLinkResourceId());
        Assertions.assertEquals("kwqqtchealmf", response.iterator().next().privateLinkResourceRegion());
        Assertions.assertEquals("aayg", response.iterator().next().groupIds().get(0));
        Assertions.assertEquals("wvgpiohg", response.iterator().next().requestMessage());
        Assertions.assertEquals("mnpkukghimdblxg", response.iterator().next().privateLinkServiceUrl());
    }
}
