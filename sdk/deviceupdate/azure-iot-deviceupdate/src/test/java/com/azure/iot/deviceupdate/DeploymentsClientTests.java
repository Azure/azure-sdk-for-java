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
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.iot.deviceupdate.models.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DeploymentsClientTests extends TestBase {
    private static final String DEFAULT_SCOPE = "6ee392c4-d339-4083-b04d-6b7947c6cf78/.default";

    private Deployments createClient() {
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

        return client.getDeployments();
    }

    @Test
    public void testGetAllDeployments() {
        Deployments client = createClient();
        PagedFlux<Deployment> response = client.getAllDeploymentsAsync(null);

        assertNotNull(response);
        List<Deployment> deployments = new ArrayList<>();
        response.byPage().map(page -> deployments.addAll(page.getValue())).blockLast();
        assertTrue(deployments.size() > 0);
    }

    @Test
    public void testGetDeployment() {
        Deployments client = createClient();
        Deployment deployment = client.getDeploymentAsync(TestData.DEPLOYMENT_ID)
            .block();
        assertNotNull(deployment);
        assertEquals(TestData.DEPLOYMENT_ID, deployment.getDeploymentId());
        assertEquals(DeploymentType.COMPLETE, deployment.getDeploymentType());
        assertEquals(DeviceGroupType.DEVICE_GROUP_DEFINITIONS, deployment.getDeviceGroupType());
        assertEquals(TestData.PROVIDER, deployment.getUpdateId().getProvider());
        assertEquals(TestData.NAME, deployment.getUpdateId().getName());
        assertEquals(TestData.VERSION, deployment.getUpdateId().getVersion());
    }

    @Test
    public void testGetDeploymentNotFound() {
        Deployments client = createClient();
        try {
            client.getDeploymentAsync("foo")
                .block();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testCreateCancelAndDeleteDeployment() {
        if (getTestMode() != TestMode.LIVE) {
            return;
        }

        Deployments client = createClient();
        String deployment_id = UUID.randomUUID().toString();
        List<String> devices = new ArrayList<String>();
        devices.add(TestData.DEVICE_ID);
        Deployment deployment = client.createOrUpdateDeploymentAsync(
            deployment_id,
            new Deployment()
                .setDeploymentId(deployment_id)
                .setDeploymentType(DeploymentType.COMPLETE)
                .setStartDateTime(TestData.CREATE_DEPLOYMENT_START)
                .setDeviceGroupType(DeviceGroupType.DEVICE_GROUP_DEFINITIONS)
                .setDeviceGroupDefinition(devices)
                .setUpdateId(
                    new UpdateId()
                        .setProvider(TestData.PROVIDER)
                        .setName(TestData.NAME)
                        .setVersion(TestData.VERSION))
            )
            .block();
        assertNotNull(deployment);
        assertEquals(deployment_id, deployment.getDeploymentId());

        deployment = client.getDeploymentAsync(deployment_id)
            .block();
        assertNotNull(deployment);
        assertEquals(deployment_id, deployment.getDeploymentId());
        assertFalse(deployment.isCanceled());

        DeploymentStatus deploymentStatus = client.getDeploymentStatusAsync(deployment_id)
            .block();
        assertNotNull(deploymentStatus);
        assertEquals(DeploymentState.ACTIVE, deploymentStatus.getDeploymentState());

        deployment = client.cancelDeploymentAsync(deployment_id)
            .block();
        assertNotNull(deployment);
        assertEquals(deployment_id, deployment.getDeploymentId());
        assertTrue(deployment.isCanceled());

        Response<Void> responseDelete = client.deleteDeploymentWithResponseAsync(deployment_id)
            .block();
        assertNotNull(deployment);
        assertEquals(200, responseDelete.getStatusCode());

        try {
            client.getDeploymentAsync(deployment_id)
                .block();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetDeploymentStatus() {
        Deployments client = createClient();
        DeploymentStatus deploymentStatus = client.getDeploymentStatusAsync(TestData.DEPLOYMENT_ID)
            .block();
        assertNotNull(deploymentStatus);
        assertEquals(DeploymentState.ACTIVE, deploymentStatus.getDeploymentState());
    }

    @Test
    public void testGetDeploymentStatusNotFound() {
        Deployments client = createClient();
        try {
            client.getDeploymentStatusWithResponseAsync("foo")
                .block();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetDeploymentDevices() {
        Deployments client = createClient();
        PagedFlux<DeploymentDeviceState> response = client.getDeploymentDevicesAsync(TestData.DEPLOYMENT_ID, null);

        assertNotNull(response);
        List<DeploymentDeviceState> deviceStates = new ArrayList<>();
        response.byPage().map(page -> deviceStates.addAll(page.getValue())).blockLast();
        assertTrue(deviceStates.size() > 0);
    }

    @Test
    public void testGetDeploymentDevicesNotFound() {
        Deployments client = createClient();
        try {
            PagedFlux<DeploymentDeviceState> response = client.getDeploymentDevicesAsync("foo", null);
            List<DeploymentDeviceState> deviceStates = new ArrayList<>();
            response.byPage().map(page -> deviceStates.addAll(page.getValue())).blockLast();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }
}
