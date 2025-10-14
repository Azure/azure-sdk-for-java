# Azure Key Vault JCA client library for Java
The JCA Provider for Azure Key Vault is a Java Cryptography Architecture provider for certificates in
Azure Key Vault. It is built on four principles:

1. Must be extremely thin to run within a JVM.
2. Must not introduce any library version conflicts with Java app code dependencies.
3. Must not introduce any class loader hierarchy conflicts with Java app code dependencies.
4. Must be ready for "never trust, always verify and credential-free" Zero Trust environments.

[Source code] | [API reference documentation] | [Product documentation] | [Samples]

## Getting started

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
    - Here are details about [Java 8 client compatibility with Azure Certificate Authority](https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis).
- [Azure Subscription][azure_subscription]
- An existing [Azure Key Vault][azure_keyvault]. If you need to create a Key Vault, you can use the [Azure Cloud Shell][azure_cloud_shell] to create one with this Azure CLI command. Replace `<your-resource-group-name>` and `<your-key-vault-name>` with your own, unique names:

  ```Bash
  az keyvault create --resource-group <your-resource-group-name> --name <your-key-vault-name>
  ```
- Access configuration:
    - If using [role-based](https://learn.microsoft.com/azure/key-vault/general/rbac-guide) access, assign the roles: `Key Vault Secrets User` and `Key Vault Certificate User`. If used for Jar signing, add role `Key Vault Crypto User`.
    - If using [access policy](https://learn.microsoft.com/azure/key-vault/general/assign-access-policy), add the permissions: `get` and `list` Secret permissions, `get` and `list` Certificate permissions. If used for Jar signing, add `Sign` Cryptographic Operations.

### Include the package

#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on the General Availability (GA) version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
and then include the direct dependency in the dependencies section without the version tag as shown below.

```xml
<dependencies>
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-security-keyvault-jca</artifactId>
    </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-jca;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-jca</artifactId>
    <version>2.10.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts
### SSL/TLS and mTLS
The JCA library supports SSL/TLS and mTLS (Mutual TLS) to enhance security in secure communication channels. It enables applications to securely retrieve certificates from Azure Key Vault and use them for TLS-related operations.

### Jar Signer
The JCA library provides support for Java Archive (JAR) signing, ensuring the integrity and authenticity of JAR files using certificates stored in Azure Key Vault.

## Examples
### Exposed Options
The JCA library supports configuring the following options:
* `azure.keyvault.uri`: The Azure Key Vault endpoint to retrieve certificates.
* `azure.keyvault.tenant-id`: The Microsoft Entra ID tenant ID required for authentication.
* `azure.keyvault.client-id`: The client/application ID used for authentication.
* `azure.keyvault.client-secret`: The client secret for authentication when using client credentials.
* `azure.keyvault.managed-identity`: Indicates whether Managed Identity authentication is enabled.
* `azure.cert-path.well-known`: The path where the well-known certificate is stored.
* `azure.cert-path.custom`: The path where the custom certificate is stored.
* `azure.keyvault.jca.refresh-certificates-when-have-un-trust-certificate`: Indicates whether to refresh certificates when have untrusted certificate.
* `azure.keyvault.jca.certificates-refresh-interval`: The refresh interval time.
* `azure.keyvault.jca.certificates-refresh-interval-in-ms`: The refresh interval time.
* `azure.keyvault.disable-challenge-resource-verification`: Indicates whether to disable verification that the authentication challenge resource matches the Key Vault or Managed HSM domain.

You can configure these properties using:
```java
System.setProperty("azure.keyvault.uri", "<your-azure-keyvault-uri>");
```
or as a JVM argument:
```shell
-Dazure.keyvault.uri=<your-azure-keyvault-uri>
```

### SSL/TLS
#### Server side SSL
If you are looking to integrate the JCA provider to create an SSLServerSocket see the example below.

```java readme-sample-serverSSL
System.setProperty("azure.keyvault.uri", "<your-azure-keyvault-uri>");
System.setProperty("azure.keyvault.tenant-id", "<your-azure-keyvault-tenant-id>");
System.setProperty("azure.keyvault.client-id", "<your-azure-keyvault-client-id>");
System.setProperty("azure.keyvault.client-secret", "<your-azure-keyvault-client-secret>");

KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
Security.addProvider(provider);

KeyStore keyStore = KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();

KeyManagerFactory managerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
managerFactory.init(keyStore, "".toCharArray());

SSLContext context = SSLContext.getInstance("TLS");
context.init(managerFactory.getKeyManagers(), null, null);

SSLServerSocketFactory socketFactory = context.getServerSocketFactory();
SSLServerSocket serverSocket = (SSLServerSocket) socketFactory.createServerSocket(8765);

while (true) {
    SSLSocket socket = (SSLSocket) serverSocket.accept();
    System.out.println("Client connected: " + socket.getInetAddress());
    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

    String body = "Hello, this is server.";
    String response =
        "HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + "Content-Length: " + body.getBytes("UTF-8").length + "\r\n" + "Connection: close\r\n" + "\r\n" + body;

    out.write(response);
    out.flush();
    socket.close();
}
```

Note if you want to use Azure Managed Identity, you should set the value of `azure.keyvault.uri`, and the rest of the parameters would be `null`.

#### Client side SSL
If you are looking to integrate the JCA provider for client side socket connections, see the Apache HTTP client example below.

```java readme-sample-clientSSL
System.setProperty("azure.keyvault.uri", "<your-azure-keyvault-uri>");
System.setProperty("azure.keyvault.tenant-id", "<your-azure-keyvault-tenant-id>");
System.setProperty("azure.keyvault.client-id", "<your-azure-keyvault-client-id>");
System.setProperty("azure.keyvault.client-secret", "<your-azure-keyvault-client-secret>");

KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
Security.addProvider(provider);

KeyStore keyStore = KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();

SSLContext sslContext = SSLContexts
    .custom()
    .loadTrustMaterial(keyStore, new TrustSelfSignedStrategy())
    .build();

SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
    sslContext, (hostname, session) -> true);

PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(
    RegistryBuilder.<ConnectionSocketFactory>create()
        .register("https", sslConnectionSocketFactory)
        .build());

String result = null;

try (CloseableHttpClient client = HttpClients.custom().setConnectionManager(manager).build()) {
    HttpGet httpGet = new HttpGet("https://localhost:8765");
    HttpClientResponseHandler<String> responseHandler = (ClassicHttpResponse response) -> {
        int status = response.getCode();
        String result1 = "Not success";
        if (status == 200) {
            result1 = EntityUtils.toString(response.getEntity());
        }
        return result1;
    };
    result = client.execute(httpGet, responseHandler);
} catch (IOException ioe) {
    ioe.printStackTrace();
}
System.out.println(result);
```

Note if you want to use Azure managed identity, you should set the value of `azure.keyvault.uri`, and the rest of the parameters would be `null`.

### mTLS
#### Server side mTLS
If you are looking to integrate the JCA provider to create an SSLServerSocket see the example below.

```java readme-sample-serverMTLS
KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
Security.addProvider(provider);

System.setProperty("azure.keyvault.uri", "<server-azure-keyvault-uri>");
System.setProperty("azure.keyvault.tenant-id", "<server-azure-keyvault-tenant-id>");
System.setProperty("azure.keyvault.client-id", "<server-azure-keyvault-client-id>");
System.setProperty("azure.keyvault.client-secret", "<server-azure-keyvault-client-secret>");
KeyStore keyStore = KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();

KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
kmf.init(keyStore, "".toCharArray());

System.setProperty("azure.keyvault.uri", "<client-azure-keyvault-uri>");
System.setProperty("azure.keyvault.tenant-id", "<client-azure-keyvault-tenant-id>");
System.setProperty("azure.keyvault.client-id", "<client-azure-keyvault-client-id>");
System.setProperty("azure.keyvault.client-secret", "<client-azure-keyvault-client-secret>");
KeyStore trustStore = KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();

TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
tmf.init(trustStore);

SSLContext context = SSLContext.getInstance("TLS");
context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

SSLServerSocketFactory socketFactory = context.getServerSocketFactory();
SSLServerSocket serverSocket = (SSLServerSocket) socketFactory.createServerSocket(8765);
serverSocket.setNeedClientAuth(true);

while (true) {
    SSLSocket socket = (SSLSocket) serverSocket.accept();
    System.out.println("Client connected: " + socket.getInetAddress());
    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

    String body = "Hello, this is server.";
    String response =
        "HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + "Content-Length: " + body.getBytes("UTF-8").length + "\r\n" + "Connection: close\r\n" + "\r\n" + body;

    out.write(response);
    out.flush();
    socket.close();
}
```

Note if you want to use Azure Managed Identity, you should set the value of `azure.keyvault.uri`, and the rest of the parameters would be `null`.

#### Client side mTLS
If you are looking to integrate the JCA provider for client side socket connections, see the Apache HTTP client example below.

```java readme-sample-clientMTLS
KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
Security.addProvider(provider);

System.setProperty("azure.keyvault.uri", "<client-azure-keyvault-uri>");
System.setProperty("azure.keyvault.tenant-id", "<client-azure-keyvault-tenant-id>");
System.setProperty("azure.keyvault.client-id", "<client-azure-keyvault-client-id>");
System.setProperty("azure.keyvault.client-secret", "<client-azure-keyvault-client-secret>");
KeyStore keyStore = KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();

System.setProperty("azure.keyvault.uri", "<server-azure-keyvault-uri>");
System.setProperty("azure.keyvault.tenant-id", "<server-azure-keyvault-tenant-id>");
System.setProperty("azure.keyvault.client-id", "<server-azure-keyvault-client-id>");
System.setProperty("azure.keyvault.client-secret", "<server-azure-keyvault-client-secret>");
KeyStore trustStore = KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();

SSLContext sslContext = SSLContexts
    .custom()
    .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
    .loadKeyMaterial(keyStore, "".toCharArray())
    .build();

SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
    sslContext, (hostname, session) -> true);

PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(
    RegistryBuilder.<ConnectionSocketFactory>create()
        .register("https", sslConnectionSocketFactory)
        .build());

String result = null;

try (CloseableHttpClient client = HttpClients.custom().setConnectionManager(manager).build()) {
    HttpGet httpGet = new HttpGet("https://localhost:8765");
    HttpClientResponseHandler<String> responseHandler = (ClassicHttpResponse response) -> {
        int status = response.getCode();
        String result1 = "Not success";
        if (status == 200) {
            result1 = EntityUtils.toString(response.getEntity());
        }
        return result1;
    };
    result = client.execute(httpGet, responseHandler);
} catch (IOException ioe) {
    ioe.printStackTrace();
}
System.out.println(result);
```

Note if you want to use Azure managed identity, you should set the value of `azure.keyvault.uri`, and the rest of the parameters would be `null`.

### Jarsigner
You can use the JCA provider to sign JAR files using certificates stored in Azure Key Vault by the following commands:
```bash
 jarsigner   -keystore NONE -storetype AzureKeyVault \
             -signedjar signerjar.jar ${PARAM_YOUR_JAR_FILE_PATH} "${CERT_NAME}" \
             -verbose  -storepass "" \
             -providerName AzureKeyVault \
             -providerClass com.azure.security.keyvault.jca.KeyVaultJcaProvider \
             -J--module-path="${PARAM_JCA_PROVIDER_JAR_PATH}" \
             -J--add-modules="com.azure.security.keyvault.jca" \
             -J-Dazure.keyvault.uri=${KEYVAULT_URL} \
             -J-Dazure.keyvault.tenant-id=${TENANT} \
             -J-Dazure.keyvault.client-id=${CLIENT_ID} \
             -J-Dazure.keyvault.client-secret=${CLIENT_SECRET}
