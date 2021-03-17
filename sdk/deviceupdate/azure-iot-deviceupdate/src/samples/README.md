# Device Update for IoT Hub Samples
You can explore Device Update for IoT Hub APIs (using the SDK) with our samples project. This document describes the most commonly used API methods for the most common scenarios (this section does not attempt to be a complete API documentation).

 The sample covers these scanarios and actions:

- publish new update 
- retrieve the newly imported update
- create device group (if not exists already)
- wait for update to be installable to our device group
- create deployment to deploy our new update to our device
- check deployment and wait for the device to report the update being deployed
- retrieve all updates installed on the device
- delete the update
- retrieve the deleted update and check that it no longer exists

## Running the samples

To run the samples, you need to compile console Samples project. The executable retrieves arguments from the following environment settings:

| Environment variable   | Description                  |
| ---------------------- | ---------------------------- |
| AZURE_TENANT_ID        | AAD tenant id                |
| AZURE_CLIENT_ID        | AAD client id                |
| AZURE_CLIENT_SECRET    | AAD client secret            |
| AZURE_STORAGE_NAME     | Azure Storage account name   |
| AZURE_STORAGE_KEY      | Azure Storage account key    |
| AZURE_ACCOUNT_ENDPOINT | ADU account endpoint         |
| AZURE_INSTANCE_ID      | ADU instance id              |
| AZURE_DEVICE_ID        | IoT device id                |

## Creating Client

