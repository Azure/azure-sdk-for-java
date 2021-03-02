// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.iot.deviceupdate;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.*;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.iot.deviceupdate.models.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DevicesClientTests extends TestBase {
    private static final String DEFAULT_SCOPE = "6ee392c4-d339-4083-b04d-6b7947c6cf78/.default";

    private Devices createClient() {
        TokenCredential credentials;
        HttpClient httpClient;
        HttpPipelinePolicy recordingPolicy = null;

        if (getTestMode() != TestMode.PLAYBACK) {
            // Record & Live
            credentials = new ClientSecretCredentialBuilder()
                .tenantId(TestData.TENANT_ID)
                .clientId(TestData.CLIENT_ID)
                .clientSecret(System.getenv("AZURE_CLIENT_SECRET"))
                .build();
            httpClient = HttpClient.createDefault();
            if (getTestMode() == TestMode.RECORD) {
                recordingPolicy = interceptorManager.getRecordPolicy();
            }
        }
        else {
            // Playback
            credentials = new DefaultAzureCredentialBuilder().build();
            httpClient = interceptorManager.getPlaybackClient();
        }

        BearerTokenAuthenticationPolicy bearerTokenAuthenticationPolicy = new BearerTokenAuthenticationPolicy(credentials, DEFAULT_SCOPE);

        HttpHeaders headers = new HttpHeaders().put("Accept", ContentType.APPLICATION_JSON);
        AddHeadersPolicy addHeadersPolicy = new AddHeadersPolicy(headers);

        HttpPipeline httpPipeline;
        if (getTestMode() == TestMode.RECORD) {
            // Record & Live
            httpPipeline = new HttpPipelineBuilder()
                .httpClient(httpClient)
                .policies(bearerTokenAuthenticationPolicy, addHeadersPolicy, recordingPolicy)
                .build();
        }
        else {
            // Playback
            httpPipeline = new HttpPipelineBuilder()
                .httpClient(httpClient)
                .policies(bearerTokenAuthenticationPolicy, addHeadersPolicy)
                .build();
        }

        DeviceUpdateClient client = new DeviceUpdateClientBuilder()
            .accountEndpoint(TestData.ACCOUNT_ENDPOINT)
            .instanceId(TestData.INSTANCE_ID)
            .pipeline(httpPipeline)
            .buildClient();

        return client.getDevices();
    }

    @Test
    public void testGetAllDeviceClasses() {
        Devices client = createClient();
        PagedFlux<DeviceClass> response = client.getAllDeviceClassesAsync();

        assertNotNull(response);
        List<DeviceClass> deviceClasses = new ArrayList<>();
        response.byPage().map(page -> deviceClasses.addAll(page.getValue())).blockLast();
        assertTrue(deviceClasses.size() > 0);
    }

    @Test
    public void testGetDeviceClass() {
        Devices client = createClient();
        DeviceClass deviceClass = client.getDeviceClassAsync(TestData.DEVICE_CLASS_ID)
            .block();
        assertNotNull(deviceClass);
        assertEquals(TestData.PROVIDER, deviceClass.getManufacturer());
        assertEquals(TestData.NAME, deviceClass.getModel());
    }

    @Test
    public void testDeviceClassNotFound() {
        Devices client = createClient();
        try {
            client.getDeviceClassWithResponseAsync("foo")
                .block();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetAllDeviceClassIds() {
        Devices client = createClient();
        PagedFlux<String> response = client.getDeviceClassDeviceIdsAsync(TestData.DEVICE_CLASS_ID);

        assertNotNull(response);
        List<String> deviceIds = new ArrayList<>();
        response.byPage().map(page -> deviceIds.addAll(page.getValue())).blockLast();
        assertTrue(deviceIds.size() > 0);
    }

    @Test
    public void testGetAllDeviceClassIdsNotFound() {
        Devices client = createClient();
        try {
            PagedFlux<String> response = client.getDeviceClassDeviceIdsAsync("foo");
            List<String> deviceIds = new ArrayList<>();
            response.byPage().map(page -> deviceIds.addAll(page.getValue())).blockLast();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetAllDeviceClassInstallableUpdates() {
        Devices client = createClient();
        PagedFlux<UpdateId> response = client.getDeviceClassInstallableUpdatesAsync(TestData.DEVICE_CLASS_ID);

        assertNotNull(response);
        List<UpdateId> updateIds = new ArrayList<>();
        response.byPage().map(page -> updateIds.addAll(page.getValue())).blockLast();
        assertTrue(updateIds.size() > 0);
        boolean found = false;
        for (UpdateId updateId : updateIds) {
            if (updateId.getProvider().equals(TestData.PROVIDER) &&
                updateId.getName().equals(TestData.NAME) &&
                updateId.getVersion().equals(TestData.VERSION)) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testGetAllDeviceClassInstallableUpdatesNotFound() {
        Devices client = createClient();
        try {
            PagedFlux<UpdateId> response = client.getDeviceClassInstallableUpdatesAsync("foo");
            List<UpdateId> updateIds = new ArrayList<>();
            response.byPage().map(page -> updateIds.addAll(page.getValue())).blockLast();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetAllDevices() {
        Devices client = createClient();
        PagedFlux<Device> response = client.getAllDevicesAsync(null);

        assertNotNull(response);
        List<Device> devices = new ArrayList<>();
        response.byPage().map(page -> devices.addAll(page.getValue())).blockLast();
        assertTrue(devices.size() > 0);
    }

    @Test
    public void testGetDevice() {
        Devices client = createClient();
        Device device = client.getDeviceAsync(TestData.DEVICE_ID)
            .block();

        assertNotNull(device);
        assertEquals(TestData.PROVIDER, device.getManufacturer());
        assertEquals(TestData.NAME, device.getModel());
    }

    @Test
    public void testGetDeviceNotFound() {
        Devices client = createClient();
        try {
            client.getDeviceAsync("foo")
                .block();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetUpdateCompliance() {
        Devices client = createClient();
        UpdateCompliance updateCompliance = client.getUpdateComplianceAsync()
            .block();
        assertNotNull(updateCompliance);
        assertTrue(updateCompliance.getTotalDeviceCount() > 0);
    }

    @Test
    public void testGetAllDeviceTags() {
        Devices client = createClient();
        PagedFlux<DeviceTag> response = client.getAllDeviceTagsAsync();

        assertNotNull(response);
        List<DeviceTag> deviceTags = new ArrayList<>();
        response.byPage().map(page -> deviceTags.addAll(page.getValue())).blockLast();
        assertTrue(deviceTags.size() > 0);
    }

    @Test
    public void testGetDeviceTag() {
        Devices client = createClient();
        String tag_name = "functionaltests-groupname1";
        DeviceTag deviceTag = client.getDeviceTagAsync(tag_name)
            .block();
        assertNotNull(deviceTag);
        assertEquals(tag_name, deviceTag.getTagName());
    }

    @Test
    public void testGetDeviceTagNotFound() {
        Devices client = createClient();
        try {
            client.getDeviceAsync("foo")
                .block();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetAllGroups() {
        Devices client = createClient();
        PagedFlux<Group> response = client.getAllGroupsAsync();

        assertNotNull(response);
        List<Group> groups = new ArrayList<>();
        response.byPage().map(page -> groups.addAll(page.getValue())).blockLast();
        assertTrue(groups.size() > 0);
    }

    @Test
    public void testGetGroup() {
        Devices client = createClient();
        String group_id = "Uncategorized";
        Group group = client.getGroupAsync(group_id)
            .block();
        assertNotNull(group);
        assertEquals(group_id, group.getGroupId());
    }

    @Test
    public void testGetGroupNotFound() {
        Devices client = createClient();
        try {
            client.getGroupAsync("foo")
                .block();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetGroupUpdateCompliance() {
        Devices client = createClient();
        String group_id = "Uncategorized";
        UpdateCompliance compliance = client.getGroupUpdateComplianceAsync(group_id)
            .block();
        assertNotNull(compliance);
        assertTrue(compliance.getTotalDeviceCount() > 0);
    }

    @Test
    public void testGetGroupUpdateComplianceNotFound() {
        Devices client = createClient();
        try {
            client.getGroupUpdateComplianceAsync("foo")
                .block();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetGroupBestUpdates() {
        Devices client = createClient();
        String group_id = TestData.DEVICE_ID;
        PagedFlux<UpdatableDevices> response = client.getGroupBestUpdatesAsync(group_id, null);

        assertNotNull(response);
        List<UpdatableDevices> updates = new ArrayList<>();
        response.byPage().map(page -> updates.addAll(page.getValue())).blockLast();
        assertTrue(updates.size() > 0);
    }

    @Test
    public void testGetGroupBestUpdatesNotFound() {
        Devices client = createClient();
        try {
            PagedFlux<UpdatableDevices> response = client.getGroupBestUpdatesAsync("foo", null);
            List<UpdatableDevices> updates = new ArrayList<>();
            response.byPage().map(page -> updates.addAll(page.getValue())).blockLast();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }
}