```
You can find completed steps [here](#using-jarsigner-with-azure-key-vault-jca)

### File-System certificates
You can load the certificate in the file system as a trusted certificate by configuring the following properties.

| Certificate Type       | Description                                 | Usage                                                                                     |
|------------------------|---------------------------------------------|-------------------------------------------------------------------------------------------|
| Well-Known Certificate | The file path to the well-known certificate | `System.setProperty("azure.cert-path.well-known", "<well-known-certificate-file-path>")]` |
| Custom Certificate     | The file path to the custom certificate     | `System.setProperty("azure.cert-path.custom", "<custom-certificate-file-path>")`          |

Note: These properties support certificate files only (e.g., `.cer`, `.pem`, `.der`, `.crt`). Private keys are not supported.

### Key-Less certificates
You can set the private key as [non-exportable] to ensure the security of the key.

Note if you want to use key less certificate, you must add `sign` permission.

You can add permission in portal: ![Sign To Principal](https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/keyvault/azure-security-keyvault-jca/resources/SignToPrincipal.png)

Or add permission by cli command:
```shell
  az keyvault set-policy --name ${KEY_VAULT} \
        --object-id ${MANAGED_IDENTITY} \
        --key-permissions get list sign\
        --secret-permissions get list \
        --certificate-permissions get list
