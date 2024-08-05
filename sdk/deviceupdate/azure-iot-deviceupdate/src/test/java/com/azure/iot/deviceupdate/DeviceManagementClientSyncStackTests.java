// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.deviceupdate;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.azure.iot.deviceupdate.TestUtils.getCredential;

public class DeviceManagementClientSyncStackTests extends TestProxyTestBase {
    protected DeviceManagementClient deviceManagementClient;
    private RequestOptions requestOptions;

    @Override
    protected void beforeTest() {
        DeviceManagementClientBuilder clientBuilder = new DeviceManagementClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", TestData.ACCOUNT_ENDPOINT))
            .instanceId(Configuration.getGlobalConfiguration().get("INSTANCEID", TestData.INSTANCE_ID))
            .httpClient(buildSyncAssertingClient(HttpClient.createDefault()))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(buildSyncAssertingClient(interceptorManager.getPlaybackClient()));
        } else if (interceptorManager.isRecordMode()) {
            clientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }

        deviceManagementClient = clientBuilder.credential(getCredential(getTestMode())).buildClient();
        requestOptions = new RequestOptions();
    }

    @Test
    public void testListGroups() {
        PagedIterable<BinaryData> response = deviceManagementClient.listGroups(requestOptions);
        Assertions.assertEquals(200, response.iterableByPage().iterator().next().getStatusCode());
        Assertions.assertTrue(response.stream().findAny().isPresent());
    }

    @Test
    public void testGetGroup() {
        Response<BinaryData> response = deviceManagementClient.getGroupWithResponse(TestData.DEVICE_GROUP, requestOptions);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(response.getValue());
    }

    @Test
    public void testListBestUpdatesForGroup() {
        PagedIterable<BinaryData> response = deviceManagementClient.listBestUpdatesForGroup(TestData.DEVICE_GROUP, requestOptions);
        Assertions.assertEquals(200, response.iterableByPage().iterator().next().getStatusCode());
        Assertions.assertTrue(response.stream().findAny().isPresent());
    }

    @Test
    public void testListDeviceClassSubgroupsForGroup() {
        PagedIterable<BinaryData> response =
            deviceManagementClient.listDeviceClassSubgroupsForGroup(TestData.DEVICE_GROUP, requestOptions);
        Assertions.assertEquals(200, response.iterableByPage().iterator().next().getStatusCode());
        Assertions.assertTrue(response.stream().findAny().isPresent());
    }

    @Test
    public void testGetUpdateCompliance() {
        Response<BinaryData> response = deviceManagementClient.getUpdateComplianceWithResponse(requestOptions);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(response.getValue());
    }

    @Test
    public void testGetUpdateComplianceForGroup() {
        Response<BinaryData> response =
            deviceManagementClient.getUpdateComplianceForGroupWithResponse(TestData.DEVICE_GROUP, requestOptions);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(response.getValue());
    }

    @Test
    public void testListDevices() {
        PagedIterable<BinaryData> response = deviceManagementClient.listDevices(requestOptions);
        Assertions.assertEquals(200, response.iterableByPage().iterator().next().getStatusCode());
        Assertions.assertTrue(response.stream().findAny().isPresent());
    }

    @Test
    public void testGetDevice() {
        Response<BinaryData> response = deviceManagementClient.getDeviceWithResponse(TestData.DEVICE_ID, requestOptions);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(response.getValue());
    }

    @Test
    public void testListDeviceClasses() {
        PagedIterable<BinaryData> response = deviceManagementClient.listDeviceClasses(requestOptions);
        Assertions.assertEquals(200, response.iterableByPage().iterator().next().getStatusCode());
        Assertions.assertTrue(response.stream().findAny().isPresent());
    }

    @Test
    public void testListHealthOfDevices() {
        PagedIterable<BinaryData> response =
            deviceManagementClient.listHealthOfDevices("state eq 'Healthy'", requestOptions);
        Assertions.assertEquals(200, response.iterableByPage().iterator().next().getStatusCode());
        Assertions.assertTrue(response.stream().findAny().isPresent());
    }

    @Test
    public void testGetDeviceClass() {
        Response<BinaryData> response =
            deviceManagementClient.getDeviceClassWithResponse(TestData.DEVICE_CLASS_ID, requestOptions);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(response.getValue());
    }

    @Test
    public void testListInstallableUpdatesForDeviceClass() {
        PagedIterable<BinaryData> response =
            deviceManagementClient.listInstallableUpdatesForDeviceClass(TestData.DEVICE_CLASS_ID, requestOptions);
        Assertions.assertEquals(200, response.iterableByPage().iterator().next().getStatusCode());
        Assertions.assertTrue(response.stream().findAny().isPresent());
    }

    @Test
    public void testGetDeviceClassSubgroup() {
        Response<BinaryData> response =
            deviceManagementClient.getDeviceClassSubgroupWithResponse(TestData.DEVICE_GROUP, TestData.DEVICE_CLASS_ID, requestOptions);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(response.getValue());
    }

    @Test
    public void testListBestUpdatesForDeviceClassSubgroup() {
        Response<BinaryData> response =
            deviceManagementClient.getBestUpdatesForDeviceClassSubgroupWithResponse(
                TestData.DEVICE_GROUP, TestData.DEVICE_CLASS_ID, requestOptions);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(response.getValue());
    }

    @Test
    public void testGetDeviceClassSubgroupUpdateCompliance() {
        Response<BinaryData> response =
            deviceManagementClient.getDeviceClassSubgroupUpdateComplianceWithResponse(
                TestData.DEVICE_GROUP, TestData.DEVICE_CLASS_ID, requestOptions);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(response.getValue());
    }

    @Test
    public void testListDeploymentsForGroup() {
        PagedIterable<BinaryData> response =
            deviceManagementClient.listDeploymentsForGroup(TestData.DEVICE_GROUP, requestOptions);
        Assertions.assertEquals(200, response.iterableByPage().iterator().next().getStatusCode());
        Assertions.assertTrue(response.stream().findAny().isPresent());
    }

    @Test
    public void testGetDeployment() {
        Response<BinaryData> response =
            deviceManagementClient.getDeploymentWithResponse(TestData.DEVICE_GROUP, TestData.DEPLOYMENT_ID, requestOptions);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(response.getValue());
    }

    @Test
    public void testListDeploymentsForDeviceClassSubgroup() {
        PagedIterable<BinaryData> response =
            deviceManagementClient.listDeploymentsForDeviceClassSubgroup(
                TestData.DEVICE_GROUP, TestData.DEVICE_CLASS_ID, requestOptions);
        Assertions.assertEquals(200, response.iterableByPage().iterator().next().getStatusCode());
        Assertions.assertTrue(response.stream().findAny().isPresent());
    }

    @Test
    public void testGetDeploymentForDeviceClassSubgroup() {
        Response<BinaryData> response =
            deviceManagementClient.getDeploymentForDeviceClassSubgroupWithResponse(
                TestData.DEVICE_GROUP, TestData.DEVICE_CLASS_ID, TestData.DEPLOYMENT_ID, requestOptions);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(response.getValue());
    }

    @Test
    public void testGetDeploymentStatus() {
        Response<BinaryData> response =
            deviceManagementClient.getDeploymentStatusWithResponse(TestData.DEVICE_GROUP, TestData.DEPLOYMENT_ID, requestOptions);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(response.getValue());
    }

    @Test
    public void testGetDeviceClassSubgroupDeploymentStatus() {
        Response<BinaryData> response =
            deviceManagementClient.getDeviceClassSubgroupDeploymentStatusWithResponse(
                TestData.DEVICE_GROUP, TestData.DEVICE_CLASS_ID, TestData.DEPLOYMENT_ID, requestOptions);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(response.getValue());
    }

    @Test
    public void testListDeviceStatesForDeviceClassSubgroupDeployment() {
        PagedIterable<BinaryData> response =
            deviceManagementClient.listDeviceStatesForDeviceClassSubgroupDeployment(
                TestData.DEVICE_GROUP, TestData.DEVICE_CLASS_ID, TestData.DEPLOYMENT_ID, requestOptions);
        Assertions.assertEquals(200, response.iterableByPage().iterator().next().getStatusCode());
        Assertions.assertTrue(response.stream().findAny().isPresent());
    }

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertSync()
            .build();
    }
}
