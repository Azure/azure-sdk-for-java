// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.netapp.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.netapp.NetAppFilesManager;
import com.azure.resourcemanager.netapp.models.SubvolumeInfo;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class SubvolumesCreateMockTests {
    @Test
    public void testCreate() throws Exception {
        String responseStr
            = "{\"properties\":{\"path\":\"rtws\",\"size\":1479956662341449642,\"parentPath\":\"icphvtrrmhw\",\"provisioningState\":\"Succeeded\"},\"id\":\"glrocuy\",\"name\":\"lw\",\"type\":\"hmem\"}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        NetAppFilesManager manager = NetAppFilesManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        SubvolumeInfo response = manager.subvolumes()
            .define("xmycjimryvwgcw")
            .withExistingVolume("fbsfarfsiow", "kjxnqpvwgfstmhq", "kizmdksaoafclu", "v")
            .withPath("mzgwe")
            .withSize(8389782043357868314L)
            .withParentPath("wefohecbvo")
            .create();

        Assertions.assertEquals("rtws", response.path());
        Assertions.assertEquals(1479956662341449642L, response.size());
        Assertions.assertEquals("icphvtrrmhw", response.parentPath());
    }
}
