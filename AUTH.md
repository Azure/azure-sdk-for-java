# Authentication in Azure Management Libraries for Java

To use the APIs in the Azure Management Libraries for Java, as the first step you need to 
create an authenticated client. There are several possible approaches to authentication. This document illustrates a couple of the simpler ones

## Using an authentication file

> ​:warning: Note, file-based authentication is an experimental feature that may or may not be available in later releases. The file format it relies on is subject to change as well.

To create an authenticated Azure client:

```java
Azure azure = Azure.authenticate(new File("my.azureauth")).withDefaultSubscription();
```

The authentication file, referenced as "my.azureauth" in the example above, contains the information of a service principal. You can generate this file using [Azure CLI 2.0](https://github.com/Azure/azure-cli) through the following command. Make sure you selected your subscription by `az account set --subscription <name or id>` and you have the privileges to create service principals.

```bash
az ad sp create-for-rbac --sdk-auth > my.azureauth
```

If you don't have Azure CLI installed, you can also do this in the [cloud shell](https://docs.microsoft.com/en-us/azure/cloud-shell/quickstart). Alternatively, you can login to Java SDK through other ways of authentication and create an auth file by following [this sample](https://github.com/Azure/azure-sdk-for-java/blob/master/azure-samples/src/main/java/com/microsoft/azure/management/graphrbac/samples/ManageServicePrincipal.java). For detailed explanations of the content in this auth file, or directions to create the auth file manually, please see [Auth file formats](#auth-file-formats).

## Using `ApplicationTokenCredentials`

Similarly to the [file-based approach](#using-an-authentication-file), this method requires a [service principal registration](#creating-a-service-principal-in-azure), but instead of storing the credentials in a local file, the required inputs can be supplied directly via an instance of the `ApplicationTokenCredentials` class:

```
ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
        client, tenant, key, AzureEnvironment.AZURE);
Azure azure = Azure.authenticate(credentials).withSubscription(subscriptionId);
```

or

```
ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
        client, tenant, pfxCertificatePath, password, AzureEnvironment.AZURE);
Azure azure = Azure.authenticate(credentials).withSubscription(subscriptionId);
```

where `client`, `tenant`, `subscriptionId`, and `key` or `pfxCertificatePath` and `password` are strings with the required pieces of information about your service principal and subscription. The last parameter, `AzureEnvironment.AZURE` represents the Azure worldwide public cloud. You can use a different value out of the currently supported alternatives in the `AzureEnvironment` enum.

## Using credentials from Azure CLI Automatically (Preview)

If you have [Azure CLI](https://github.com/Azure/azure-cli) (>=2.0) installed and authenticated on your machine, the SDK client is able to use the current account and subscription Azure CLI is logged in.

Run `az login` to authenticate to Azure CLI and `az account set --subscription <subscription Id>` in your terminal to select the subscription to use. Initialize the Azure client as following:

```
Azure azure = Azure.authenticate(AzureCliCredentials.create()).withDefaultSubscription();
```

And you are good to go.

If Azure CLI is authenticated as a user, tokens acquired in Azure CLI expire after 90 days. You will be prompted to re-authenticate. If Azure CLI is authenticated with a service principal, it will never expire until the service principal credential expires.

## Auth file formats

Prior to this release, we've been using Java properties file format containing the following information:

```
subscription=########-####-####-####-############
client=########-####-####-####-############
tenant=########-####-####-####-############
key=XXXXXXXXXXXXXXXX
managementURI=https\://management.core.windows.net/
baseURL=https\://management.azure.com/
authURL=https\://login.windows.net/
graphURL=https\://graph.windows.net/
```

or certificate based format (password is optional if using .pem certificate):

```
subscription=########-####-####-####-############
client=########-####-####-####-############
tenant=########-####-####-####-############
certificate=<path to certificate file>
certificatePassword=XXXXXXXXXXXXXXXX
managementURI=https\://management.core.windows.net/
baseURL=https\://management.azure.com/
authURL=https\://login.windows.net/
graphURL=https\://graph.windows.net/
```

This format is still supported for backward compatibility at least until 2.0 release of the SDK. Meanwhile, the new JSON based auth file format is introduced and supported across the Java SDK, the fluent .NET SDK and the Python SDK (more coming!):

```json
{
  "clientId": "b52dd125-9272-4b21-9862-0be667bdf6dc",
  "clientSecret": "ebc6e170-72b2-4b6f-9de2-99410964d2d0",
  "subscriptionId": "ffa52f27-be12-4cad-b1ea-c2c241b6cceb",
  "tenantId": "72f988bf-86f1-41af-91ab-2d7cd011db47",
  "activeDirectoryEndpointUrl": "https://login.microsoftonline.com",
  "resourceManagerEndpointUrl": "https://management.azure.com/",
  "activeDirectoryGraphResourceId": "https://graph.windows.net/",
  "sqlManagementEndpointUrl": "https://management.core.windows.net:8443/",
  "galleryEndpointUrl": "https://gallery.azure.com/",
  "managementEndpointUrl": "https://management.core.windows.net/"
}
```

or certificate based format (password is optional if using .pem certificate):

```json
{
  "clientId": "b52dd125-9272-4b21-9862-0be667bdf6dc",
  "clientCertificate": "<path to certificate file>",
  "clientCertificatePassword": "XXXXXXXXXXXXXXXX",
  "subscriptionId": "ffa52f27-be12-4cad-b1ea-c2c241b6cceb",
  "tenantId": "72f988bf-86f1-41af-91ab-2d7cd011db47",
  "activeDirectoryEndpointUrl": "https://login.microsoftonline.com",
  "resourceManagerEndpointUrl": "https://management.azure.com/",
  "activeDirectoryGraphResourceId": "https://graph.windows.net/",
  "sqlManagementEndpointUrl": "https://management.core.windows.net:8443/",
  "galleryEndpointUrl": "https://gallery.azure.com/",
  "managementEndpointUrl": "https://management.core.windows.net/"
}
```

If you are using the default Azure public cloud, you can leave all the URL fields blank. 

The `clientId` and `tenantId` are from your service principal registration. If your service principal uses key authentication, `clientSecret` is the password credential added to the service principal. If your service principal uses certificate authentication, `clientCertificate` is the path to your pem or pfx certificate. In the case of a pfx certificate, you also need to provide the `clientCertificatePassword`.

This approach enables unattended authentication for your application (i.e. no interactive user login, no token management needed).  The `subscription` represents the subscription ID you want to use as the default subscription. The remaining URIs and URLs represent the end points for the needed Azure services, defaulted to Azure public cloud.


