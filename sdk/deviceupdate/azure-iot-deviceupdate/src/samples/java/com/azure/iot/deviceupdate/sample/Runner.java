package com.azure.iot.deviceupdate.sample;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.*;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.rest.PagedFlux;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.iot.deviceupdate.DeploymentsAsyncClient;
import com.azure.iot.deviceupdate.DeviceUpdateClientBuilder;
import com.azure.iot.deviceupdate.DevicesAsyncClient;
import com.azure.iot.deviceupdate.UpdatesAsyncClient;
import com.azure.iot.deviceupdate.models.*;
import com.azure.iot.deviceupdate.models.Error;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Runner {
    private static final String DEFAULT_SCOPE = "6ee392c4-d339-4083-b04d-6b7947c6cf78/.default";

    private static final String MANUFACTURER = "Contoso";
    private static final String MODEL = "Virtual-Machine";
    private static final String BLOB_CONTAINER = "test";
    private static final long DEFAULT_RETRY_AFTER = 5;

    private final String storageName;
    private final String storageKey;
    private final String accountEndpoint;
    private final String instanceId;
    private final String deviceId;
    private final String deviceTag;
    private final boolean delete;

    private UpdatesAsyncClient updatesClient;
    private DevicesAsyncClient devicesClient;
    private DeploymentsAsyncClient deploymentsClient;

    public Runner(String tenantId, String clientId, String clientSecret, String accountEndpoint, String instanceId,
                  String storageName, String storageKey, String deviceId, String deviceTag, boolean delete) {
        ClientSecretCredential credentials = new ClientSecretCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();
        BearerTokenAuthenticationPolicy bearerTokenAuthenticationPolicy = new BearerTokenAuthenticationPolicy(credentials, DEFAULT_SCOPE);

        HttpHeaders headers = new HttpHeaders().put("Accept", ContentType.APPLICATION_JSON);
        AddHeadersPolicy addHeadersPolicy = new AddHeadersPolicy(headers);
        HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(HttpClient.createDefault())
            .policies(bearerTokenAuthenticationPolicy, addHeadersPolicy)
            .build();

        DeviceUpdateClientBuilder builder = new DeviceUpdateClientBuilder()
            .accountEndpoint(accountEndpoint)
            .instanceId(instanceId)
            .pipeline(httpPipeline);
        this.updatesClient = builder.buildUpdatesAsyncClient();
        this.devicesClient = builder.buildDevicesAsyncClient();
        this.deploymentsClient = builder.buildDeploymentsAsyncClient();

        this.storageName = storageName;
        this.storageKey = storageKey;
        this.accountEndpoint = accountEndpoint;
        this.instanceId = instanceId;
        this.deviceId = deviceId;
        this.deviceTag = deviceTag;
        this.delete = delete;
    }

    public void Run() throws Exception {
        String version = new SimpleDateFormat("yyyy.Mdd.Hmm.s").format(Calendar.getInstance().getTime());

        // Create new update and import it into ADU
        String jobId = ImportUpdate(version);

        // Let's retrieve the existing (newly imported) update
        RetrieveUpdateStep(MANUFACTURER, MODEL, version, false);

        // Create deployment/device group
        String groupId = CreateDeploymentGroupStep();

        // Check that device group contains devices that can be updated with our new update
        CheckGroupDevicesAreUpToDateStep(groupId, MANUFACTURER, MODEL, version, false);

        // Create deployment for our device group to deploy our new update
        String deploymentId = DeployUpdateStep(MANUFACTURER, MODEL, version, groupId);

        // Check that device group contains *NO* devices that can be updated with our new update
        CheckGroupDevicesAreUpToDateStep(MANUFACTURER, MODEL, version);

        // Check that device group contains *NO* devices that can be updated with our new update
        CheckGroupDevicesAreUpToDateStep(groupId, MANUFACTURER, MODEL, version, true);

        if (this.delete) {
            // Delete the update
            DeleteUpdateStep(MANUFACTURER, MODEL, version);

            // Let's retrieve the deleted update (newly imported) update and expect 404 (not found response)
            RetrieveUpdateStep(MANUFACTURER, MODEL, version, true);
        }

        // Dump test data to be used for unit-testing
        OutputTestData(version, jobId, deploymentId);
    }

    private String ImportUpdate(String version) throws Exception {
        ContentFactory contentFactory = new ContentFactory(this.storageName, this.storageKey, BLOB_CONTAINER);
        ImportUpdateInput update = contentFactory.CreateImportUpdate(MANUFACTURER, MODEL, version);

        System.out.println("Importing updates...");
        UpdatesImportUpdateResponse response = updatesClient.importUpdateWithResponse(update).block();
        String operationId = getOperationId(response.getHeaders().get("Location").getValue());
        System.out.println("Import operation id: " + operationId);

        System.out.println("(this may take a minute or two)");
        boolean repeat = true;
        while (repeat) {
            UpdatesGetOperationResponse operation = updatesClient.getOperationWithResponse(operationId, null).block();
            if (operation.getValue().getStatus() == OperationStatus.SUCCEEDED)
            {
                System.out.println(operation.getValue().getStatus());
                repeat = false;
            }
            else if (operation.getValue().getStatus() == OperationStatus.FAILED)
            {
                Error error = operation.getValue().getError();
                ObjectMapper objectMapper = new ObjectMapper();
                throw new Exception("Import failed with response:\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(error));
            }
            else {
                System.out.print(".");
                waitSeconds(getRetryAfter(operation.getHeaders()));
            }
        }

        return operationId;
    }

    private void RetrieveUpdateStep(String provider, String name, String version, boolean notFoundExpected) throws Exception {
        System.out.println("Retrieving update...");
        try {
            Update update = updatesClient.getUpdate(provider, name, version, null)
                .block();
            if (notFoundExpected) {
                throw new Exception("Service returned valid update even though NotFound response was expected");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(update));
        } catch (HttpResponseException e) {
            if (notFoundExpected && e.getResponse().getStatusCode() == 404)
            {
                System.out.println("Received an expected NotFound response");
            }
            else
            {
                throw new Exception("Service returned unexpected error status code: " + e.getResponse().getStatusCode());
            }
        }

        System.out.println();
    }

    private String CreateDeploymentGroupStep() {
        String groupId = deviceTag;
        boolean createNewGroup = false;

        System.out.println("Querying deployment group...");
        try {
            Group group = devicesClient.getGroup(groupId)
                .block();
            System.out.println(String.format("Deployment group %s already exists.", groupId));
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == 404) {
                createNewGroup = true;
            }
        }

        if (createNewGroup) {
            System.out.println("Creating deployment group...");
            List<String> devices = new ArrayList<String>();
            devices.add(deviceId);
            Group group = devicesClient.createOrUpdateGroup(
                groupId,
                new Group()
                    .setGroupId(groupId)
                    .setGroupType(GroupType.IO_THUB_TAG)
                    .setTags(devices)
                    .setCreatedDateTime(DateTimeFormatter.ISO_INSTANT.format(OffsetDateTime.now())))
                .block();

            System.out.println(String.format("Group %s created.", groupId));
            System.out.println();

            System.out.println("Waiting for the group to be populated with devices...");
            System.out.println("(this may take about five minutes to complete)");
            boolean repeat = true;
            while (repeat) {
                group = devicesClient.getGroup(groupId).block();
                if (group.getDeviceCount() > 0) {
                    System.out.println(String.format("Deployment group %s now has %s devices.", groupId, group.getDeviceCount()));
                    repeat = false;
                } else {
                    System.out.print(".");
                    waitSeconds(DEFAULT_RETRY_AFTER);
                }
            }
        }

        System.out.println();
        return groupId;
    }

    private void CheckGroupDevicesAreUpToDateStep(String groupId, String provider, String name, String version, boolean isCompliant) {
        System.out.println(String.format("Check group %s device compliance with update %s/%s/%s...", groupId, provider, name, version));
        boolean updateFound = false;
        int counter = 0;
        do {
            PagedFlux<UpdatableDevices> response = devicesClient.getGroupBestUpdates(groupId, null);
            List<UpdatableDevices> groupUpdatableDevices = new ArrayList<>();
            response.byPage().map(page -> groupUpdatableDevices.addAll(page.getValue())).blockLast();
            for (UpdatableDevices updatableDevices : groupUpdatableDevices) {
                UpdateId update = updatableDevices.getUpdateId();
                if (provider.equals(update.getProvider())  &&
                    name.equals(update.getName()) &&
                    version.equals(update.getVersion())) {
                    updateFound = true;
                    if (isCompliant) {
                        if (updatableDevices.getDeviceCount() == 0) {
                            System.out.println("All devices within the group have this update installed.");
                        }
                        else {
                            System.out.println(String.format("There are still %s devices that can be updated to update %s/%s/%s.",
                                updatableDevices.getDeviceCount(), provider, name, version));
                        }
                    }
                    else {
                        System.out.println(String.format("There are %s devices that can be updated to update %s/%s/%s.",
                            updatableDevices.getDeviceCount(), provider, name, version));
                    }
                }
            }

            counter++;
            if (!updateFound) {
                System.out.print(".");
                waitSeconds(DEFAULT_RETRY_AFTER);
            }
        } while (!updateFound && counter <= 6 );

        if (!updateFound) {
            System.out.println("Update is still not available for any group device.");
        }
        System.out.println();
    }

    private String DeployUpdateStep(String provider, String name, String version, String groupId) {
        System.out.println("Deploying the update to a device...");
        String deploymentId = String.format("%s-%s", deviceId, version.replace(".", "-"));
        List<String> groups = new ArrayList<String>();
        groups.add(groupId);
        Deployment deployment = deploymentsClient.createOrUpdateDeployment(
            deploymentId,
            new Deployment()
                .setDeploymentId(deploymentId)
                .setDeploymentType(DeploymentType.COMPLETE)
                .setStartDateTime(OffsetDateTime.now())
                .setDeviceGroupType(DeviceGroupType.DEVICE_GROUP_DEFINITIONS)
                .setDeviceGroupDefinition(groups)
                .setUpdateId(new UpdateId()
                    .setProvider(provider)
                    .setName(name)
                    .setVersion(version)))
            .block();
        System.out.println(String.format("Deployment '%s' is created.", deployment.getDeploymentId()));
        waitSeconds(DEFAULT_RETRY_AFTER);

        System.out.println("Checking the deployment status...");
        DeploymentStatus deploymentStatus = deploymentsClient.getDeploymentStatus(deploymentId).block();
        System.out.println("  " + deploymentStatus.getDeploymentState());

        System.out.println();
        return deploymentId;    }

    private void CheckGroupDevicesAreUpToDateStep(String provider, String name, String version) {
        System.out.println(String.format("Checking device %s status...", deviceId));
        System.out.println("Waiting for the update to be installed...");
        boolean repeat = true;
        while (repeat) {
            Device device = devicesClient.getDevice(deviceId).block();
            UpdateId installedUpdateId = device.getInstalledUpdateId();
            if (installedUpdateId != null &&
                provider.equals(installedUpdateId.getProvider())  &&
                name.equals(installedUpdateId.getName()) &&
                version.equals(installedUpdateId.getVersion()))
            {
                repeat = false;
            }
            else
            {
                System.out.print(".");
                waitSeconds(DEFAULT_RETRY_AFTER);
            }
        }

        System.out.println();
    }

    private void DeleteUpdateStep(String provider, String name, String version) throws Exception {
        System.out.println("Deleting the update...");
        UpdatesDeleteUpdateResponse response = updatesClient.deleteUpdateWithResponse(provider, name, version).block();
        String operationId = getOperationId(response.getHeaders().get("Location").getValue());
        System.out.println("Delete operation id: " + operationId);

        System.out.println("Waiting for delete to finish...");
        boolean repeat = true;
        while (repeat) {
            UpdatesGetOperationResponse operation = updatesClient.getOperationWithResponse(operationId, null).block();
            if (operation.getValue().getStatus() == OperationStatus.SUCCEEDED) {
                System.out.println(operation.getValue().getStatus());
                repeat = false;
            } else if (operation.getValue().getStatus() == OperationStatus.FAILED) {
                Error error = operation.getValue().getError();
                ObjectMapper objectMapper = new ObjectMapper();
                throw new Exception("Delete failed with response:\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(error));
            } else {
                System.out.print(".");
                waitSeconds(getRetryAfter(operation.getHeaders()));
            }
        }
    }

    private void OutputTestData(String version, String operationId, String deploymentId) {
        System.out.println(String.format("$env:AZURE_ACCOUNT_ENDPOINT=\"%s\"", accountEndpoint));
        System.out.println(String.format("$env:AZURE_INSTANCE_ID=\"%s\"", instanceId));
        System.out.println(String.format("$env:AZURE_UPDATE_VERSION=\"%s\"", version));
        System.out.println(String.format("$env:AZURE_UPDATE_OPERATION=\"%s\"", operationId));
        System.out.println(String.format("$env:AZURE_DEVICE_ID=\"%s\"", deviceId));
        System.out.println(String.format("$env:AZURE_GROUP_ID=\"%s\"", deviceTag));
        System.out.println(String.format("$env:AZURE_DEPLOYMENT_ID=\"%s\"", deploymentId));
        System.out.println();
        System.out.println("Set these environment variables before opening and running SDK unit tests.");

        System.out.println();
    }

    private String getOperationId(String location) {
        return location.substring(location.lastIndexOf("/") + 1);
    }

    private long getRetryAfter(HttpHeaders headers) {
        if (headers.get("Retry-After") != null)
        {
            return Long.parseLong(headers.getValue("Retry-After"));
        }
        return DEFAULT_RETRY_AFTER;
    }

    private void waitSeconds(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        }
        catch (InterruptedException e) {
        }
    }
}
