# Introduction to Managed Identity and Role-Based Access Control (RBAC)

[Managed identity][managed_identity] and [role-based access control][rbac] are commonly used together for identity and access management.

Managed identity provides an identity for the Azure resource in Azure Active Directory, and uses it to obtain Azure AD token.
RBAC enforces the role, scope, and access control of that managed identity. 

They are useful in scenarios of enabling Azure virtual machine or web app to either manage Azure resource, or access data in Azure resource.

The role of Owner is required for assigning role to managed identity. It can be configured from Azure Active Directory blade in Portal, or by Azure CLI.

## Managing Azure resource

Sample code to enable system-assigned managed identity to a virtual machine, and give it the role of contributor to a resource group.
```java
virtualMachine.update()
    .withSystemAssignedManagedServiceIdentity()
    .withSystemAssignedIdentityBasedAccessTo(resourceGroup.id(), BuiltInRole.CONTRIBUTOR)
    .apply();
```

It allows code running in this particular virtual machine to manage other Azure resource on the target resource group.

Managed identity can be enabled with similar code on Azure web app.

SDK will support managed identity on Azure Kubernetes in the future.

## Accessing data in Azure resource

### Storage account

Sample code to enable system-assigned managed identity to a web app, and give it the role of blob data contributor to a storage account.
```java
webApp.update()
    .withSystemAssignedManagedServiceIdentity()
    .withSystemAssignedIdentityBasedAccessTo(storageAccount.id, BuiltInRole.STORAGE_BLOB_DATA_CONTRIBUTOR)
    .apply();
```

It allows code running in this particular web app to access blob data in the target storage account.

### Key vault

SDK supports access policies for key vault.

Sample code to allow access of secret to user-assigned managed identity.
```java
vault.update()
    .defineAccessPolicy()
        .forObjectId(identity.principalId())
        .allowSecretPermissions(SecretPermissions.GET)
        .attach()
    .apply();
```

For system-assigned managed identity, use e.g. `virtualMachine.systemAssignedManagedServiceIdentityPrincipalId()` in place of `identity.principalId()`.

Since 2.5.0, SDK supports role-based access control. Please check `BuiltInRole.KEY_VAULT_CRYPTO_USER`, `BuiltInRole.KEY_VAULT_SECRETS_USER`, etc. for related roles.

## Accessing Azure Active Directory

SDK provides limited support for accessing Azure Active Directory, generally only for querying applications, users, groups, and service principals.

This requires additional permission from Azure Active Directory, which can be configured from Azure Active Directory blade in Portal, or by Azure CLI.

Since 2.2.0, SDK switched from [Azure Active Directory Grpah API][aad_graph] to [Microsoft Graph API][microsoft_graph].

Permission required (since 2.2.0):
- Access application and service principal: Microsoft Graph, Application permission, [Application.Read.All](https://docs.microsoft.com/graph/api/application-list?view=graph-rest-1.0&tabs=http#permissions)
- Access user: Microsoft Graph, Application permission, [User.Read.All](https://docs.microsoft.com/graph/api/user-list?view=graph-rest-1.0&tabs=http#permissions)
- Access group: Microsoft Graph, Application permission, [Group.Read.All](https://docs.microsoft.com/graph/api/group-list?view=graph-rest-1.0&tabs=http#permissions)

Sample code to assign role of contributor to another service principal.
```java
azure.accessManagement().roleAssignments().define(UUID.randomUUID().toString())
    .forServicePrincipal("<service-principal-name>")
    .withBuiltInRole(BuiltInRole.CONTRIBUTOR)
    .withSubscriptionScope("<subscriptionId>")
    .create();
```

For dedicated Java SDK for Microsoft Graph API, please use [Microsoft Graph SDK for Java](https://github.com/microsoftgraph/msgraph-sdk-java).

## Types of managed identity and authentication

As we know, there are 2 types of managed identities. Their usage in authentication is slightly different.

Here is sample code when using [Azure Identity library][azure_identity].

For system-assigned managed identity, one can use it without providing client ID:
```java
TokenCredential credential = new ManagedIdentityCredentialBuilder().build();
```

For user-assigned managed identity, one need to provide a client ID of the identity:
```java
TokenCredential credential = new ManagedIdentityCredentialBuilder().clientId("<clientId>").build();
```

If the user-assigned managed identity is managed by this SDK (`azure-resourcemanager-msi`), the client ID can be found via `Identity.clientId()`.

## Chaining managed identity and service principal in authentication

For application that deploys to Azure resource, but still need to debug in local machine, one can use a `ChainedTokenCredential` to concatenate 2 credentials.

In below sample code, `ManagedIdentityCredential` will take effect when deployed to Azure resource.
When debugging in local machine, as fallback, `EnvironmentCredential` will pick up configure from system environment, and authenticate via a different [service principal][service_principal], likely with access only to development resource.
```java
TokenCredential credential = new ChainedTokenCredentialBuilder()
    .addLast(new ManagedIdentityCredentialBuilder().build())
    .addLast(new EnvironmentCredentialBuilder().build())
    .build();
```

For more details on authentication methods, please refer to [Azure Identity][azure_identity].

[managed_identity]: https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview
[rbac]: https://docs.microsoft.com/azure/role-based-access-control/overview
[microsoft_graph]: https://docs.microsoft.com/graph/overview
[aad_graph]: https://docs.microsoft.com/azure/active-directory/develop/active-directory-graph-api
[service_principal]: https://docs.microsoft.com/azure/active-directory/develop/app-objects-and-service-principals
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
