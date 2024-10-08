// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.machinelearning.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.machinelearning.MachineLearningManager;
import com.azure.resourcemanager.machinelearning.models.EnvironmentContainer;
import com.azure.resourcemanager.machinelearning.models.ListViewType;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class RegistryEnvironmentContainersListMockTests {
    @Test
    public void testList() throws Exception {
        String responseStr
            = "{\"value\":[{\"properties\":{\"provisioningState\":\"Canceled\",\"isArchived\":false,\"latestVersion\":\"tt\",\"nextVersion\":\"pbilnszyjbuw\",\"description\":\"usydscizvkayiox\",\"tags\":{\"l\":\"qs\"},\"properties\":{\"ym\":\"feombodvdgfuakqs\",\"vzceuy\":\"f\"}},\"id\":\"ktck\",\"name\":\"nbpkfnxrlncmlzvv\",\"type\":\"mesfhqs\"}]}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        MachineLearningManager manager = MachineLearningManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        PagedIterable<EnvironmentContainer> response = manager.registryEnvironmentContainers()
            .list("ovriqtuzwbkqcgz", "gtdjhtbarptxurs", "oyyumhzps", ListViewType.ACTIVE_ONLY,
                com.azure.core.util.Context.NONE);

        Assertions.assertEquals("usydscizvkayiox", response.iterator().next().properties().description());
        Assertions.assertEquals("qs", response.iterator().next().properties().tags().get("l"));
        Assertions.assertEquals("feombodvdgfuakqs", response.iterator().next().properties().properties().get("ym"));
        Assertions.assertEquals(false, response.iterator().next().properties().isArchived());
    }
}