```
Please replace `${KEY_VAULT}` with your key vault name and replace `${MANAGED_IDENTITY}` with your principal's object-id.

### Supported key type
| Content Type | Key Type | Key Size or Elliptic curve name | Sign algorithm  | Support |
|--------------|----------|---------------------------------|-----------------|---------|
| PKCS #12     | RSA      | 2048                            | RSASSA-PSS      | ✔       |     
| PKCS #12     | RSA      | 3072                            | RSASSA-PSS      | ✔       |
| PKCS #12     | RSA      | 4096                            | RSASSA-PSS      | ✔       |
| PKCS #12     | EC       | P-256                           | SHA256withECDSA | ✔       |
| PKCS #12     | EC       | P-384                           | SHA384withECDSA | ✔       |
| PKCS #12     | EC       | P-521                           | SHA512withECDSA | ✔       |
| PKCS #12     | EC       | P-256K                          |                 | ✘       |
| PKCS #12     | RSA-HSM  | 2048                            | RSASSA-PSS      | ✔       |     
| PKCS #12     | RSA-HSM  | 3072                            | RSASSA-PSS      | ✔       |
| PKCS #12     | RSA-HSM  | 4096                            | RSASSA-PSS      | ✔       |
| PKCS #12     | EC-HSM   | P-256                           | SHA256withECDSA | ✔       |
| PKCS #12     | EC-HSM   | P-384                           | SHA384withECDSA | ✔       |
| PKCS #12     | EC-HSM   | P-521                           | SHA512withECDSA | ✔       |
| PKCS #12     | EC-HSM   | P-256K                          |                 | ✘       |
| PEM          | RSA      | 2048                            | RSASSA-PSS      | ✔       |
| PEM          | RSA      | 3072                            | RSASSA-PSS      | ✔       |
| PEM          | RSA      | 4096                            | RSASSA-PSS      | ✔       |
| PEM          | EC       | P-256                           | SHA256withECDSA | ✔       |
| PEM          | EC       | P-384                           | SHA384withECDSA | ✔       |
| PEM          | EC       | P-521                           | SHA512withECDSA | ✔       | 
| PEM          | EC       | P-256K                          |                 | ✘       |
| PEM          | RSA-HSM  | 2048                            | RSASSA-PSS      | ✔       |
| PEM          | RSA-HSM  | 3072                            | RSASSA-PSS      | ✔       |
| PEM          | RSA-HSM  | 4096                            | RSASSA-PSS      | ✔       |
| PEM          | EC-HSM   | P-256                           | SHA256withECDSA | ✔       |
| PEM          | EC-HSM   | P-384                           | SHA384withECDSA | ✔       |
| PEM          | EC-HSM   | P-521                           | SHA512withECDSA | ✔       | 
| PEM          | EC-HSM   | P-256K                          |                 | ✘       |

## Using jarsigner with Azure Key Vault JCA
The integration of Azure Key Vault JCA provider can be used with jarsigner to sign JAR files using certificates stored in Azure Key Vault. Below are the steps to configure and use jarsigner with this library.

### Download and Configure JCA Provider Jar
1. Download the latest [JCA](https://repo1.maven.org/maven2/com/azure/azure-security-keyvault-jca) Provider Jar.
2. If you are using Java8, you need to add the JCA provider jar to the class path.
    1. Place the jar under the folder `${JAVA_HOME}/jre/lib/ext`
        - ![place-jar.jpg](https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/keyvault/azure-security-keyvault-jca/resources/place-jar.png)
3. If you are using Java9 or higher, just place the jar in a folder that jarsigner can access.

### Prepare Azure Resources
Follow these steps carefully to achieve successful integration:

1. Prepare your parameters
```shell
DATE_STRING=$(date +%H%M%S)
RESOURCE_GROUP_NAME=jarsigner-rg-$DATE_STRING
KEYVAULT_NAME=jarsigner-kv-$DATE_STRING
CERT_NAME=jarsigner-cert-$DATE_STRING
SERVICE_PRINCIPAL_NAME=jarsigner-sp-$DATE_STRING
SUBSCRIPTION_ID=$(az account show --query id -o tsv)
```

2. Create a resource group
```shell
az group create --name $RESOURCE_GROUP_NAME --location "EastUS"
```

3. Create a key vault
```shell
az keyvault create --name $KEYVAULT_NAME --resource-group $RESOURCE_GROUP_NAME --location "EastUS"
```

4. Assign role to create certificates in the Key Vault.
```shell
# Get your user object ID (if you're using a user account)
USER_OBJECTID=$(az ad signed-in-user show --query id -o tsv)

