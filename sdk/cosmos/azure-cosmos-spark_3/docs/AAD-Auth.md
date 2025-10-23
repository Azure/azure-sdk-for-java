# Using AAD authentication in Apache Spark with the Azure Cosmos DB Spark connector



## Introduction

Azure Cosmos DB provides two distinct RBAC permission models for Data-plane (Document read, create, replace, upsert, patch, delete, query) operations and management operations (creating, modifying or deleting databases or containers). The Azure Cosmos DB Spark Connector spans both of these models, because it allows management operations via the Spark Catalog API as well as data-plane operations. This means, that when you use any of the Spark Catalog APIs or SQL statements to create, modify or delete TABLES or DATABASES, you need to provide sufficient permissions for the AAD / Microsoft Entra ID identity to both the [data-plane RBAC](https://learn.microsoft.com/azure/cosmos-db/nosql/security/how-to-grant-data-plane-role-based-access) as well as the [control-plane RBAC](https://learn.microsoft.com/azure/cosmos-db/nosql/security/how-to-grant-control-plane-role-based-access). If you do not create, modify or delete TABLES or DATABASES from Spark via the Spark-Catalog APIs, you can skip the latter.

The Azure Cosmos DB Spark connector provides out-of-the box support for AAD / Microsoft Entra ID authentication via system-managed identities and ServicePrincipals via password or certificate assuming the Spark environment used also supports those. Further details can be found below in the [Out-of-the-box Managed Identity support](#out-of-the-box-managed-identity-support) and [Out-of-the-box Service Principal support](#out-of-the-box-service-principal-support) sections. If none of the out-of-the-box authentication mechanisms work in the Spark environment used, a custom token resolver can be provided - see [Using a custom Token Provider](#using-a-custom-token-provider)

Please also see the [end-to-end sample for a custom `AccountDataResolver` implementation](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/cosmos/azure-cosmos-spark-account-data-resolver-sample) for more details.

## Out-of-the-box Managed Identity support

This authentication option allows using a system managed identity via simple config entries passed to the `cosmos.oltp` or `cosmos.oltp.changefeed` `DataSource`s or to the Spark catalog APIs.

*If you still have a choice regarding the Spark environment, our recommendation would be to use Azure Databricks when you want to use system managed identities. Azure Databricks provides a smooth out-of-the box experience for managed identities - not just when using the Azure Cosmos DB Spark connector, but also for other Connectors.*

### Prerequisites  

To enable managed identity support out-of-the-box, the Spark environment needs to allow access to the MSI (Managed System Identity) endpoint via the [Azure IMDS - Instance Metadata Service](https://learn.microsoft.com/entra/identity/managed-identities-azure-resources/how-to-use-vm-token) endpoint or a redirected endpoint following the same protocol (like in Azure AppServices). Azure Databricks supports this mode with recently created workspaces. Azure Synapse and Azure HDInsights do not support managed identities out-of-the-box currently - see [Using a custom Token Provider](#using-a-custom-token-provider) to use custom access tokens there.



### Configuration options

| Config Property Name                     | Default     | Description                                                                                                                                                                                                                                                       |
|:-----------------------------------------|:------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `spark.cosmos.auth.type`                 | `MasterKey` | Set this value to `ManagedIdentity` to enable the out-of-the-box Managed Identity support                                                                                                                                                                         |
| `spark.cosmos.auth.aad.clientId`         | None        | The client id of the managed identity. This parameter is optional and only useful when multiple system managed identities would be available.                                                                                                                     |
| `spark.cosmos.auth.aad.resourceId`       | None        | The resource id of the managed identity. This parameter is optional and only useful when multiple system managed identities would be available.                                                                                                                   |
| `spark.cosmos.account.subscriptionId`    | None        | The `SubscriptionId` of the Azure Cosmos DB account resource specified under `spark.cosmos.accountEndpoint`. This parameter is required for all management operations when using AAD / Microsoft Entra ID authentication.                                         |
| `spark.cosmos.account.tenantId`          | None        | The `AAD TenantId` of the Azure Cosmos DB account resource specified under `spark.cosmos.accountEndpoint`. This parameter is required for all management operations when using AAD / Microsoft Entra ID authentication.                                           |
| `spark.cosmos.account.resourceGroupName` | None        | The  simple resource group name (not the full qualified one) of the Azure Cosmos DB account resource specified under `spark.cosmos.accountEndpoint`. This parameter is required for all management operations when using AAD / Microsoft Entra ID authentication. |

#### Non-public clouds
For non-public clouds the `spark.cosmos.account.azureEnvironment` config value need to be set to `Custom`and the config entries `spark.cosmos.account.azureEnvironment.management` and `spark.cosmos.account.azureEnvironment.aad` have to be specified to the correct values for the non-public cloud.

| Config Property Name                               | Default | Description                                                                                                                                               |
|:---------------------------------------------------|:--------|:----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `spark.cosmos.account.azureEnvironment.management` | None    | The Uri of the ARM (Resource Manager) endpoint in the custom cloud - e.g. the corresponding value to `https://management.azure.com/` in the public cloud. |
| `spark.cosmos.account.azureEnvironment.aad`        | None    | The Uri of the AAD endpoint in the custom cloud - e.g. the corresponding value to `https://login.microsoftonline.com/` in the public cloud.               |

#### Environment variables or system properties

By default the Azure SDK for Java uses the [Azure IMDS - Instance Metadata Service](https://learn.microsoft.com/entra/identity/managed-identities-azure-resources/how-to-use-vm-token) to retrieve MSI tokens. The default endpoint is `http://169.254.169.254/metadata/identity/oauth2/token`. In some Spark environment or Compute environments access to the IMDS endpoint is disallowed (for example because it can become dangerous in multi-tenant environments because every tenant would be allowed to use the same identity if not disallowed). Some Compute environments like Azure AppServices provide an alternative endpoint for MSI authentication which uses the same wire protocol - so, overriding the MSI endpoint is sufficient to make this work safely.
The MSI endpoint can be overridden via the `AZURE_POD_IDENTITY_TOKEN_URL` environment variable or JVM system property. If both are presented the JVM system property trumps the environment variable. 



## Out-of-the-box Service Principal support

This authentication option allows using a `ServicePrincipal` with either password or client-certificate via simple config entries passed to the `cosmos.oltp` or `cosmos.oltp.changefeed` `DataSource`s or to the Spark catalog APIs.



### Configuration options

| Config Property Name                        | Default     | Description                                                                                                                                                                                                                                                                                                                            |
|:--------------------------------------------|:------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `spark.cosmos.auth.type`                    | `MasterKey` | Set this value to `ServicePrincipal` to enable the out-of-the-box SPN support                                                                                                                                                                                                                                                          |
| `spark.cosmos.auth.aad.clientId`            | None        | The application id of the `SPN/ServicePrincipal`. This parameter is  required.                                                                                                                                                                                                                                                         |
| `spark.cosmos.auth.aad.clientSecret`        | None        | The password/secret used to authenticate to AAD / Microsoft Entra when retrieving access tokens for the `SPN/ServicePrincipal`. This property is required when using passwords for authentication - when using client certificates, use `spark.cosmos.auth.aad.clientCertPemBase64` instead.                                           |
| `spark.cosmos.auth.aad.clientCertPemBase64` | None        | The client certificate (with private key, Base64 encoded PEM format) used to authenticate to AAD / Microsoft Entra when retrieving access tokens for the `SPN/ServicePrincipal`. This property is required when using client certificates for authentication - when using passwords, use `spark.cosmos.auth.aad.clientSecret` instead. |
| `spark.cosmos.auth.aad.clientCertSendChain` | `false`     | *Only relevant for first-party MSFT-internal users.* If `true`it will send the entire trust chain to AAD / Microsoft Entra when retrieving access tokens to simplify safe certificate rotations.                                                                                                                                       |
| `spark.cosmos.account.subscriptionId`       | None        | The `SubscriptionId` of the Azure Cosmos DB account resource specified under `spark.cosmos.accountEndpoint`. This parameter is required for all management operations when using AAD / Microsoft Entra ID authentication.                                                                                                              |
| `spark.cosmos.account.tenantId`             | None        | The `AAD TenantId` of the Azure Cosmos DB account resource specified under `spark.cosmos.accountEndpoint`. This parameter is required for all management operations when using AAD / Microsoft Entra ID authentication.                                                                                                                |
| `spark.cosmos.account.resourceGroupName`    | None        | The  simple resource group name (not the full qualified one) of the Azure Cosmos DB account resource specified under `spark.cosmos.accountEndpoint`. This parameter is required for all management operations when using AAD / Microsoft Entra ID authentication.                                                                      |


#### Environment variables or system properties

By default the Azure SDK for Java uses the [Azure IMDS - Instance Metadata Service](https://learn.microsoft.com/entra/identity/managed-identities-azure-resources/how-to-use-vm-token) to retrieve MSI tokens. The default endpoint is `http://169.254.169.254/metadata/identity/oauth2/token`. In some Spark environment or Compute environments access to the IMDS endpoint is disallowed (for example because it can become dangerous in multi-tenant environments because every tenant would be allowed to use the same identity if not disallowed). Some Compute environments like Azure AppServices provide an alternative endpoint for MSI authentication which uses the same wire protocol - so, overriding the MSI endpoint is sufficient to make this work safely.
The MSI endpoint can be overridden via the `AZURE_POD_IDENTITY_TOKEN_URL` environment variable or JVM system property. If both are presented the JVM system property trumps the environment variable. 



## Using a custom Token Provider

Due to the fact that support for system managed identities and token/secret APIs varies widely between different Spark environments, the Azure Cosmos DB Spark connector provides a service interface [`com.azure.cosmos.spark.AccountDataResolver`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-spark_3/src/main/scala/com/azure/cosmos/spark/AccountDataResolver.scala), that can be used to customize retrieval of access tokens.

### Configuration options

| Config Property Name                          | Default     | Description                                                                                                                                                                                                                                                        |
|:----------------------------------------------|:------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `spark.cosmos.auth.type`                      | `MasterKey` | Set this value to `AccessToken` to enable AAD / Microsoft Entra ID authentication via access tokens from your custom `AccountDataResolver` implementation.                                                                                                         |
| `spark.cosmos.accountDataResolverServiceName` | None        | The FQDN (full qualified domain name) of your custom `AccountDataResolver` implementation. FQDN means `<package/namespace>.<ClassName>`                                                                                                                            |
| `spark.cosmos.account.subscriptionId`         | None        | The `SubscriptionId` of the Azure Cosmos DB account resource specified under `spark.cosmos.accountEndpoint`. This parameter is required for all management operations when using AAD / Microsoft Entra ID authentication.                                          |
| `spark.cosmos.account.tenantId`               | None        | The `AAD TenantId` of the Azure Cosmos DB account resource specified under `spark.cosmos.accountEndpoint`. This parameter is required for all management operations when using AAD / Microsoft Entra ID authentication.                                            |
| `spark.cosmos.account.resourceGroupName`      | None        | The  simple resource group name (not the full qualified one) of the Azure Cosmos DB account resource specified under `spark.cosmos.accountEndpoint`. This parameter is required for all management operations when using AAD / Microsoft Entra ID  authentication. |
| `your.own.custom.property`                    |             | You can add and use custom properties for the configuration of your custom `AccountDataResolver` implementation.                                                                                                                                                   |

### Implementation of a custom `AccountDataResolver`

The latest and complete API documentation for the service interface can be found in the source code for the [`com.azure.cosmos.spark.AccountDataResolver`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-spark_3/src/main/scala/com/azure/cosmos/spark/AccountDataResolver.scala) file. In general, there are two methods that a custom `AccountDataResolver` implementation needs to implement:

- `getAccountDataConfig`: The purpose of this method is to allow applying configuration changes from the custom `AccountDataResolver`. In Azure Synapse a custom `AccountDataResolver` for example is used to apply the key-based auth configuration for a `Linked Service`. This method could also be used to map other configuration providers (environment variables, separate config files etc.) to the Spark configuration map. Last-but-not-least in many custom 'AccountDataResolver' implementations the configuration entry `spark.cosmos.auth.type` would be set to `AccessToken` to ensure AAD / Microsoft Entra ID authentication via a custom access token provider is used. 
- `getAccessTokenProvider`: The purpose of this method is to allow the custom `AccountDataResolver` implementation to return a token minting function depending on the configuration map chosen. For performance reasons it is critical that each set of relevant configuration entries only produces a singleton Function instance - what set of configuration entries is relevant to you token minting function depends on your custom implementation. A sample on how to achieve this singleton-pattern based off of the relevant config subset can be found [here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos-spark-account-data-resolver-sample/src/main/scala/com/azure/cosmos/spark/samples/ManagedIdentityAccountDataResolver.scala). In general, any configuration entry reflecting the AAD/Microsoft Entra-Login Uri or the actual token minting approach (for example `TokenCredential` you are using) should be considered `relevant`. ***NOTE: When the token-minting function is invoked, the `List[String]` input parameter contains a list of the requested audiences/scopes. In most cases you probably want to honor these audiences and pass them to the `TokenRequestContext`when creating the access token, but you have the option to also filter/modify audiences when needed.***

#### Project structure
A sample project structure for a jar containing a custom `AccountDataResolver` would look like below. See the [end-to-end sample for a custom `AccountDataResolver` implementation](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/cosmos/azure-cosmos-spark-account-data-resolver-sample) for more details.

```
├───src
│   └───main
│       ├───java
│       |   └───com
│       |       └───your-company
│       |       |   └───your-packagename
│                       └───YourJavaResolver.java
│       ├───resources
│       │   └───META-INF
│       │       └───services
|       |           └───com.azure.cosmos.spark.AccountDataResolver
│       └───scala
│       |   └───com
│       |       └───your-company
│       |       |   └───your-packagename
│                       └───YourScalaResolver.scala
└───pom.xml
```

### Using the custom `AccountDataResolver` 

The Azure Cosmos DB Spark connector treats the `com.azure.cosmos.spark.AccountDataResolver` interface as a classical service interface. Meaning, reflection will be used to find implementations of this interface on the class-path that have also announced the service interface implementation in a corresponding `src/main/resources/META-INF/services/com.azure.cosmos.spark.AccountDataResolver` file. See the [Java Service Provider Interfaces](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) for more information around service interfaces in Java.

This means, to make sure the Spark job can find and use your custom `AccountDataResolver` implementation you have to ensure, that:

- The `jar`with your custom implementation is available on the class-path
- You set the `spark.cosmos.accountDataResolverServiceName` config entry to point to your custom `AccountDataResolver` implementation
- The `jar` containing the implementation is correctly announcing the service in a `META-INF/services/com.azure.cosmos.spark.AccountDataResolver` file.

## Configuration options in Azure air gapped (non-public) clouds.

Currently management operations (creating, modifying or deleting databases or containers via Spark-Catalog API) are not supported in non-public Azure clouds. Support will be added in CY2025.

## Out of scope

Please follow-up with any questions around usage of the Spark environment specific APIs to retrieve secrets in Azure Databricks, Azure Synapse or Azure HDInsights with the respective owners. The Azure Cosmos DB connector is completely decoupled from these token APIs and as such we can't help there.

The same is true for questions regarding the conversion of client certificates from PFX into PEM. The Azure SDK for Java uses the PEM format - and as such the configuration for client certificates expects the certificate with private key in Base64 encoded PEM format. There are usually Token/secret APIs that allow getting the client certificate from Azure KeyVault in PEM format - or to convert them. A sample for converting the certificate from PFX to PEM format is below - but this is provided without any support simply for convenience. Please look at the token/secret APIs of your Spark environment first and only rely on this conversion when there are really no APIs provided that allow accessing the client certificate in PEM format.

#### Scala sample for converting PFX to base64-encoded PEM 

```scala
def getBase64EncodedPemCertificateFromPfx(pfxBase64:String, pfxPassword: String = "") : String = {
    val secret = pfxBase64
    val retBytes = Utilities.decodeBase64StringToBytes(secret)
    val is = new ByteArrayInputStream(retBytes)
    val clientCertificate = ClientCredentialFactory.createFromCertificate(is, pfxPassword)
    val privateKey = Utilities.encodeBase64String(clientCertificate.privateKey().getEncoded())
    val privateKeySection = s"-----BEGIN PRIVATE KEY-----$privateKey\n-----END PRIVATE KEY-----\n"

    val sb = StringBuilder.newBuilder
    sb.append(privateKeySection)

    val list = clientCertificate.getEncodedPublicKeyCertificateChain
    if (list != null) {
      for(cert <- list.asScala) {
        val certSection = s"-----BEGIN CERTIFICATE-----\n$cert\n-----END CERTIFICATE-----\n"
        sb.append(certSection)
      }
    }

    Utilities.encodeBase64String(sb.toString())
}
```



#### Python sample for converting PFX to base64-encoded PEM

```python
import base64
import cryptography
from cryptography.hazmat.primitives.serialization import pkcs12
from cryptography.hazmat.primitives import serialization

# Get the secret. This is a base64-encoded pcks12.
secret64 = mssparkutils.credentials.getSecret("xxx", "yyy", "zzz")

# Decode to bytes.
secret64Bytes = bytes(secret64, "utf-8")
secretBytes = base64.b64decode(secret64)

# Load as a certificate: https://cryptography.io/en/latest/hazmat/primitives/asymmetric/serialization/#pkcs12.
# Return value is a tuple (private key, public key, certificate chain).
# private key: RSA PrivateKey: https://cryptography.io/en/latest/hazmat/primitives/asymmetric/rsa/#cryptography.hazmat.primitives.asymmetric.rsa.RSAPrivateKey.
# public key: x509 Certificate: https://cryptography.io/en/latest/x509/reference/#cryptography.x509.Certificate.
# certificate chain: list of X509 Certificate. Note that this does not include the leaf cert in the public key.
certificate = pkcs12.load_key_and_certificates(secretBytes, None)

private_key = certificate[0]
public_key = certificate[1]
certificate_chain = certificate[2]

# Convert the private key to PEM.
private_pem_bytes = private_key.private_bytes(serialization.Encoding.PEM, serialization.PrivateFormat.PKCS8, serialization.NoEncryption())

# Begin constructing the full PEM.
# This adds BEGIN/END PRIVATE KEY.
pem = private_pem_bytes.decode()

# Convert the public key as the first cert in the chain.
public_pem_bytes = public_key.public_bytes(serialization.Encoding.PEM)
public_pem = public_pem_bytes.decode()

# Add to the list.
# This adds BEGIN/END CERTIFICATE.
pem += public_pem

# Add each certificate in the parent chain.
for cert in certificate_chain:
    public_pem_bytes = cert.public_bytes(serialization.Encoding.PEM)
    public_pem = public_pem_bytes.decode()
    pem += public_pem

# Encode the complete PEM as base64.
pem_bytes = bytes(pem, "utf-8")
pem64 = base64.b64encode(pem_bytes).decode()

dfStream = spark.readStream\
    .format("cosmos.oltp.changeFeed")\
    .option("spark.cosmos.accountEndpoint", "<ServiceEndpoint>")\
    .option("spark.cosmos.auth.type", "ServicePrincipal")\
    .option("spark.cosmos.account.subscriptionId", "<SubscriptionId>")\
    .option("spark.cosmos.account.tenantId", "<TenantId>")\
    .option("spark.cosmos.account.resourceGroupName", "<SimpleresourceGroupName>")\
    .option("spark.cosmos.auth.aad.clientId", "<SPN-ClientId>")\
    .option("spark.cosmos.auth.aad.clientCertPemBase64", pem64)\
    .option("spark.cosmos.auth.aad.clientCertSendChain", "true")\
    .option("spark.cosmos.database", "Application")\
    .option("spark.cosmos.container", "UserData")\
    .option("spark.cosmos.enforceNativeTransport", "true")\
    .load()
```

