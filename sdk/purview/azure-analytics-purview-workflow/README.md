# Azure Purview Workflow client library for Java

Workflows are automated, repeatable business processes that users can create within Microsoft Purview to validate and orchestrate CUD (create, update, delete) operations on their data entities. Enabling these processes allow organizations to track changes, enforce policy compliance, and ensure quality data across their data landscape.

Use the client library for Purview Workflow to:

- Manage workflows
- Submit user requests and monitor workflow runs
- View and respond to workflow tasks

**For more details about how to use workflow, please refer to the [service documentation][product_documentation]**

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- An existing Azure [Purview account][purview_resource].

### Authentication

Since the Workflow service uses an Azure Active Directory (AAD) bearer token for authentication and identification, an email address should be encoded into the token to allow for notification when using Workflow. It is recommended that the [Azure Identity][azure_identity] library be used  with a the [UsernamePasswordCredential][username_password_credential]. Before using the [Azure Identity][azure_identity] library with Workflow, [an application][app_registration] should be registered and used for the clientId passed to the [UsernamePasswordCredential][username_password_credential].

To use the [UsernamePasswordCredential][username_password_credential] provider shown below, or other credential providers provided with the Azure SDK, please include the `azure-identity` package:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.12.2</version>
</dependency>
```

Set the values of the client ID, tenant ID of the AAD application as environment variables: AZURE_CLIENT_ID, AZURE_TENANT_ID.
Set the value of user name and password of an AAD user as environment variables: USER_NAME, PASSWORD.

```java readme-sample-createWorkflowClient
WorkflowClient purviewWorkflowClient = new WorkflowClientBuilder()
    .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
    .credential(new UsernamePasswordCredentialBuilder()
        .clientId(Configuration.getGlobalConfiguration().get("CLIENTID", "clientId"))
        .tenantId(Configuration.getGlobalConfiguration().get("TENANTID", "tenantId"))
        .username(Configuration.getGlobalConfiguration().get("USERNAME", "username"))
        .password(Configuration.getGlobalConfiguration().get("PASSWORD", "password"))
        .build())
    .buildClient();
```

## Examples

The following section provides several code snippets covering some of the most common scenarios, including:

- [Create Workflow](#create-workflow)
- [Submit User Requests](#submit-user-requests)
- [Approve Workflow Task](#approve-workflow-task)

### Create workflow

```java readme-sample-createWorkflow
BinaryData workflowCreateOrUpdateCommand =
    BinaryData.fromString(
        "{\"name\":\"Create glossary term workflow\",\"description\":\"\",\"triggers\":[{\"type\":\"when_term_creation_is_requested\",\"underGlossaryHierarchy\":\"/glossaries/20031e20-b4df-4a66-a61d-1b0716f3fa48\"}],\"isEnabled\":true,\"actionDag\":{\"actions\":{\"Startandwaitforanapproval\":{\"type\":\"Approval\",\"inputs\":{\"parameters\":{\"approvalType\":\"PendingOnAll\",\"title\":\"ApprovalRequestforCreateGlossaryTerm\",\"assignedTo\":[\"eece94d9-0619-4669-bb8a-d6ecec5220bc\"]}},\"runAfter\":{}},\"Condition\":{\"type\":\"If\",\"expression\":{\"and\":[{\"equals\":[\"@outputs('Startandwaitforanapproval')['body/outcome']\",\"Approved\"]}]},\"actions\":{\"Createglossaryterm\":{\"type\":\"CreateTerm\",\"runAfter\":{}},\"Sendemailnotification\":{\"type\":\"EmailNotification\",\"inputs\":{\"parameters\":{\"emailSubject\":\"GlossaryTermCreate-APPROVED\",\"emailMessage\":\"YourrequestforGlossaryTerm@{triggerBody()['request']['term']['name']}isapproved.\",\"emailRecipients\":[\"@{triggerBody()['request']['requestor']}\"]}},\"runAfter\":{\"Createglossaryterm\":[\"Succeeded\"]}}},\"else\":{\"actions\":{\"Sendrejectemailnotification\":{\"type\":\"EmailNotification\",\"inputs\":{\"parameters\":{\"emailSubject\":\"GlossaryTermCreate-REJECTED\",\"emailMessage\":\"YourrequestforGlossaryTerm@{triggerBody()['request']['term']['name']}isrejected.\",\"emailRecipients\":[\"@{triggerBody()['request']['requestor']}\"]}},\"runAfter\":{}}}},\"runAfter\":{\"Startandwaitforanapproval\":[\"Succeeded\"]}}}}}");
RequestOptions requestOptions = new RequestOptions();
Response<BinaryData> response =
    purviewWorkflowClient.createOrReplaceWithResponse(
        "4afb5752-e47f-43a1-8ba7-c696bf8d2745", workflowCreateOrUpdateCommand, requestOptions);
```

### Submit user requests

```java readme-sample-submitUserRequests
BinaryData userRequestsPayload =
    BinaryData.fromString(
        "{\"comment\":\"Thanks!\",\"operations\":[{\"type\":\"CreateTerm\",\"payload\":{\"glossaryTerm\":{\"name\":\"term\",\"anchor\":{\"glossaryGuid\":\"20031e20-b4df-4a66-a61d-1b0716f3fa48\"},\"nickName\":\"term\",\"status\":\"Approved\"}}}]}");
RequestOptions requestOptions = new RequestOptions();
Response<BinaryData> response =
    userRequestsClient.submitWithResponse(userRequestsPayload, requestOptions);
```

### Approve workflow task

```java readme-sample-approveApproval
BinaryData approvalResponseComment = BinaryData.fromString("{\"comment\":\"Thanks for raising this!\"}");
RequestOptions requestOptions = new RequestOptions();
Response<Void> response =
    approvalClient.approveWithResponse(
        "69b57a00-f5de-4a17-a44a-6479adae373d", approvalResponseComment, requestOptions);
```

## Key concepts

## Troubleshooting

## Next steps

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[product_documentation]: https://learn.microsoft.com/azure/purview/concept-workflow
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[purview_resource]: https://docs.microsoft.com/azure/purview/create-catalog-portal
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md
[app_registration]:https://learn.microsoft.com/azure/active-directory/develop/quickstart-register-app
[username_password_credential]: https://learn.microsoft.com/java/api/com.azure.identity.usernamepasswordcredential?source=recommendations&view=azure-java-stable
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fpurview%2Fazure-analytics-purview-workflow%2FREADME.png)