# Or if you're using a service principal, get its object ID
# SP_OBJECTID=$(az ad sp show --id <your-sp-id> --query id -o tsv)

# Assign Key Vault Certificates Officer role
az role assignment create \
    --role "Key Vault Certificates Officer" \
    --assignee $USER_OBJECTID \
    --scope "/subscriptions/$SUBSCRIPTION_ID/resourceGroups/$RESOURCE_GROUP_NAME/providers/Microsoft.KeyVault/vaults/$KEYVAULT_NAME"
```

5. Get the Key Vault URL
```shell
KEYVAULT_URL=$(az keyvault show --name $KEYVAULT_NAME --query "properties.vaultUri" --resource-group $RESOURCE_GROUP_NAME -o tsv| tr -d '\r\n')
echo $KEYVAULT_URL
```

6. Add a certificate to Key Vault
```shell
az keyvault certificate create --vault-name $KEYVAULT_NAME -n $CERT_NAME -p "$(az keyvault certificate get-default-policy)"
```

7. Create a Service Principal
```shell
SP_JSON=$(az ad sp create-for-rbac --name $SERVICE_PRINCIPAL_NAME)

CLIENT_ID=$(echo $SP_JSON | jq -r '.appId')
CLIENT_SECRET=$(echo $SP_JSON | jq -r '.password')
TENANT=$(echo $SP_JSON | jq -r '.tenant')

echo "CLIENT_ID:"$CLIENT_ID
echo "CLIENT_SECRET:"$CLIENT_SECRET
echo "TENANT:"$TENANT
```
Note the appId and password from the output, you'll need them later.

8. Get the objectId
```shell
OBJECTID=$(az ad sp show --id "$CLIENT_ID" --query id -o tsv | tr -d '\r\n')
echo $OBJECTID
```

9. Assign Roles to Service Principal:
```shell
# Assign Key Vault Secrets Officer role to Service Principal
az role assignment create \
    --role "Key Vault Secrets Officer" \
    --assignee $OBJECTID \
    --scope "/subscriptions/$SUBSCRIPTION_ID/resourceGroups/$RESOURCE_GROUP_NAME/providers/Microsoft.KeyVault/vaults/$KEYVAULT_NAME"

