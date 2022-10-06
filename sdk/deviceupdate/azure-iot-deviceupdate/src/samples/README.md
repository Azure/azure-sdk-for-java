# Introduction

These samples demonstrate basic operations with `DeviceUpdateClient` and `DeviceManagementClient` service clients. 
`DeviceUpdateClient` is used to manage updates and `DeviceManagementClient` is used to manage devices and deployments.
Ech method call sends a request to the service's REST API. To get started, you'll need Device Update for IoT Hub 
AccountId (hostname) and InstanceId which you can access in Azure Portal. 
See the [README](https://github.com/Azure/azure-sdk-for-net/tree/main/sdk/deviceupdate/Azure.IoT.DeviceUpdate/README.md) 
for links and instructions.

# Device Update for IoT Hub Samples

Samples here demonstrate the following:

- Instantiate the client
- Enumerate available updates
- Retrieve a specific device update metadata and the corresponding file metadata
- Import new device update
- Delete existing device update
- Enumerate devices, groups and device classes
- Retrieve best updates for devices in a specific group
- Deploy existing update to a group of devices

## Prerequisites

You need an [Azure subscription][https://azure.microsoft.com/free/], and a [Device Update for IoT Hub][https://docs.microsoft.com/azure/iot-hub-device-update/understand-device-update] 
account and instance to use this package.

Set the following environment variables:

- `AZURE_CLIENT_ID`: AAD service principal client id
- `AZURE_TENANT_ID`: AAD tenant id
- `AZURE_CLIENT_SECRET` or `AZURE_CLIENT_CERTIFICATE_PATH`: AAD service principal client secret

- `DEVICEUPDATE_ENDPOINT`: Device Update for IoT Hub hostname
- `DEVICEUPDATE_INSTANCE_ID`: Device Update for IoT Hub instance name
- `DEVICEUPDATE_UPDATE_PROVIDER`: Update provider to retrieve, deploy or delete
- `DEVICEUPDATE_UPDATE_NAME`: Update name to retrieve, deploy or delete
- `DEVICEUPDATE_UPDATE_VERSION`: Update version to retrieve, deploy or delete
- `DEVICEUPDATE_DEVICE_GROUP`: Device group to enumerate or deploy update to

## Creating service client

We have to clients, `DeviceUpdateClient` to manage updates and `DeviceManagementClient` to manage devices and deployments.

Let's start by creating instance of `DeviceUpdateClient` using environment variables:

``` java com.azure.iot.deviceupdate.DeviceUpdateClient.instantiate
DeviceUpdateClient client = new DeviceUpdateClientBuilder()
    .endpoint(Configuration.getGlobalConfiguration().get("AZURE_ACCOUNT_ENDPOINT"))
    .instanceId(Configuration.getGlobalConfiguration().get("AZURE_INSTANCE_ID"))
    .credential(new DefaultAzureCredentialBuilder().build())
    .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
    .buildClient();
```

To instantiate `DeviceManagementClient` we use similar code:

``` java com.azure.iot.deviceupdate.DeviceManagementClient.instantiate
DeviceManagementClient client = new DeviceManagementClientBuilder()
    .endpoint(Configuration.getGlobalConfiguration().get("AZURE_ACCOUNT_ENDPOINT"))
    .instanceId(Configuration.getGlobalConfiguration().get("AZURE_INSTANCE_ID"))
    .credential(new DefaultAzureCredentialBuilder().build())
    .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
    .buildClient();
```


### Enumerate update providers

Let's start by enumerating all update providers:

``` java com.azure.iot.deviceupdate.DeviceUpdateClient.EnumerateProviders
PagedIterable<BinaryData> providers = client.listProviders(null);
for (BinaryData p: providers) {
    System.out.println(p);
}
```

### Enumerate update names

Let's enumerate all update names for a given update provider:

``` java com.azure.iot.deviceupdate.DeviceUpdateClient.EnumerateNames
System.out.println("Providers:");
PagedIterable<BinaryData> names = client.listNames(updateProvider, null);
for (BinaryData n: names) {
    System.out.println(n);
}
```

### Enumerate update versions

Let's enumerate all update version for a given update provider and update name:

``` java com.azure.iot.deviceupdate.DeviceUpdateClient.EnumerateVersions
PagedIterable<BinaryData> versions = client.listVersions(updateProvider, updateName, null);
for (BinaryData v: versions) {
    System.out.println(v);
}
```

### Get update information

Let's retrieve specific update metadata. As before we first read environment variables to 
retrieve specific update identity to retrieve:

``` java
String updateProvider = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_UPDATE_PROVIDER");
String updateName = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_UPDATE_NAME");
String updateVersion = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_UPDATE_VERSION");
```

Now we get request the specific update metadata:

``` java com.azure.iot.deviceupdate.DeviceUpdateClient.GetUpdate
System.out.println("Update:");
Response<BinaryData> updateResponse = client.getUpdateWithResponse(updateProvider, updateName, updateVersion, null);
ObjectMapper updateMapper = new ObjectMapper();
JsonNode updateJsonNode = updateMapper.readTree(updateResponse.getValue().toBytes());
System.out.println("  Provider: " + updateJsonNode.get("updateId").get("provider").asText());
System.out.println("  Name: " + updateJsonNode.get("updateId").get("name").asText());
System.out.println("  Version: " + updateJsonNode.get("updateId").get("version").asText());
System.out.println("Metadata:");
System.out.println(updateResponse.getValue());
```

### Enumerate update files

To enumerate all update files corresponding to our device update:

``` java com.azure.iot.deviceupdate.DeviceUpdateClient.EnumerateUpdateFiles
PagedIterable<BinaryData> items = client.listFiles(updateProvider, updateName, updateVersion, null);
List<String> fileIds = new ArrayList<String>();
ObjectMapper fileIdMapper = new ObjectMapper();
for (BinaryData i: items) {
    String fileId = fileIdMapper.readTree(i.toBytes()).asText();
    System.out.println(fileId);
    fileIds.add(fileId);
}
```

### Get update file information

Now that we know update file identities, we can retrieve their metadata:

``` java com.azure.iot.deviceupdate.DeviceUpdateClient.GetFiles
PagedIterable<BinaryData> files = client.listFiles(updateProvider, updateName, updateVersion, null);
ObjectMapper fileMapper = new ObjectMapper();
for (String fileId: fileIds) {
    System.out.println("File:");
    Response<BinaryData> fileResponse  = client.getFileWithResponse(updateProvider, updateName, updateVersion, fileId, null);
    JsonNode fileJsonNode = fileMapper.readTree(fileResponse.getValue().toBytes());
    System.out.println("  FileId: " + fileJsonNode.get("fileId").asText());
    System.out.println("Metadata:");
    System.out.println(fileResponse.getValue());
}
```

### Enumerate devices

To enumerate all registered devices and print their device identifiers:

``` java com.azure.iot.deviceupdate.DeviceManagementClient.EnumerateDevices
PagedIterable<BinaryData> devices = client.listDevices(null);
for (BinaryData d: devices) {
    System.out.println(new ObjectMapper().readTree(d.toBytes()).get("deviceId").asText());
}
```

### Enumerate device groups

To enumerate all available device groups and print their group identifiers:

``` java com.azure.iot.deviceupdate.DeviceManagementClient.EnumerateGroups
PagedIterable<BinaryData> groups = client.listGroups(null);
for (BinaryData g: groups) {
    System.out.println(new ObjectMapper().readTree(g.toBytes()).get("groupId").asText());
}
```

### Enumerate device classes

To enumerate all available device classes and print their device class identifiers:

``` java com.azure.iot.deviceupdate.DeviceManagementClient.EnumerateDeviceClasses
PagedIterable<BinaryData> deviceClasses = client.listDeviceClasses(null);
for (BinaryData dc: deviceClasses) {
    System.out.println(new ObjectMapper().readTree(dc.toBytes()).get("deviceClassId").asText());
}
```

### Get best updates for all devices within a specific device group

Finally, lets find out all best updates for all devices in a specific group, groupped by their device class identifier:

``` java com.azure.iot.deviceupdate.DeviceManagementClient.GetBestUpdates
PagedIterable<BinaryData> bestUpdates = client.listBestUpdatesForGroup(groupId, null);
ObjectMapper updateMapper = new ObjectMapper();
for (BinaryData bu: bestUpdates) {
    JsonNode json = updateMapper.readTree(bu.toBytes());
    System.out.println(String.format("For device class '%s' in group '%s', the best update is:",
        json.get("deviceClassId").asText(), groupId));
    System.out.println("  Provider: " + json.get("update").get("updateId").get("provider").asText());
    System.out.println("  Name: " + json.get("update").get("updateId").get("name").asText());
    System.out.println("  Version: " + json.get("update").get("updateId").get("version").asText());
}
```

### Import update

Before we can import device update, we need to upload all device update artifacts, in our case payload file and import 
manifest file, to an Azure Blob container. Let's assume we have local artifact file and import manifest 
local filepath as well as Azure Blob Urls save in environment variables..

We will need to be able to calculate file hash and file size. For that we will use the following methods:

``` java
private static long GetFileSize(String payloadLocalFile) {
    File file = new File(payloadLocalFile);
    return file.length();
}

private static String GetFileName(String payloadLocalFile) {
    File file = new File(payloadLocalFile);
    return file.getName();
}

private static String GetFileHash(String payloadLocalFile) throws IOException {
    try {
        MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
        String payload = ReadAllTextFromFile(payloadLocalFile);
        byte[] result = mDigest.digest(payload.getBytes());

        return Base64.getEncoder().encodeToString(result);
    } catch (NoSuchAlgorithmException e) {
        return null;
    }
}

private static String ReadAllTextFromFile(String filePath) throws IOException {
    String content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
    return content;
}
```

Now we can create import request.

``` java
String payloadFile = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_PAYLOAD_FILE");
String payloadUrl = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_PAYLOAD_URL");
String manifestFile = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_MANIFEST_FILE");
String manifestUrl = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_MANIFEST_URL");

String content = String.format("[{\"importManifest\": {\"url\": \"%s\", \"sizeInBytes\": %s, \"hashes\": { \"sha256\": \"%s\" }}, " +
        "\"files\": [{\"fileName\": \"%s\", \"url\": \"%s\" }]" +
        "}]",
    manifestUrl, GetFileSize(manifestFile), GetFileHash(manifestFile),
    GetFileName(payloadFile), payloadUrl);
```

Now we can start import process.

``` java com.azure.iot.deviceupdate.DeviceUpdateClient.ImportUpdate
SyncPoller<BinaryData, BinaryData> response = client.beginImportUpdate(BinaryData.fromString(content), null);
response.waitForCompletion();
```

### Delete update

Let's retrieve specific update metadata:

``` java com.azure.iot.deviceupdate.DeviceUpdateClient.DeleteUpdate
SyncPoller<BinaryData, BinaryData> response = client.beginDeleteUpdate(updateProvider, updateName, updateVersion, null);
response.waitForCompletion();
```

### Deploy update to a group of devices

First we need to create new deployment:

``` java
String deploymentId = UUID.randomUUID().toString();
String startAt = OffsetDateTime.now().toString();
String deployment = String.format("{\"deploymentId\": \"%s\", \"startDateTime\": \"%s\", \"update\": {" +
        "\"updateId\": {" +
        "\"provider\": \"%s\", \"name\": \"%s\", \"version\": \"%s\"" +
        "}}," +
        "\"groupId\": \"%s\"" +
        "}",
    deploymentId, startAt,
    updateProvider, updateName, updateVersion,
    groupId);
```

Then we can start the deployment:

``` java com.azure.iot.deviceupdate.DeviceManagementClient.DeployUpdate
Response<BinaryData> response = client.createOrUpdateDeploymentWithResponse(groupId, deploymentId, BinaryData.fromString(deployment), null);
```

To monitor the progress of the deployment:

``` java com.azure.iot.deviceupdate.DeviceManagementClient.CheckDeploymentState
Response<BinaryData> stateResponse = client.getDeploymentStatusWithResponse(groupId, deploymentId, null);
System.out.println(stateResponse.getValue());
```
