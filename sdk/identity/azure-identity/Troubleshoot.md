## Table of contents
  - [Troubleshooting Service Principal Authentication Issues](#troubleshooting-service-principal-authentication-issues)
  - [Troubleshooting Mananged Identity Authenticaiton Issues](#troubleshooting-mananged-identity-authenticaiton-issues)
  - [Troubleshooting Visual Studio Code Authenticaiton Issues](#troubleshooting-visual-studio-code-authenticaiton-issues)
  - [Troubleshooting Azure CLI Authenticaiton Issues](#troubleshooting-azure-cli-authenticaiton-issues)
  - [Troubleshooting Azure Powershell Authenticaiton Issues](#troubleshooting-azure-powershell-authenticaiton-issues)

## Troubleshooting Service Principal Authentication Issues.

### Illegal/Invalid Argument Issues

#### Client Id
 
The Client Id is the application Id of the registered application / service principal in Azure Active Directory.
It is a required parameter for `ClientSecretCredential` and `ClientCertificateCredential`. If you have already created your service principal
then you can retrieve the client/app id by following the instructions [here](https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal#get-tenant-and-app-id-values-for-signing-in).

#### Tenant Id
The tenant id is te Global Unique Identifier (GUID) that identifies your organization. It is a required parameter for
`ClientSecretCredential` and `ClientCertificateCredential`. If you have already created your service principal
then you can retrieve the client/app id by following the instructions [here](https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal#get-tenant-and-app-id-values-for-signing-in).

#### Client Secret
The client secret is the secret string that the application uses to prove its identity when requesting a token, this can also can be referred to as an application password.
If you have already created a servie principal you can follow the instructions [here](https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal#option-2-create-a-new-application-secret) to get the client secret for your application.

If you're looking to create a new service principal and would like to use that, then follow tne instructions [here](https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-service-principal-auth#create-a-service-principal-with-the-azure-cli) to create a new service principal.


## Troubleshooting Mananged Identity Authenticaiton Issues

### Credential Unavailable

#### Connection Timed Out / Connection could not be established / Target Environment could not be determined.
The Managed Identity credential runs only on Azure Hosted machines/servers. So ensure that you are running your application on an
Azure Hosted resource. Currently Azure Identity SDK supports [Managed Identity Authentication]((https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview)) 
in the below listed Azure Services, so ensure you're running your application on one of these resources and have enabled the Managed Identity on
them by following the instructions at their configuration links below.

Azure Service | Managed Identity Configuration
--- | --- |
[Azure Virtual Machines](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/how-to-use-vm-token) | [Configuration Instructions](https://docs.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/qs-configure-portal-windows-vm)
[Azure App Service](https://docs.microsoft.com/azure/app-service/overview-managed-identity?tabs=java) | [Configuration Instructions](https://docs.microsoft.com/en-us/azure/app-service/overview-managed-identity?tabs=java)
[Azure Kubernetes Service](https://docs.microsoft.com/azure/aks/use-managed-identity) | [Configuration Instructions](https://docs.microsoft.com/azure/aks/use-managed-identity)
[Azure Cloud Shell](https://docs.microsoft.com/azure/cloud-shell/msi-authorization) |  |
[Azure Arc](https://docs.microsoft.com/azure/azure-arc/servers/managed-identity-authentication) | [Configuration Instructions](https://docs.microsoft.com/en-us/azure/azure-arc/servers/security-overview#using-a-managed-identity-with-arc-enabled-servers)
[Azure Service Fabric](https://docs.microsoft.com/azure/service-fabric/concepts-managed-identity) | [Configuration Instructions](https://docs.microsoft.com/en-us/azure/service-fabric/configure-existing-cluster-enable-managed-identity-token-service)


## Troubleshooting Visual Studio Code Authenticaiton Issues

### Credential Unavailable

#### Failed To Read VS Code Credentials / Authenticate via Azure Tools plugin in VS Code.
THe `VS Code Credential` failed to read the credential details from the cache. 

The Visual Studio Code authentication is handled by an integration with the Azure Account extension.
To use this form of authentication, ensure that you have installed the Azure Account extension,
then use View > Command Palette to execute the Azure: Sign In command. This command opens a browser window and displays a page that allows you 
to sign in to Azure. After you've completed the login process, you can close the browser as directed. Running your application 
(either in the debugger or anywhere on the development machine) will use the credential from your sign-in.

If you already had the Azure Account extension installed and had logged in to your account. Then try logging out and logging in again, as
that will re-populate the cache on the disk and potentially mitigate the error you're getting.

#### Msal Interaction Required Error
THe `VS Code Credential` was able to read the cached credentials from the cache but the cached token is likely expired.
Log into the Azure Account extension by via View > Command Palette to execute the Azure: Sign In command in the VS Code IDE.

#### ADFS Tenant Not Supported
The ADFS Tenants are not supported via the Azure Account extension in VS Code currently. 
The supported clouds are:

Azure Cloud | Cloud Authority Host
--- | --- | 
AZURE PUBLIC CLOUD | https://login.microsoftonline.com/
AZURE GERMANY | https://login.microsoftonline.de/
AZURE CHINA | https://login.chinacloudapi.cn/
AZURE GOVERNMENT | https://login.microsoftonline.us/


## Troubleshooting Azure CLI Authenticaiton Issues

### Credential Unavailable

#### Azure CLI Not Installed.
THe `Azure CLI Credential` failed to execute as Azure CLI command line tool is not installed. 
To use Azure CLI credential, the Azure CLI needs to be installed, please follow the instructions [here](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli)
to install it for your platform and then try running the credential again.

#### Azure account not logged in.
The `Azure CLI Credential` utilizes the current logged in Azure user in Azure CLI to fetch an access token. 
You need to login to your account in Azure CLI via `az login` command. You can further read instructions to [Sign in with Azure CLI](https://docs.microsoft.com/en-us/cli/azure/authenticate-azure-cli).
Once logged in try running the credential again.

### Illegal State
#### Safe Working Directory Not Located.
The `Azure CLI Credential` was not able to locoate a value for System Environment property `SystemRoot` to execute in.
Please ensure the `SystemRoot` environment variable is configured to a safe working directory and then try running the credential again.


## Troubleshooting Azure Powershell Authenticaiton Issues

### Credential Unavailable

#### Powershell not installed.

The `Azure Powershell Credential` utilizes the locally installed `Powershell` command line tool to fetch an access token. Please ensure it is installed on your platform by following the instructions [here](https://docs.microsoft.com/en-us/powershell/scripting/install/installing-powershell?view=powershell-7.1) and then run the credential again.

#### Azure Az Moudle Not Installed.
The `Azure Powershell Credential` failed to execute as Azure az module is not installed. 
To use Azure Powershell credential, the Azure az module needs to be installed, please follow the instructions [here](https://docs.microsoft.com/en-us/powershell/azure/install-az-ps?view=azps-6.3.0)
to install it for your platform and then try running the credential again.

#### Azure account not logged in.
The `Azure Powershell Credential` utilizes the current logged in Azure user in Azure Powershell to fetch an access token. 
You need to login to your account in Azure Powershell via `Connect-AzAccount` command. You can further read instructions to [Sign in with Azure Powershell](https://docs.microsoft.com/en-us/powershell/azure/authenticate-azureps?view=azps-6.3.0).
Once logged in try running the credential again.


#### Deserialization error.
The `Azure Powershell Credential` was able to retrieve a response from the Azure Powershell when attempting to get an access token but failed 
to parse that response.
In your local powershell window, run the following command to ensure that Azure Powerhsell is returning an access token in correct format.

```pwsh
Get-AzAccessToken -ResourceUrl "<Scopes-Url>"
```
In the event above command is not working properly, follow the instructions to resolve the Azure Powershell issue being faced and then try running the credential again.
