// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.elasticsan.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.elasticsan.ElasticSanManager;
import com.azure.resourcemanager.elasticsan.models.Volume;
import com.azure.resourcemanager.elasticsan.models.VolumeCreateOption;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class ResourceProvidersRestoreVolumeMockTests {
    @Test
    public void testRestoreVolume() throws Exception {
        String responseStr
            = "{\"properties\":{\"volumeId\":\"t\",\"creationData\":{\"createSource\":\"DiskRestorePoint\",\"sourceId\":\"rcgp\"},\"sizeGiB\":4566842388697384284,\"storageTarget\":{\"targetIqn\":\"mejzanlfzxia\",\"targetPortalHostname\":\"mbzonokix\",\"targetPortalPort\":115638191,\"provisioningState\":\"Invalid\",\"status\":\"Unhealthy\"},\"managedBy\":{\"resourceId\":\"frl\"},\"provisioningState\":\"Succeeded\"},\"id\":\"rnwoiindfp\",\"name\":\"pj\",\"type\":\"lwbtlhf\"}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        ElasticSanManager manager = ElasticSanManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureCloud.AZURE_PUBLIC_CLOUD));

        Volume response = manager.resourceProviders()
            .restoreVolume("slthaq", "x", "smwutwbdsrezpd", "hneuyowqkd", com.azure.core.util.Context.NONE);

        Assertions.assertEquals(VolumeCreateOption.DISK_RESTORE_POINT, response.creationData().createSource());
        Assertions.assertEquals("rcgp", response.creationData().sourceId());
        Assertions.assertEquals(4566842388697384284L, response.sizeGiB());
        Assertions.assertEquals("frl", response.managedBy().resourceId());
    }
}
