# Azure Synapse Access Control client library for Java
Azure Synapse is a limitless analytics service that brings together enterprise data warehousing and Big Data analytics. It gives you the freedom to query data on your terms, using either serverless on-demand or provisioned resources—at scale. Azure Synapse brings these two worlds together with a unified experience to ingest, prepare, manage, and serve data for immediate BI and machine learning needs.

The Azure Synapse Analytics access control client library enables programmatically managing role assignments.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azsynapse_docs] | [Samples][accesscontrol_samples]

## Getting started
### Adding the package to your project
Maven dependency for the Azure Synapse Access Control client library. Add it to your project's POM file.

[//]: # ({x-version-update-start;com.azure:azure-analytics-synapse-accesscontrol;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-analytics-synapse-accesscontrol</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites
- Java Development Kit (JDK) with version 8 or above
- An [Azure subscription][azure_sub].
- An existing Azure Synapse workspace. If you need to create an Azure Synapse workspace, you can use the Azure Portal or [Azure CLI][azure_cli].
    ```Bash
    az synapse workspace create \
        --name <your-workspace-name> \
        --resource-group <your-resource-group-name> \
        --storage-account <your-storage-account-name> \
        --file-system <your-storage-file-system-name> \
        --sql-admin-login-user <your-sql-admin-user-name> \
        --sql-admin-login-password <your-sql-admin-user-password> \
        --location <your-workspace-location>
    ```

### Authenticate the client
In order to interact with the Azure Synapse service, you'll need to create an instance of the [AccessControlClient](#create-access-control-client) class. You would need a **workspace endpoint** and **client secret credentials (client id, client secret, tenant id)** to instantiate a client object using the default `DefaultAzureCredential` examples shown in this document.

The `DefaultAzureCredential` way of authentication by providing client secret credentials is being used in this getting started section but you can find more ways to authenticate with [azure-identity][azure_identity].

#### Create/Get credentials
To create/get client secret credentials you can use the [Azure Portal][azure_create_application_in_portal], [Azure CLI][azure_synapse_cli_full] or [Azure Cloud Shell](https://shell.azure.com/bash)

Here is an [Azure Cloud Shell](https://shell.azure.com/bash) snippet below to

 * Create a service principal and configure its access to Azure resources:

    ```Bash
    az ad sp create-for-rbac -n <your-application-name> --skip-assignment
    ```

    Output:

    ```json
    {
        "appId": "generated-app-ID",
        "displayName": "dummy-app-name",
        "name": "http://dummy-app-name",
        "password": "random-password",
        "tenant": "tenant-ID"
    }
    ```

#### Create Access Control client
Once you've populated the **AZURE_CLIENT_ID**, **AZURE_CLIENT_SECRET**, and **AZURE_TENANT_ID** environment variables and replaced **your-workspace-endpoint** with the URI returned above, you can create the AccessControlClient:

```Java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.analytics.synapse.accesscontrol.AccessControlClient;
import com.azure.analytics.synapse.accesscontrol.AccessControlClientBuilder;

AccessControlClient client = new AccessControlClientBuilder()
    .endpoint("https://{YOUR_WORKSPACE_NAME}.dev.azuresynapse.net")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

> NOTE: For using an asynchronous client use AccessControlAsyncClient instead of AccessControlClient and call `buildAsyncClient()`

## Key concepts
### AccessControlClient
With a `AccessControlClient` you can get role assignments from the workspace, create new role assignments, and delete role assignments.

### Role Assignment
The way you control access to Synapse resources is to create role assignments. A role assignment is the process of attaching a role definition to a user, group, service principal, or managed identity at a particular scope for the purpose of granting access. Access is granted by creating a role assignment, and access is revoked by removing a role assignment.

## Examples
The Azure.Analytics.Synapse.AccessControl package supports synchronous and asynchronous APIs. The following section covers some of the most common Azure Synapse Analytics access control related tasks:

### Sync API
The following sections provide several code snippets covering some of the most common Azure Synapse Access Control service tasks, including:

### Role assignment examples
* [Create a role assignment](#create-a-role-assignment)
* [Retrieve a role assignment](#retrieve-a-role-assignment)
* [List role assignments](#list-role-assignments)
* [Delete a role assignment](#delete-a-role-assignment)

### Create a role assignment

`createRoleAssignment` creates a role assignment.

```java
SynapseRole role = client.getRoleDefinitions().stream()
    .filter(r -> r.getName().equalsIgnoreCase("Workspace Admin"))
    .findAny()
    .get();

RoleAssignmentOptions request = new RoleAssignmentOptions();
request.setRoleId(role.getId());
request.setPrincipalId(principalId);
RoleAssignmentDetails roleAssignmentCreated = client.createRoleAssignment(request);
```

### Retrieve a role assignment

`getRoleAssignmentById` retrieves a role assignment by the given principal ID.

```java
RoleAssignmentDetails roleAssignment = client.getRoleAssignmentById(roleAssignmentCreated.getId());
System.out.printf("Role %s is assigned to %s. Role assignment id: %s\n",
    role.getName(),
    roleAssignment.getPrincipalId(),
    roleAssignment.getId());
```

### List role assignments
`getRoleAssignments` enumerates the role assignments in the Synapse workspace.

```java
List<RoleAssignmentDetails> allRoleAssignments = client.getRoleAssignments();
for (RoleAssignmentDetails roleAssignment : allRoleAssignments)
{
    System.out.println(roleAssignment.getId());
}
```

### Delete a role assignment

`deleteRoleAssignmentById` deletes a role assignment by the given principal ID.

```java
client.deleteRoleAssignmentById(roleAssignment.getId());
```

### Async API
The following sections provide several code snippets covering some of the most common asynchronous Azure Synapse Access Control service tasks, including:

* [Create a role assignment asynchronously](#create-a-role-assignment-asynchronously)
* [Retrieve a role assignment asynchronously](#retrieve-a-role-assignment-asynchronously)
* [List role assignments asynchronously](#list-role-assignments-asynchronously)
* [Delete a role assignment asynchronously](#delete-a-role-assignment-asynchronously)

> Note : You should add `System.in.read()` or `Thread.sleep()` after the function calls in the main class/thread to allow async functions/operations to execute and finish before the main application/thread exits.

### Create a role assignment asynchronously


`createRoleAssignment` creates a role assignment.

```java
// Get the role definition of workspace admin.
SynapseRole role = client.getRoleDefinitions().blockFirst();

// Add a role assignment
String principalId = UUID.randomUUID().toString();
RoleAssignmentOptions request = new RoleAssignmentOptions();
request.setRoleId(role.getId());
request.setPrincipalId(principalId);
client.createRoleAssignment(request)
    .subscribe(r -> System.out.printf("Role assignment created with id \"%s\"", r.getId()));
```

### Retrieve a role assignment asynchronously

`getRoleAssignmentById` retrieves a role assignment by the given principal ID.

```java
client.getRoleAssignmentById(roleAssignment.getId())
    .subscribe(ra ->
        System.out.printf("Role %s is assigned to %s. Role assignment id: %s\n",
            ra.getRoleId(),
            ra.getPrincipalId(),
            ra.getId()));
```

### List role assignments asynchronously
`getRoleAssignments` enumerates the role assignments in the Synapse workspace.

```java
client.getRoleAssignments()
    .subscribe(allRoleAssignments -> {
        for (RoleAssignmentDetails roleAssignment : allRoleAssignments)
        {
            System.out.println(roleAssignment.getId());
        }
    });
```

### Delete a role assignment asynchronously

`deleteRoleAssignmentById` deletes a role assignment by the given principal ID.

```java
client.deleteRoleAssignmentById("roleAssignmentId").block();
```

## Troubleshooting
### General
Azure Synapse Access Control clients raise exceptions. For example, if you try to retrieve a role assignment after it is deleted a `404` error is returned, indicating the resource was not found. In the following snippet, the error is handled gracefully by catching the exception and displaying additional information about the error.

```java
try {
    RoleAssignmentDetails deletedRoleAssignment = client.getRoleAssignmentById(roleAssignmentCreated.getId());
} catch (ResourceNotFoundException e) {
    System.out.println(e.getMessage());
}
```

### Default HTTP client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the [HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients).

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL operations. The Boring SSL library is an Uber JAR containing native libraries for Linux / macOS / Windows, and provides better performance compared to the default SSL implementation within the JDK. For more information, including how to reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps
Several Synapse Java SDK samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered while working with Azure Synapse Analytics.

###  Additional documentation
For more extensive documentation on Azure Synapse Analytics, see the [API reference documentation][azsynapse_rest].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact <opencode@microsoft.com> with any additional questions or comments.

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/synapse/azure-analytics-synapse-accesscontrol/src
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[azsynapse_docs]: https://docs.microsoft.com/azure/synapse-analytics/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity
[maven]: https://maven.apache.org/
[azure_subscription]: https://azure.microsoft.com/
[azure_synapse]: https://docs.microsoft.com/en-us/azure/synapse-analytics/quickstart-create-workspace
[azure_cli]: https://docs.microsoft.com/cli/azure
[rest_api]: https://docs.microsoft.com/rest/api/synapse/
[azsynapse_rest]: https://docs.microsoft.com/rest/api/synapse/
[azure_create_application_in_portal]: https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal
[azure_synapse_cli_full]: https://docs.microsoft.com/cli/azure/synapse?view=azure-cli-latest
[accesscontrol_samples]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/synapse/azure-analytics-synapse-accesscontrol/src/samples/java/com/azure/analytics/synapse/accesscontrol
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning

