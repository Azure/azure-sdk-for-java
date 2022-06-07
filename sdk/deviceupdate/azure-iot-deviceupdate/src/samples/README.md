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

You need to use AzureDeviceUpdateClientBuilder to create a proper valid AzureDeviceUpdateClient. This main client will
let you access the other clients - Updates and Management. 
