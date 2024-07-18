// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.deviceupdate;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.PollerFlux;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.azure.iot.deviceupdate.TestUtils.getCredential;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DeviceUpdateClientTests extends TestProxyTestBase {
    private DeviceUpdateAsyncClient createClient() {
        DeviceUpdateClientBuilder clientBuilder = new DeviceUpdateClientBuilder()
            .endpoint(TestData.ACCOUNT_ENDPOINT)
            .instanceId(TestData.INSTANCE_ID)
            .httpClient(HttpClient.createDefault())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else if (interceptorManager.isRecordMode()) {
            clientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return clientBuilder.credential(getCredential(getTestMode())).buildAsyncClient();
    }

    @Test
    public void testGetProviders() {
        DeviceUpdateAsyncClient client = createClient();
        PagedFlux<BinaryData> response = client.listProviders(null);

        assertNotNull(response);
        assertTrue(response.toStream().count() > 0);
    }

    @Test
    public void testGetNames() {
        DeviceUpdateAsyncClient client = createClient();
        PagedFlux<BinaryData> response = client.listNames(TestData.PROVIDER, null);

        assertNotNull(response);
        assertTrue(response.toStream().count() > 0);
    }

    @Test
    public void testGetNamesNotFound() {
        DeviceUpdateAsyncClient client = createClient();

        try {
            PagedFlux<BinaryData> response = client.listNames("foo", null);
            long count = response.toStream().count();
            fail("NotFound response expected");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetVersions() {
        DeviceUpdateAsyncClient client = createClient();
        PagedFlux<BinaryData> response = client.listVersions(TestData.PROVIDER, TestData.NAME, null);

        assertNotNull(response);
        assertTrue(response.toStream().count() > 0);
    }

    @Test
    public void testGetVersionsNotFound() {
        DeviceUpdateAsyncClient client = createClient();

        try {
            PagedFlux<BinaryData> response = client.listVersions("foo", "bar", null);
            long count = response.toStream().count();
            fail("NotFound response expected");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetUpdate() {
        DeviceUpdateAsyncClient client = createClient();
        Response<BinaryData> response = client.getUpdateWithResponse(TestData.PROVIDER, TestData.NAME, TestData.VERSION, null).block();
        assertNotNull(response.getValue());
    }

    @Test
    public void testGetUpdateNotFound() {
        DeviceUpdateAsyncClient client = createClient();
        try {
            client.getUpdateWithResponse("foo", "bar", "1.2", null).block();
            fail("NotFound response expected");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetFiles() {
        DeviceUpdateAsyncClient client = createClient();
        PagedFlux<BinaryData> response = client.listFiles(TestData.PROVIDER, TestData.NAME, TestData.VERSION, null);

        assertNotNull(response);
        assertTrue(response.toStream().count() > 0);
    }

    @Test
    public void testGetFilesNotFound() {
        DeviceUpdateAsyncClient client = createClient();

        try {
            PagedFlux<BinaryData> response = client.listFiles("foo", "bar", "1.2", null);
            long count = response.toStream().count();
            fail("NotFound response expected");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Disabled("session records not ready")
    @Test
    public void testImportUpdates() {
        DeviceUpdateAsyncClient client = createClient();

        PollerFlux<BinaryData, BinaryData> response = client.beginImportUpdate(BinaryData.fromString("{\"test\":\"test\"}"), null);
        BinaryData binaryData = response.last().block().getFinalResult().block();
    }
}
