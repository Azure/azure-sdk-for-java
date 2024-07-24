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
import org.junit.jupiter.api.Test;

import static com.azure.iot.deviceupdate.TestUtils.getCredential;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DeviceManagementClientTests extends TestProxyTestBase {
    private DeviceManagementAsyncClient createClient() {
        DeviceManagementClientBuilder clientBuilder = new DeviceManagementClientBuilder()
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
    public void testListDevices() {
        DeviceManagementAsyncClient client = createClient();
        PagedFlux<BinaryData> response = client.listDevices(null);

        assertNotNull(response);
        assertTrue(response.toStream().count() > 0);
    }

    @Test
    public void testGetDeviceNotFound() {
        DeviceManagementAsyncClient client = createClient();
        try {
            client.getDeviceWithResponse("foo", null).block();
            fail("NotFound response expected");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetGroups() {
        DeviceManagementAsyncClient client = createClient();
        PagedFlux<BinaryData> response = client.listGroups(null);

        assertNotNull(response);
        assertTrue(response.toStream().count() > 0);
    }

    @Test
    public void testGetGroup() {
        DeviceManagementAsyncClient client = createClient();
        Response<BinaryData> response = client.getGroupWithResponse(TestData.DEVICE_GROUP, null).block();
        assertNotNull(response.getValue());
    }

    @Test
    public void testGetGroupNotFound() {
        DeviceManagementAsyncClient client = createClient();
        try {
            client.getGroupWithResponse("foo", null).block();
            fail("NotFound response expected");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetDeviceClasses() {
        DeviceManagementAsyncClient client = createClient();
        PagedFlux<BinaryData> response = client.listDeviceClasses(null);

        assertNotNull(response);
        assertTrue(response.toStream().count() > 0);
    }

    @Test
    public void testGetDeviceClassesNotFound() {
        DeviceManagementAsyncClient client = createClient();
        try {
            client.getDeviceClassWithResponse("foo", null).block();
            fail("NotFound response expected");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetBestUpdatesForGroup() {
        DeviceManagementAsyncClient client = createClient();
        PagedFlux<BinaryData> response = client.listBestUpdatesForGroup(TestData.DEVICE_GROUP, null);

        assertNotNull(response);
        assertTrue(response.toStream().count() > 0);
    }

    @Test
    public void testGetBestUpdatesForGroupNotFound() {
        DeviceManagementAsyncClient client = createClient();

        try {
            PagedFlux<BinaryData> response = client.listBestUpdatesForGroup("foo", null);
            long count = response.toStream().count();
            fail("NotFound response expected");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    // Temporary disabled because the service doesn't properly handle this method yet
    // @Test
    public void testGetDeploymentsForGroup() {
        DeviceManagementAsyncClient client = createClient();
        PagedFlux<BinaryData> response = client.listDeploymentsForGroup(TestData.DEVICE_GROUP, null);

        assertNotNull(response);
        assertTrue(response.toStream().count() > 0);
    }

    @Test
    public void testGetDeploymentsForGroupNotFound() {
        DeviceManagementAsyncClient client = createClient();

        try {
            PagedFlux<BinaryData> response = client.listDeploymentsForGroup("foo", null);
            long count = response.toStream().count();
            fail("NotFound response expected");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }
}
