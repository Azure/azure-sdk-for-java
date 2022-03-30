# Device Update for IoT Hub Samples

Samples under `generated` package are auto generated sample for you to refer how variables should be provided. 
`ListDevicesSample` and `GetUpdateNotFoundSample` are hand-written samples that can be run directly when you configured correct environment variables.

## Running the samples

To run `ListDevicesSample` sample, you need to compile console Samples project. The executable retrieves arguments from the following
environment settings:

| Environment variable   | Description          |
| ---------------------- | -------------------- |
| AZURE_TENANT_ID        | AAD tenant id        |
| AZURE_SUBSCRIPTION_ID  | AAD subscription id  |
| AZURE_CLIENT_ID        | AAD client id        |
| AZURE_CLIENT_SECRET    | AAD client secret    |
| AZURE_ACCOUNT_ENDPOINT | ADU account endpoint |
| AZURE_INSTANCE_ID      | ADU instance id      |


## Creating Client

You need to use AzureDeviceUpdateClientBuilder to create a proper valid AzureDeviceUpdateClient. This main client will
let you access the other clients - Updates and Management. In the sample `ListDevicesSample`, it creates `DeviceManagementAsyncClient`.

```java com.azure.iot.deviceupdate.DeviceManagementAsyncClient.instantiate
DeviceManagementAsyncClient client = new DeviceManagementClientBuilder()
    .endpoint(Configuration.getGlobalConfiguration().get("AZURE_ACCOUNT_ENDPOINT"))
    .instanceId(Configuration.getGlobalConfiguration().get("AZURE_INSTANCE_ID"))
    .credential(new DefaultAzureCredentialBuilder().build())
    .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
    .buildAsyncClient();
```

## List Devices

In the sample `ListDevicesSample`, it uses `DeviceManagementAsyncClient` to call `listDevices`. 
It returns a `PagedFlux`, you can call `response.toStream().count()` to get the device count.

