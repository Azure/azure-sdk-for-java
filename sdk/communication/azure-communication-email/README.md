# Azure Communication Email client library for Java

This package contains the Java SDK for Azure Communication Services for Email.

## Getting started

### Prerequisites

- [Azure subscription][azure_sub]
- [Communication Service Resource][communication_resource_docs]
- [Email Communication Resource][email_resource_docs] with an active [Domain][domain_overview]
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above
- [Apache Maven](https://maven.apache.org/download.cgi)

To create these resources, you can use the [Azure Portal][communication_resource_create_portal], the [Azure PowerShell][communication_resource_create_power_shell], or the [.NET management client library][communication_resource_create_net].

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

and then include the direct dependency in the dependencies section without the version tag.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-email</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-communication-email;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-email</artifactId>
    <version>1.0.14</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts
> More details coming soon.

## Examples

`EmailClient` provides the functionality to send email messages .

### Client Creation and Authentication

Email clients can be created and authenticated using the connection string acquired from an Azure Communication Resource in the [Azure Portal][azure_portal].

```java readme-sample-createEmailClientWithConnectionString
String connectionString = "https://<resource-name>.communication.azure.com/;<access-key>";

EmailClient emailClient = new EmailClientBuilder()
    .connectionString(connectionString)
    .buildClient();
```

Email clients can also be created and authenticated using the endpoint and Azure Key Credential acquired from an Azure Communication Resource in the [Azure Portal][azure_portal].

```java readme-sample-createEmailClientUsingAzureKeyCredential
String endpoint = "https://<resource-name>.communication.azure.com";
AzureKeyCredential azureKeyCredential = new AzureKeyCredential("<access-key>");

EmailClient emailClient = new EmailClientBuilder()
    .endpoint(endpoint)
    .credential(azureKeyCredential)
    .buildClient();
```

### Azure Active Directory Token Authentication
A `DefaultAzureCredential` object must be passed to the `EmailClientBuilder` via the `credential()` method. An endpoint must also be set via the `endpoint()` method.

The `AZURE_CLIENT_SECRET`, `AZURE_CLIENT_ID`, and `AZURE_TENANT_ID` environment variables are needed to create a `DefaultAzureCredential` object.

```java readme-sample-createEmailClientWithAAD
// You can find your endpoint and access key from your resource in the Azure Portal
String endpoint = "https://<resource-name>.communication.azure.com/";

EmailClient emailClient = new EmailClientBuilder()
    .endpoint(endpoint)
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

### Send an Email Message

To send an email message, call the `beginSend` function from the `EmailClient`. This will return a poller. You can use this poller to check on the status of the operation and retrieve the result once it's finished.

```java readme-sample-sendEmailToSingleRecipient
EmailMessage message = new EmailMessage()
    .setSenderAddress("<sender-email-address>")
    .setToRecipients("<recipient-email-address>")
    .setSubject("test subject")
    .setBodyPlainText("test message");

SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message);
PollResponse<EmailSendResult> response = poller.waitForCompletion();

System.out.println("Operation Id: " + response.getValue().getId());
```

### Send an Email Message to Multiple Recipients

To send an email message to multiple recipients, simply add the new addresses in the appropriate `EmailMessage` setter.

```java readme-sample-sendEmailToMultipleRecipients
EmailMessage message = new EmailMessage()
    .setSenderAddress("<sender-email-address>")
    .setSubject("test subject")
    .setBodyPlainText("test message")
    .setToRecipients("<recipient-email-address>", "<recipient-2-email-address>")
    .setCcRecipients("<cc-recipient-email-address>")
    .setBccRecipients("<bcc-recipient-email-address>");

SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message);
PollResponse<EmailSendResult> response = poller.waitForCompletion();

System.out.println("Operation Id: " + response.getValue().getId());
```

To customize the email message recipients further, you can instantiate the `EmailAddress` objects and pass that them to the appropriate `EmailMessage' setters.

```java readme-sample-sendEmailToMultipleRecipientsWithOptions
EmailAddress toAddress1 = new EmailAddress("<recipient-email-address>")
    .setDisplayName("Recipient");

EmailAddress toAddress2 = new EmailAddress("<recipient-2-email-address>")
    .setDisplayName("Recipient 2");

EmailMessage message = new EmailMessage()
    .setSenderAddress("<sender-email-address>")
    .setSubject("test subject")
    .setBodyPlainText("test message")
    .setToRecipients(toAddress1, toAddress2);

SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message);
PollResponse<EmailSendResult> response = poller.waitForCompletion();

System.out.println("Operation Id: " + response.getValue().getId());
```

### Send Email with Attachments

Azure Communication Services support sending email with attachments.

```java readme-sample-sendEmailWithAttachment
BinaryData attachmentContent = BinaryData.fromFile(new File("C:/attachment.txt").toPath());
EmailAttachment attachment = new EmailAttachment(
    "attachment.txt",
    "text/plain",
    attachmentContent
);

EmailMessage message = new EmailMessage()
    .setSenderAddress("<sender-email-address>")
    .setToRecipients("<recipient-email-address>")
    .setSubject("test subject")
    .setBodyPlainText("test message")
    .setAttachments(attachment);

SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message);
PollResponse<EmailSendResult> response = poller.waitForCompletion();

System.out.println("Operation Id: " + response.getValue().getId());
```

## Troubleshooting
> More details coming soon,

## Next steps

- [Read more about Email in Azure Communication Services][nextsteps]

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit [cla.microsoft.com][cla].

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->

[azure_sub]: https://azure.microsoft.com/free/dotnet/
[azure_portal]: https://portal.azure.com
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[communication_resource_docs]: https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource?tabs=windows&pivots=platform-azp
[email_resource_docs]: https://aka.ms/acsemail/createemailresource
[communication_resource_create_portal]: https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource?tabs=windows&pivots=platform-azp
[communication_resource_create_power_shell]: https://docs.microsoft.com/powershell/module/az.communication/new-azcommunicationservice
[communication_resource_create_net]: https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource?tabs=windows&pivots=platform-net
[package]: https://www.nuget.org/packages/Azure.Communication.Common/
[product_docs]: https://aka.ms/acsemail/overview
[nextsteps]: https://aka.ms/acsemail/overview
[nuget]: https://www.nuget.org/
[source]: https://github.com/Azure/azure-sdk-for-net/tree/main/sdk/communication
[domain_overview]: https://aka.ms/acsemail/domainsoverview