# Assign Key Vault Certificates Officer role Service Principal
az role assignment create \
    --role "Key Vault Certificates Officer" \
    --assignee $OBJECTID \
    --scope "/subscriptions/$SUBSCRIPTION_ID/resourceGroups/$RESOURCE_GROUP_NAME/providers/Microsoft.KeyVault/vaults/$KEYVAULT_NAME"
```

### Sign with Jarsigner
1. If you are using Java8, try to sign the jar using below command
 ```bash
 jarsigner   -keystore NONE -storetype AzureKeyVault \
             -signedjar signerjar.jar ${PARAM_YOUR_JAR_FILE_PATH} "${CERT_NAME}" \
             -verbose  -storepass "" \
             -providerName AzureKeyVault \
             -providerClass com.azure.security.keyvault.jca.KeyVaultJcaProvider \
             -J-Dazure.keyvault.uri=${KEYVAULT_URL} \
             -J-Dazure.keyvault.tenant-id=${TENANT} \
             -J-Dazure.keyvault.client-id=${CLIENT_ID} \
             -J-Dazure.keyvault.client-secret=${CLIENT_SECRET}
 ```

2. If you are using Java9 or higher, try to sign the jar using below command
 ```bash
 jarsigner   -keystore NONE -storetype AzureKeyVault \
             -signedjar signerjar.jar ${PARAM_YOUR_JAR_FILE_PATH} "${CERT_NAME}" \
             -verbose  -storepass "" \
             -providerName AzureKeyVault \
             -providerClass com.azure.security.keyvault.jca.KeyVaultJcaProvider \
             -J--module-path="${PARAM_JCA_PROVIDER_JAR_PATH}" \
             -J--add-modules="com.azure.security.keyvault.jca" \
             -J-Dazure.keyvault.uri=${KEYVAULT_URL} \
             -J-Dazure.keyvault.tenant-id=${TENANT} \
             -J-Dazure.keyvault.client-id=${CLIENT_ID} \
             -J-Dazure.keyvault.client-secret=${CLIENT_SECRET}
 ```

replace ${PARAM_YOUR_JAR_FILE_PATH} with the path of your jar file, replace ${PARAM_JCA_PROVIDER_JAR_PATH} with the path of the jca provider jar.

Check your output, if you see the `jar signed` message, it means the jar is signed successfully.
 ![Jar Signed](https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/keyvault/azure-security-keyvault-jca/resources/jarsigned.png)

### Verify with Jarsigner
After signing, you can verify the JAR file with:
```bash
jarsigner -verify -verbose -certs signerjar.jar
```

Check your output, if you see the `jar verified` message, it means the jar is verified successfully.
 ![Jar Verified](https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/keyvault/azure-security-keyvault-jca/resources/jarverified.png)

### Clean up Resources
```bash
az group delete --name $RESOURCE_GROUP_NAME --yes --no-wait
az ad app delete --id $CLIENT_ID
```

## Troubleshooting

### Debug Key Vault Provider

Remote debugger can be used to troubleshoot complex issues. Let’s try this out in Java 9 and above!

Before you start debugging, make sure the code of your JCA jar is the same as your IDE source code. 

1. Replace the placeholders with your own credentials and execute below command to start the `jarsigner` command:

   ```shell
   jarsigner \
       -keystore NONE \
       -storetype AzureKeyVault \
       -signedjar <file-name-generated-after-signing> <jar-file-name-to-be-signed> <certificate-bundle-name-in-key-vault> \
       -verbose  \
       -storepass "" \
       -providerName AzureKeyVault \
       -providerClass com.azure.security.keyvault.jca.KeyVaultJcaProvider \
       -J--module-path="<your-local-Maven-repository-path>/com/azure/azure-security-keyvault-jca/<current-version-num>/azure-security-keyvault-jca-<current-version-num>.jar" \
       -J--add-modules="com.azure.security.keyvault.jca" \
       -J-Dazure.keyvault.uri=https://<your-key-vault-name>.vault.azure.net/ \
       -J-Dazure.keyvault.tenant-id=<your-tenant-id> \
       -J-Dazure.keyvault.client-id=<your-client-id> \
       -J-Dazure.keyvault.client-secret=<your-client-secret> \
       -J-Djava.security.debug=jar \
       -J-agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=y
   ```

   After execution, you will see the following output information:

   ![start jarsigner command for debug](https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/keyvault/azure-security-keyvault-jca/resources/start-jarsigner-command-for-debug.png)

2. Create a Remote JVM Debug configuration in your IDE tool, such as in Intellij IDEA:

   ![add remote JVM Debug configuration](https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/keyvault/azure-security-keyvault-jca/resources/add-remote-jvm-debug-configuration.png)

3. Click the `Debug` button to debug in your IDE:

   ![debug breakpoints](https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/keyvault/azure-security-keyvault-jca/resources/debug-breakpoints.png)

## Configure logging
This module uses JUL (`java.util.logging`), so to configure things like the logging level you can directly modify the JUL configuration.

Here is an example of a `logging.properties` file:
```properties
# To enable this configuration file, please add this property:
# -Djava.util.logging.config.file="src/test/resources/logging.properties"
#
# The Java logging APIs (java.util.logging) default loads logging.properties from:
# 1. $JAVA_HOME/jre/lib/ (Java 8 and before)
# 2. $JAVA_HOME/conf/ (Java 9 and above)
#
# For more information about this file, please refer to:
# 1. https://docs.oracle.com/javase/8/docs/technotes/guides/logging/overview.html#a1.8
# 2. https://docs.oracle.com/cd/E23549_01/doc.1111/e14568/handler.htm

