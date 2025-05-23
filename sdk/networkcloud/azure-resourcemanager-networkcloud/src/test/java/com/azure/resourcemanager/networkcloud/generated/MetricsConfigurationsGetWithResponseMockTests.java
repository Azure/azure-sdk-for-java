// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.networkcloud.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.networkcloud.NetworkCloudManager;
import com.azure.resourcemanager.networkcloud.models.ClusterMetricsConfiguration;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class MetricsConfigurationsGetWithResponseMockTests {
    @Test
    public void testGetWithResponse() throws Exception {
        String responseStr
            = "{\"extendedLocation\":{\"name\":\"f\",\"type\":\"zsifcuvbdujgcwx\"},\"properties\":{\"collectionInterval\":1219254206667481816,\"detailedStatus\":\"Applied\",\"detailedStatusMessage\":\"jtrdxr\",\"disabledMetrics\":[\"gbbgiarksykp\",\"dqxwabzrwiqrxhac\"],\"enabledMetrics\":[\"osqkptjqgk\",\"fmmainwhedxkpbq\",\"unt\"],\"provisioningState\":\"Accepted\"},\"location\":\"z\",\"tags\":{\"laxp\":\"elwgvydjufbnkl\",\"lfdxaglz\":\"gjwdab\",\"siflikyypzkgxf\":\"ytlbtlqhopxouvm\",\"qsdb\":\"fmy\"},\"id\":\"oksz\",\"name\":\"nm\",\"type\":\"wgpterdiuw\"}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        NetworkCloudManager manager = NetworkCloudManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureCloud.AZURE_PUBLIC_CLOUD));

        ClusterMetricsConfiguration response = manager.metricsConfigurations()
            .getWithResponse("lhxd", "bkl", "iichgjsysmvxodgw", com.azure.core.util.Context.NONE)
            .getValue();

        Assertions.assertEquals("z", response.location());
        Assertions.assertEquals("elwgvydjufbnkl", response.tags().get("laxp"));
        Assertions.assertEquals("f", response.extendedLocation().name());
        Assertions.assertEquals("zsifcuvbdujgcwx", response.extendedLocation().type());
        Assertions.assertEquals(1219254206667481816L, response.collectionInterval());
        Assertions.assertEquals("osqkptjqgk", response.enabledMetrics().get(0));
    }
}
