// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.iot.deviceupdate;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.ContentType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManagementClientTests extends TestBase {

    private ManagementAsyncClient createClient() {
        DeviceUpdateClientBuilder builder =
            new DeviceUpdateClientBuilder()
                .endpoint(TestData.ACCOUNT_ENDPOINT)
                .instanceId(TestData.INSTANCE_ID)
                .httpClient(HttpClient.createDefault())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.httpClient(interceptorManager.getPlaybackClient())
                .credential(request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)));
        } else if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build());
        } else if (getTestMode() == TestMode.LIVE) {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        return builder.buildManagementAsyncClient();
    }

    @Test
    public void testGetAllDeployments() {
        ManagementAsyncClient client = createClient();
        PagedFlux<BinaryData> response = client.listDeploymentsForGroup("Uncategorized", null);

        assertNotNull(response);
        assertEquals(0, response.toStream().count());
    }

    @Test
    public void testListDeviceTags() {
        ManagementAsyncClient client = createClient();
        PagedFlux<BinaryData> response = client.listDeviceTags(null);

        assertNotNull(response);
        assertEquals(1, response.toStream().count());
    }

    @Test
    public void testListDevices() {
        ManagementAsyncClient client = createClient();
        PagedFlux<BinaryData> response = client.listDevices(null);

        assertNotNull(response);
        assertEquals(3, response.toStream().count());
    }

    @Test
    public void testGetAllDeviceClasses() {
        ManagementAsyncClient client = createClient();
        PagedFlux<BinaryData> response = client.listDeviceClasses(null);

        assertNotNull(response);
        assertEquals(1, response.toStream().count());
    }

}
