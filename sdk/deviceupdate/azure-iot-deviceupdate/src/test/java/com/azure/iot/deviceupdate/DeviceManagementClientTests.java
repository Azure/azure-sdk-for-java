// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.iot.deviceupdate;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DeviceManagementClientTests extends TestBase {

    private DeviceManagementAsyncClient createClient() {
        DeviceManagementClientBuilder builder =
            new DeviceManagementClientBuilder()
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
        return builder.buildAsyncClient();
    }

    @Test
    public void testGetAllDeployments() {
        DeviceManagementAsyncClient client = createClient();
        PagedFlux<BinaryData> response = client.listDeploymentsForGroup("Uncategorized", null);

        assertNotNull(response);
        assertEquals(0, response.toStream().count());
    }

    @Test
    public void testListDeviceTags() {
        DeviceManagementAsyncClient client = createClient();
        PagedFlux<BinaryData> response = client.listDeviceTags(null);

        assertNotNull(response);
        assertEquals(1, response.toStream().count());
    }

    @Test
    public void testListDevices() {
        DeviceManagementAsyncClient client = createClient();
        PagedFlux<BinaryData> response = client.listDevices(null);

        assertNotNull(response);
        assertEquals(3, response.toStream().count());
    }

    @Test
    public void testGetAllDeviceClasses() {
        DeviceManagementAsyncClient client = createClient();
        PagedFlux<BinaryData> response = client.listDeviceClasses(null);

        assertNotNull(response);
        assertEquals(1, response.toStream().count());
    }

}
