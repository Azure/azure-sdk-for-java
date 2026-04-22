# Azure Identity Examples


This page shows common `azure-identity` patterns for authenticating Azure SDK clients. All examples authenticate a `SecretClient` from `azure-security-keyvault-secrets`; the same pattern applies to any Azure SDK client.

**Dependency:**

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-identity</artifactId>
  <version>1.13.0</version> <!-- {x-version-update;com.azure:azure-identity;dependency} -->
</dependency>
```

---

## DefaultAzureCredential

Tries a chain of credentials automatically (environment vars → workload identity → managed identity → Azure CLI → etc.):

```java
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

SecretClient client = new SecretClientBuilder()
    .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
    .credential(credential)
    .buildClient();
```


### User-Assigned Managed Identity with DefaultAzureCredential

```java
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
    .managedIdentityClientId("<MANAGED_IDENTITY_CLIENT_ID>")
    .build();
```

### IntelliJ Toolkit with DefaultAzureCredential (Windows)

```java
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
    .intelliJKeePassDatabasePath("C:\\Users\\user\\AppData\\Roaming\\JetBrains\\IdeaIC2020.1\\c.kdbx")
    .build();
```

---

## Service Principal — Client Secret

```java
ClientSecretCredential credential = new ClientSecretCredentialBuilder()
    .clientId("<YOUR_CLIENT_ID>")
    .clientSecret("<YOUR_CLIENT_SECRET>")
    .tenantId("<YOUR_TENANT_ID>")
    .build();
```


---

## Service Principal — Client Certificate

```java
ClientCertificateCredential credential = new ClientCertificateCredentialBuilder()
    .clientId("<YOUR_CLIENT_ID>")
    .pemCertificate("<PATH TO PEM CERTIFICATE>")
    // .pfxCertificate("<PATH TO PFX CERTIFICATE>", "PFX PASSWORD")
    .tenantId("<YOUR_TENANT_ID>")
    .build();
```

---

## Device Code Flow (IoT / headless)

```java
DeviceCodeCredential credential = new DeviceCodeCredentialBuilder()
    .challengeConsumer(challenge -> System.out.println(challenge.getMessage()))
    .build();
```


---

## Interactive Browser

```java
InteractiveBrowserCredential credential = new InteractiveBrowserCredentialBuilder()
    .clientId("<YOUR CLIENT ID>")
    .redirectUrl("http://localhost:8765")
    .build();
```

---

## Authorization Code Flow (Web Apps)

```java
AuthorizationCodeCredential credential = new AuthorizationCodeCredentialBuilder()
    .clientId("<YOUR CLIENT ID>")
    .authorizationCode("<AUTH CODE FROM QUERY PARAMETERS>")
    .redirectUrl("<THE REDIRECT URL>")
    .build();
```

---

## Azure CLI Credential

```java
AzureCliCredential credential = new AzureCliCredentialBuilder().build();
```

Sign in with: `az login`

---

## Azure PowerShell Credential

```java
AzurePowerShellCredential credential = new AzurePowerShellCredentialBuilder().build();
```

Sign in with: `Connect-AzAccount`

---

## IntelliJ IDEA Credential

```java
IntelliJCredential credential = new IntelliJCredentialBuilder()
    .keePassDatabasePath("C:\\Users\\user\\AppData\\Roaming\\JetBrains\\IdeaIC2020.1\\c.kdbx")
    .build();
```

---

## Managed Identity (VM, App Service, AKS, Cloud Shell)

```java
// System-assigned managed identity
ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().build();

// User-assigned managed identity
ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder()
    .clientId("<USER ASSIGNED MANAGED IDENTITY CLIENT ID>")
    .build();
```

---

## Chaining Credentials

Try credentials in sequence, stopping at the first success:

```java
ManagedIdentityCredential managedIdentity = new ManagedIdentityCredentialBuilder()
    .clientId("<YOUR_CLIENT_ID>")
    .build();

ClientSecretCredential servicePrincipal = new ClientSecretCredentialBuilder()
    .clientId("<YOUR_CLIENT_ID>")
    .clientSecret("<YOUR_CLIENT_SECRET>")
    .tenantId("<YOUR_TENANT_ID>")
    .build();

ChainedTokenCredential credential = new ChainedTokenCredentialBuilder()
    .addLast(managedIdentity)
    .addLast(servicePrincipal)
    .build();
```

---

## Azure Stack

```java
ClientSecretCredential credential = new ClientSecretCredentialBuilder()
    .authorityHost("<Azure Stack Authority Host>")   // from Get-AzEnvironment
    .tenantId("<Tenant Id>")                          // "adfs" for ADFS identity provider
    .clientSecret("<client-secret>")
    .clientId("<client-id>")
    .build();
```

---

## Using MSAL Directly as TokenCredential

If you need to bypass `azure-identity` and use MSAL4J directly:

```java
TokenCredential credential = tokenRequestContext -> Mono.defer(() -> {
    String authorityUrl = AzureAuthorityHosts.AZURE_PUBLIC_CLOUD + "/" + "<YOUR-TENANT>";
    PublicClientApplication app = PublicClientApplication.builder("<YOUR-CLIENT-ID>")
        .authority(authorityUrl)
        .build();
    DeviceCodeFlowParameters params = DeviceCodeFlowParameters
        .builder(Collections.singleton("<Your-Azure-Service-Scope>"),
                 dc -> System.out.println(dc.message()))
        .build();
    return Mono.fromFuture(app.acquireToken(params));
}).map(result -> new AccessToken(result.accessToken(),
        OffsetDateTime.ofInstant(result.expiresOnDate().toInstant(), ZoneOffset.UTC)));
```

---

## See Also

- [Configuration](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/configuration.md)
- [FAQ](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/faq.md)