You need to use AzureDeviceUpdateClientBuilder to create a proper valid AzureDeviceUpdateClient. This main client will let you access the other clients - Updates for update management, Devices for device management and finally Deployments for deployment management. To create a new client, you need the Device Update for IoT Hub account, instance and credentials.
In the sample below, you can set `accountEndpoint`, `instanceId`, `tenantId` and `clientId` through the above mentioned environment variables. The client requires an instance of [TokenCredential](https://docs.microsoft.com/java/api/com.azure.core.credential.tokencredential?view=azure-java-stable).
In these samples, we illustrate how to use just derived class: InteractiveLogin. There are other options if you want to use client certificates for authentication (ClientCertificateCredential) or client secret (ClientSecretCredential).

``` java
InteractiveBrowserCredential credentials = new InteractiveBrowserCredentialBuilder()
    .tenantId(tenantId))
    .clientId(clientId)
    .build();
BearerTokenAuthenticationPolicy bearerTokenAuthenticationPolicy = new BearerTokenAuthenticationPolicy(credentials, "6ee392c4-d339-4083-b04d-6b7947c6cf78/.default");
HttpHeaders headers = new HttpHeaders().put("Accept", ContentType.APPLICATION_JSON);
AddHeadersPolicy addHeadersPolicy = new AddHeadersPolicy(headers);
HttpPipeline httpPipeline = new HttpPipelineBuilder()
    .httpClient(HttpClient.createDefault())
    .policies(bearerTokenAuthenticationPolicy, addHeadersPolicy)
    .build();
AzureDeviceUpdateClient client = new AzureDeviceUpdateClientBuilder()
    .accountEndpoint(accountEndpoint)
    .instanceId(instanceId)
    .pipeline(httpPipeline)
    .buildClient();
```

## Update Management

### Import Update

Device Update for IoT Hub client library allows you to import a device update into ADU, as well as the ability to view and delete previously imported update. For Public Preview, ADU supports rolling out a single update per device, making it ideal for full-image updates that update an entire OS partition at once as well as a Desired-State Manifest that describes all the packages you might want to update on your device.

Each update in ADU is described by an **Update** entity. The Update entity describes basic metadata about the entity itself, like its identity and version, as well as metadata about the files that make up the update.

Update identity in ADU consists of three parts: **provider**, **name**, and **version**. In the ADU Public Preview, the names that make up the identity must follow some simple rules in order for updates to be delivered to the correct, compatible devices. The `provider` must match the device manufacturer name, and the `name` must match the device model name.

The importing workflow is as follows:

1. Build/download a device image update or create a Desired-State Manifest for package update
2. Create an ADU import manifest describing the update and its files
3. Upload the manifest and files to an Azure Blob storage
4. Call the API NewUpdate API, passing it the URLs of the manifest and other files
5. Wait for the import process to finish
6. Deploy the update to one or more devices via the ADU Deployment Management API

#### Create Update Payload

We mentioned above that you need to build/download a device image update or create Device-State Manifest. In this Sample code we will deploy an update to a simulator device and we can therefore create simulated device image (arbitrary file would do). In the Samples you will see that we simply create a simple JSON file that we name `setup.exe` and deploy to the simulator device.

#### Create Import Manifest

Create an ADU import manifest (metadata about the update). The manifest will contain compatibility/applicability information, as well as file names and hashes for validation.

#### Upload Import Manifest and Update Payload to Azure Blob

The Samples application uses two envioronment variables `AZURE_STORAGE_NAME` and `AZURE_STORAGE_KEY` that will be used to upload the two files to Azure Blob container `test`.

#### Import the update

Now that we have the update artifacts ready, we can create `ImportUpdateInput` and start the import. The Samples uses the following code to create the proper object:

``` java
Map<String, String> hashes = new HashMap<String, String>();
hashes.put("SHA256", importManifestFileHash);
ImportManifestMetadata importManifest = new ImportManifestMetadata()
    .setUrl(importManifestUrl)
    .setSizeInBytes(importManifestFileSize)
    .setHashes(hashes);
List<FileImportMetadata> files = new ArrayList<FileImportMetadata>();
files.add(
    new FileImportMetadata()
        .setFilename(FILE_NAME)
        .setUrl(payloadUrl));
ImportUpdateInput update = new ImportUpdateInput()
    .setImportManifest(importManifest)
    .setFiles(files);
```

With that we can the import:

``` java
UpdatesImportUpdateResponse response = client.getUpdates()
    .importUpdateWithResponseAsync(update)
    .block();
string location = response.getHeaders().get("Location").getValue();
string operationId = location.substring(location.lastIndexOf("/") + 1);
```

The import operation is asynchronous long-running operation that uses step-aside approach. The operation returns the operation identifier, that you can use to check the status of the import operation.

``` java
while (true)
{
    UpdatesGetOperationResponse operation = client.getUpdates()
        .getOperationWithResponseAsync(operationId, null)
        .block();
    if (operation.getValue().getStatus() == OperationStatus.SUCCEEDED)
    {
		break;
    }
    else if (operation.getValue().getStatus() == OperationStatus.FAILED)
    {
        Error error = operation.getValue().getError();
        ObjectMapper objectMapper = new ObjectMapper();
        throw new Exception("Import failed:\n" +
            objectMapper.writeValueAsString(error));
    }
    else
    {
        Thread.sleep(getRetryAfter(operation.getHeaders()) * 1000);
    }
}
```

`getRetryAfter` is a helper method that retrieves `Retry-After` header value to wait before checking the status again.

### Retrieve existing Update

Using `getUpdateWithResponseAsync` you can retrieve existing update:

``` java
Update update = client.getUpdates()
    .getUpdateAsync(provider, name, version, null)
    .block();
```

If the update doesn't exist, then the method will throw HttpResponseException where you can check the status code:

``` java
try {
    Update response = client.getUpdates()
        .getUpdateAsync(provider, name, version, null)
        .block();
} catch (HttpResponseException e) {
    if (e.getResponse().getStatusCode() == 404) {
        // TODO:
    }
}
```

### Delete Update

To delete an update, pass in the update identity to `deleteUpdateWithResponseAsync` method:

``` java
UpdatesDeleteUpdateResponse response = client.getUpdates()
    .deleteUpdateWithResponseAsync(provider, name, version)
    .block();
```

Similar to importing update, the method will return job identity and you need to check the status of the asynchronous job.

## Device Management

### Create Simulator Device

To create a simulator device, follow steps on [Getting Started Using Ubuntu (18.04 x64) Simulator Reference Client](https://docs.microsoft.com/azure/iot-hub-device-update/device-update-simulator) page.

When you do that and have your simulator running, use your device identifier as `device` command-line argument.

### Device properties

Before you install something on a device you might want to check the currently installed update on a device:

``` java
Device device = client.getDevices()
    .getDeviceAsync(deviceId)
    .block();
UpdateId currentlyInstalledUpdateId = device.getInstalledUpdateId();
```
Instead of checking all individual devices, you can check update compliance for a device group:

``` java
UpdateCompliance compliance = client.getDevices()
    .getGroupUpdateComplianceAsync(groupId)
    .block();
int totalNumberOfDevicesInGroup = compliance.getTotalDeviceCount();
int devicesThatCouldBeUpdated = compliance.getNewUpdatesAvailableDeviceCount();
```

Based on this we can decided to deploy a specific update to the group of devices.

## Deployment Management

### Deploy Update to Device

We can deploy ADU update to a device group (that contains our simulator device):

``` java
List<String> groups = new ArrayList<String>();
groups.add(groupId);
Deployment response = client.getDeployments()
    .createOrUpdateDeploymentAsync(
    deploymentId,
    new Deployment()
        .setDeploymentId(deploymentId)
        .setDeploymentType(DeploymentType.COMPLETE)
        .setStartDateTime(OffsetDateTime.now())
        .setDeviceGroupType(DeviceGroupType.DEVICE_GROUP_DEFINITIONS)
        .setDeviceGroupDefinition(groups)
        .setUpdateId(
            new UpdateId()
                .setProvider(provider)
                .setName(name)
                .setVersion(version))
    )
    .block();
```

`DeploymentId` must be a new unique string identifier for this particular deployment. 

It will take some time for the update to get deployed. You can monitor the progress of the deployment by checking its status:

``` java
DeploymentStatus deploymentStatus = client.getDeployments()
    .getDeploymentStatusAsync(deployment_id)
    .block();
```

The response object has `DeploymentState` property where you can see the current status. 

### Cancel Deployment

You can cancel current deployment by calling `cancelDeploymentWithResponseAsync` method.

``` java
Deployment deployment = client.getDeployments()
    .cancelDeploymentAsync(deployment_id)
    .block();
```