handlers = java.util.logging.ConsoleHandler
java.util.logging.ConsoleHandler.level = ALL
java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter
java.util.logging.SimpleFormatter.format= [%1$tF %1$tT] %3 [%4$-7s] %5$s %n

.level = INFO
com.azure.security.keyvault.jca.level = ALL
```


### General
Azure Key Vault JCA clients raise exceptions. For example, if you try to check a client's identity with a certificate chain that does not include a trusted certificate, a `CertificateException` will be thrown. In the following snippet, the error is handled gracefully by catching the exception and displaying additional information about the error.

```java
class Demo {
    void demo () {
        try {
            KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
            Security.addProvider(provider);
            // ...
            // Start SSL server socket
            // ...
        } catch (CertificateException e) {
            System.out.println(e.getMessage());
        }
    }
}
```

## Next steps
### Spring Boot
For Spring Boot applications see our [Spring Boot starter][spring_boot_starter].

### References
1. [Java Cryptography Architecture (JCA) Reference Guide][jca_reference_guide]
2. [Creating a Shaded Jar Wiki](https://github.com/Azure/azure-sdk-for-java/wiki/Creating-a-Shaded-Jar)

### Additional documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][microsoft_code_of_conduct]. For more information see the Code of Conduct FAQ or contact <opencode@microsoft.com> with any additional questions or comments.

<!-- LINKS -->
[Source code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-jca/src
[API reference documentation]: https://azure.github.io/azure-sdk-for-java/keyvault.html#azure-security-keyvault-jca
[Product documentation]: https://learn.microsoft.com/azure/key-vault/
[Samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-jca/src/samples/java/com/azure/security/keyvault/jca
[azure_subscription]: https://azure.microsoft.com/
[azure_keyvault]: https://learn.microsoft.com/azure/key-vault/keys/quick-create-portal
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[azure_cloud_shell]: https://shell.azure.com/bash
[spring_boot_starter]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/spring-cloud-azure-starter-keyvault-certificates
[jca_reference_guide]: https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html
[microsoft_code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[non-exportable]: https://learn.microsoft.com/azure/key-vault/certificates/about-certificates#exportable-or-non-exportable-key


