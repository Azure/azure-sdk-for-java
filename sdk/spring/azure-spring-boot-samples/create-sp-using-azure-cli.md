### Sign into Azure and set your subscription

First, use the following steps to authenticate using the Azure CLI.

1. Optionally, log out and delete some authentication files to remove any lingering credentials:

   ```azurecli
   az logout
   rm ~/.azure/accessTokens.json
   rm ~/.azure/azureProfile.json
   ```

1. Sign into your Azure account by using the Azure CLI:

   ```azurecli
   az login
   ```

   Follow the instructions to complete the sign-in process.

1. List your subscriptions:

   ```azurecli
   az account list
   ```

   Azure will return a list of your subscriptions. Copy the `id` for the subscription that you want to use; for example:

   ```json
   [
     {
       "cloudName": "AzureCloud",
       "id": "ssssssss-ssss-ssss-ssss-ssssssssssss",
       "name": "Converted Windows Azure MSDN - Visual Studio Ultimate",
       "state": "Enabled",
       "tenantId": "tttttttt-tttt-tttt-tttt-tttttttttttt",
       "user": {
         "name": "contoso@microsoft.com",
         "type": "user"
       }
     }
   ]
   ```

1. Specify the GUID for the subscription you want to use with Azure; for example:

   ```azurecli
   az account set -s ssssssss-ssss-ssss-ssss-ssssssssssss
   ```

### Create a service principal for use in by your app

Azure AD *service principals* provide access to Azure resources within your subscription. You can think of a service principal as a user identity for a service.  "Service" is any application, service, or platform, including the sample app built in this tutorial, that needs to access Azure resources. You can configure a service principal with access rights scoped only to those resources you specify. Then, configure your application or service to use the service principal's credentials to access those resources.

Create a service principal with this command.

```azurecli
az ad sp create-for-rbac --name contososp
```

The value of the `name` option must be unique within your subscription.  Save aside the values returned from the command for use later in the tutorial.  The return JSON will look something like the following.

```json
{
  "appId": "sample-app-id",
  "displayName": "ejbcontososp",
  "name": "http://ejbcontososp",
  "password": "sample-password",
  "tenant": "sample-tenant"
}
```