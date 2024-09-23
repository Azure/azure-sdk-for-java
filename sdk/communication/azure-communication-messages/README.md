# Azure Messages client library for Java
This package contains a Java SDK for Azure Communication Messages Services.

## Documentation

Various documentation is available to help you get started
- [Quick Start][azure_communication_messaging_qs]
- [API reference documentation][api_documentation]
- [Product documentation][product_docs]
- [Register WhatsApp Business Account][register_whatsapp_business_account]
- [WhatsApp Template Creation][create-manage-whatsapp-template]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- You must have an [Azure subscription][azure_sub] to use this package.
- An existing Communication Services resource. If you need to create the resource, you can use the [Azure Portal][azure_portal], the [Azure PowerShell][azure_powershell], or the [Azure CLI][azure_cli].
- See [how to register whatsapp business account & create a channel][register_whatsapp_business_account] for registering whatsapp channel to your Communication Services resource.

### Adding the package to your product

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
    <artifactId>azure-communication-messages</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-communication-messages;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-messages</artifactId>
    <version>1.0.6</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

Then SDK provides two clients and each client can be async client or normal client:
- `NotificationMessageClient` or `NotificationMessageAsyncClient` provide operation to send message (text, media or template) and download media file from Whatsapp for given mediaId which we receive in incoming message event (User to Business Flow).
-  `MessageTemplateClient` or `MessageTemplateAsyncClient` provide operation to fetch template list for given channel.

### Authentication

You can get a key and/or connection string from your Communication Services resource in [Azure Portal][azure_portal]. Once you have a key, you may authenticate with any of the following methods:

### Using `a connection string`

```java readme-sample-createNotificationMessageClientWithConnectionString
NotificationMessagesClient notificationClient = new NotificationMessagesClientBuilder()
    .connectionString("<CONNECTION_STRING>")
    .buildClient();
```

```java readme-sample-createMessageTemplateClientWithConnectionString
MessageTemplateClient messageTemplateClient = new MessageTemplateClientBuilder()
    .connectionString("<CONNECTION_STRING>")
    .buildClient();
```
### Using `AzureKeyCredential`

```java readme-sample-createNotificationMessageClientWithAzureKeyCredential
String endpoint = "https://<resource-name>.communication.azure.com";
AzureKeyCredential azureKeyCredential = new AzureKeyCredential("<access-key>");
NotificationMessagesClient notificationClient = new NotificationMessagesClientBuilder()
    .endpoint(endpoint)
    .credential(azureKeyCredential)
    .buildClient();
```

```java readme-sample-createMessageTemplateClientWithAzureKeyCredential
String endpoint = "https://<resource-name>.communication.azure.com";
AzureKeyCredential azureKeyCredential = new AzureKeyCredential("<access-key>");
MessageTemplateClient messageTemplateClient = new MessageTemplateClientBuilder()
    .endpoint(endpoint)
    .credential(azureKeyCredential)
    .buildClient();
```

### Using `Azure Active Directory` managed identity

Client API key authentication is used in most of the examples, but you can also authenticate with Azure Active Directory using the [Azure Identity library][azure_identity]. To use the [DefaultAzureCredential][defaultazurecredential] provider shown below,

The `AZURE_CLIENT_SECRET`, `AZURE_CLIENT_ID`, and `AZURE_TENANT_ID` environment variables are needed to create a `DefaultAzureCredential` object.

```java readme-sample-createNotificationMessageClientWithAAD
String endpoint = "https://<resource-name>.communication.azure.com";
TokenCredential credential = new DefaultAzureCredentialBuilder().build();
NotificationMessagesClient notificationClient =  new NotificationMessagesClientBuilder()
    .endpoint(endpoint)
    .credential(credential)
    .buildClient();
```

```java readme-sample-createMessageTemplateClientWithAAD
String endpoint = "https://<resource-name>.communication.azure.com";
TokenCredential credential = new DefaultAzureCredentialBuilder().build();
MessageTemplateClient messageTemplateClient = new MessageTemplateClientBuilder()
    .endpoint(endpoint)
    .credential(credential)
    .buildClient();
```

## Examples

