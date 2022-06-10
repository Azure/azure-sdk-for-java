# Device Update for IoT Hub Samples

Samples `ListUpdatesSample`, `GetUpdateSample` and `GetDeviceSample` are hand-written samples that can be run directly 
when you configured correct environment variables.

## Running the samples

To run the samples, you need to compile console Samples project. The executable retrieves arguments from the following
environment settings:

| Environment variable         | Description          |
|------------------------------|----------------------|
| AZURE_TENANT_ID              | AAD tenant id        |
| AZURE_SUBSCRIPTION_ID        | AAD subscription id  |
| AZURE_CLIENT_ID              | AAD client id        |
| AZURE_CLIENT_SECRET          | AAD client secret    |
| AZURE_ACCOUNT_ENDPOINT       | ADU account endpoint |
| AZURE_INSTANCE_ID            | ADU instance id      |
|                              |                      |
| DEVICEUPDATE_UPDATE_PROVIDER | fabrikam             |
| DEVICEUPDATE_UPDATE_NAME     | vacuum               |
| DEVICEUPDATE_UPDATE_VERSION  | 2022.401.504.6       |
| DEVICEUPDATE_DEVICE          | dpokluda-test        |

## Creating Client

You need to use `DeviceUpdateClientBuilder`, resp. `DeviceManagementClientBuilder`, to create a valid instance of 
`DeviceUpdateClient`, resp. `DeviceManagementClient`. There are also async clients available - 
`DeviceUpdateAsyncClient`, resp. `DeviceManagementAsyncClient`.

Create new instance of `DeviceUpdateAsyncClient`:
```java com.azure.iot.deviceupdate.DeviceUpdateAsyncClient.instantiate
DeviceUpdateAsyncClient client = new DeviceUpdateClientBuilder()
    .endpoint(Configuration.getGlobalConfiguration().get("AZURE_ACCOUNT_ENDPOINT"))
    .instanceId(Configuration.getGlobalConfiguration().get("AZURE_INSTANCE_ID"))
    .credential(new DefaultAzureCredentialBuilder().build())
    .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
    .buildAsyncClient();
```

To create new instance of `DeviceManagementAsyncClient` use the following code:
```java com.azure.iot.deviceupdate.DeviceManagementAsyncClient.instantiate
DeviceManagementAsyncClient client = new DeviceManagementClientBuilder()
    .endpoint(Configuration.getGlobalConfiguration().get("AZURE_ACCOUNT_ENDPOINT"))
    .instanceId(Configuration.getGlobalConfiguration().get("AZURE_INSTANCE_ID"))
    .credential(new DefaultAzureCredentialBuilder().build())
    .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
    .buildAsyncClient();
```

## List device updates

In the sample `ListUpdatesSample`, it uses `DeviceUpdateAsyncClient` to call `listVersions` method.
It returns a `PagedFlux`, you can call `response.toStream().count()` to get the device count.

## Get device update

In the sample `GetUpdateSample`, it uses `DeviceUpdateAsyncClient` to call `getUpdateWithResponse` method.

## Get Device

In the sample `GetDeviceSample`, it uses `DeviceManagementAsyncClient` to call `getDeviceWithResponse`. 

