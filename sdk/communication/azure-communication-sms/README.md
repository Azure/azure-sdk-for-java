# Azure Communications SMS Service client library for Java

Azure Communication SMS is used to send simple text messages.

[Source code][source] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][product_docs]

## Getting started

### Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- A deployed Communication Services resource. You can use the [Azure Portal](https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource?tabs=windows&pivots=platform-azp) or the [Azure PowerShell](https://docs.microsoft.com/powershell/module/az.communication/new-azcommunicationservice) to set it up.

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-communication-sms;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-communication-sms</artifactId>
  <version>1.0.0-beta.3</version> 
</dependency>
```

## Key concepts

There are two different forms of authentication to use the Azure Communication SMS Service.

### Azure Active Directory Token Authentication

The `DefaultAzureCredential` object must be passed to the `SmsClientBuilder` via
the credential() funtion. Endpoint and httpClient must also be set
via the endpoint() and httpClient() functions respectively.

`AZURE_CLIENT_SECRET`, `AZURE_CLIENT_ID` and `AZURE_TENANT_ID` environment variables 
are needed to create a DefaultAzureCredential object.

<!-- embedme src/samples/java/com/azure/communication/sms/samples/quickstart/ReadmeSamples.java#L80-L92 -->
```java
// You can find your endpoint and access key from your resource in the Azure Portal
String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

// Create an HttpClient builder of your choice and customize it
HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

SmsClient smsClient = new SmsClientBuilder()
    .endpoint(endpoint)
    .credential(new DefaultAzureCredentialBuilder().build())
    .httpClient(httpClient)
    .buildClient();

return smsClient;
```

### Access Key Authentication

SMS messaging also uses HMAC authentication with a resource access key. The access key must be provided
via the accessKey() function. Endpoint and httpClient must also be set via the endpoint() and httpClient()
functions respectively.

<!-- embedme src/samples/java/com/azure/communication/sms/samples/quickstart/ReadmeSamples.java#L23-L39 -->
```java

// Your can find your endpoint and access key from your resource in the Azure Portal
String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
String accessKey = "SECRET";

// Instantiate the http client
HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

// Create a new SmsClientBuilder to instantiate an SmsClient
SmsClientBuilder smsClientBuilder = new SmsClientBuilder();

// Set the endpoint, access key, and the HttpClient
smsClientBuilder.endpoint(endpoint)
    .accessKey(accessKey)
    .httpClient(httpClient);

// Build a new SmsClient
```

Alternatively, you can provide the entire connection string using the connectionString() function instead of providing the endpoint and access key. 
<!-- embedme src/samples/java/com/azure/communication/sms/samples/quickstart/ReadmeSamples.java#L65-L71 -->
```java
// Your can find your connection string from your resource in the Azure Portal
String connectionString = "<connection_string>";

SmsClient smsClient = new SmsClientBuilder()
    .connectionString(connectionString)
    .httpClient(httpClient)
    .buildClient();
```

## Examples

### Sending a message
Use the `sendMessage` function to send a new message to a list of phone numbers.
(Currently SMS Services only supports one phone number).
Once you send the message, you'll receive a response where you can access several
properties such as the message id with the `response.getMessageId()` function.

<!-- embedme src/samples/java/com/azure/communication/sms/samples/quickstart/ReadmeSamples.java#L42-L58 -->
```java
// Currently Sms services only supports one phone number
List<PhoneNumberIdentifier> to = new ArrayList<PhoneNumberIdentifier>();
to.add(new PhoneNumberIdentifier("<to-phone-number>"));

// SendSmsOptions is an optional field. It can be used
// to enable a delivery report to the Azure Event Grid
SendSmsOptions options = new SendSmsOptions();
options.setEnableDeliveryReport(true);

// Send the message and check the response for a message id
SendSmsResponse response = smsClient.sendMessage(
    new PhoneNumberIdentifier("<leased-phone-number>"), 
    to, 
    "your message",
    options /* Optional */);

System.out.println("MessageId: " + response.getMessageId());
```

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.


## Troubleshooting

In progress.

## Next steps

Check out other client libraries for Azure Communication Services

<!-- LINKS -->
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[product_docs]: https://docs.microsoft.com/azure/communication-services/
[package]: https://search.maven.org/artifact/com.azure/azure-communication-sms
[api_documentation]: https://aka.ms/java-docs
[source]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/communication/azure-communication-sms/src

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Feng%2Fazure-communications-sms%2FREADME.png)