```java readme-sample-sendTemplateMessage
/*
* This sample shows how to send template message with below details
* Name: sample_shipping_confirmation, Language: en_US
*  [
      {
        "type": "BODY",
        "text": "Your package has been shipped. It will be delivered in {{1}} business days."
      },
      {
        "type": "FOOTER",
        "text": "This message is from an unverified business."
      }
    ]
* */
private void sendTemplateMessage() {

    //Update Template Name and language according your template associate to your channel.
    MessageTemplate template = new MessageTemplate("sample_shipping_confirmation", "en_US");

    //Update template parameter type and value
    List<MessageTemplateValue> messageTemplateValues = new ArrayList<>();
    messageTemplateValues.add(new MessageTemplateText("Days", "5"));
    template.setValues(messageTemplateValues);

    //Update template parameter binding
    List<WhatsAppMessageTemplateBindingsComponent> components = new ArrayList<>();
    components.add(new WhatsAppMessageTemplateBindingsComponent("Days"));
    MessageTemplateBindings bindings = new WhatsAppMessageTemplateBindings()
        .setBody(components);
    template.setBindings(bindings);

    NotificationMessagesClient client = new NotificationMessagesClientBuilder()
        .connectionString("<CONNECTION_STRING>")
        .buildClient();
    List<String> recipients = new ArrayList<>();
    recipients.add("<RECIPIENT_IDENTIFIER e.g. PhoneNumber>");
    SendMessageResult result = client.send(
        new TemplateNotificationContent("CHANNEL_ID", recipients, template));

    result.getReceipts().forEach(r -> System.out.println("Message sent to:" + r.getTo() + " and message id:" + r.getMessageId()));
}
```

```java readme-sample-sendTextMessage
/*
 * This sample shows how to send simple text message with below details
 * Note: Business cannot initiate conversation with text message.
 * */
private void sendTextMessage() {
    NotificationMessagesClient client = new NotificationMessagesClientBuilder()
        .connectionString("<CONNECTION_STRING>")
        .buildClient();
    List<String> recipients = new ArrayList<>();
    recipients.add("<RECIPIENT_IDENTIFIER e.g. PhoneNumber>");
    SendMessageResult result = client.send(
        new TextNotificationContent("<CHANNEL_ID>", recipients, "Hello from ACS messaging"));

    result.getReceipts().forEach(r -> System.out.println("Message sent to:" + r.getTo() + " and message id:" + r.getMessageId()));
}
```

```java readme-sample-sendMediaMessage
/*
 * This sample shows how to send simple media (image, video, document) message with below details
 * Note: Business cannot initiate conversation with media message.
 * */
public void sendMediaMessage() {
    //Update the Media URL
    String mediaUrl = "https://wallpapercave.com/wp/wp2163723.jpg";
    List<String> recipients = new ArrayList<>();
    recipients.add("<RECIPIENT_IDENTIFIER e.g. PhoneNumber>");
    NotificationMessagesClient client = new NotificationMessagesClientBuilder()
        .connectionString("<CONNECTION_STRING>")
        .buildClient();
    SendMessageResult result = client.send(
        new MediaNotificationContent("<CHANNEL_ID>", recipients, mediaUrl));

    result.getReceipts().forEach(r -> System.out.println("Message sent to:" + r.getTo() + " and message id:" + r.getMessageId()));
}
```
### Get Template List for given channel example:
```java readme-sample-ListTemplates
MessageTemplateClient templateClient =
    new MessageTemplateClientBuilder()
        .connectionString("<Connection_String>")
        .buildClient();

PagedIterable<MessageTemplateItem> response = templateClient.listTemplates("<CHANNEL_ID>");

response.stream().forEach(t -> {
    WhatsAppMessageTemplateItem template = (WhatsAppMessageTemplateItem) t;
    System.out.println("===============================");
    System.out.println("Template Name :: " + template.getName());
    System.out.println("Template Language :: " + template.getLanguage());
    System.out.println("Template Status :: " + template.getStatus());
    System.out.println("Template Content :: " + template.getContent());
    System.out.println("===============================");
});
```

## Troubleshooting
> More details coming soon,

## Next steps

- [Read more about Advance Messaging in Azure Communication Services][azure_communication_messaging_qs].
- Please take a look at the samples (src/samples) directory for detailed examples on how to use this library.


## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.


<!-- LINKS -->
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[product_docs]: https://docs.microsoft.com/azure/communication-services/
[package]: https://central.sonatype.com/artifact/com.azure/azure-communication-messages
[api_documentation]: https://aka.ms/java-docs
[azure_communication_messaging_qs]: https://learn.microsoft.com/azure/communication-services/concepts/advanced-messaging/whatsapp/whatsapp-overview
[handle_advance_messaging_events]: https://learn.microsoft.com/azure/communication-services/quickstarts/advanced-messaging/whatsapp/handle-advanced-messaging-events
[register_whatsapp_business_account]: https://learn.microsoft.com/azure/communication-services/quickstarts/advanced-messaging/whatsapp/connect-whatsapp-business-account
[create-manage-whatsapp-template]: https://developers.facebook.com/docs/whatsapp/business-management-api/message-templates/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[defaultazurecredential]: https://github.com/Azure/azure-sdk-for-js/tree/main/sdk/identity/identity#defaultazurecredential
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_sub]: https://azure.microsoft.com/free/
[azure_portal]: https://portal.azure.com
[azure_powershell]: https://docs.microsoft.com/powershell/module/az.communication/new-azcommunicationservice

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcommunication%2Fazure-communication-messages%2FREADME.png)
