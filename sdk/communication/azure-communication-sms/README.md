# Azure Communications SMS Service client library for Java

Azure Communication SMS is used to send simple text messages.

<!-- [Source code][source] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][azconfig_docs] -->

## Getting started

### Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- A deployed Communication Services resource. <!--[Create a Communication Services resource](../create-a-communication-resource.md). -->

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-communication-sms;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-communication-sms</artifactId>
  <version>1.0.0-beta.1</version> 
</dependency>
```

## Key concepts

To send messages with Azure Communication SMS Service a resource access key is used 
for authentication. 

SMS messaging uses HMAC authentication with resource access key. This is done via the 
CommunicationClientCredentials The credentials must be provided to the SMSClientBuilder 
via the credential() function. Endpoint and httpClient must also be set.

<!-- embedme src/samples/java/com/azure/communication/sms/samples/quickstart/App.java#L25-L56 -->
```java
// Retrieve the Azure Communication SMS Service endpoint for use with the application. 
// The endpoint string is stored in an environment variable on the machine running the 
// application called COMMUNICATION_SERVICES_ENDPOINT.
String endpoint = System.getenv("COMMUNICATION_SERVICES_ENDPOINT");

// Retrieve the access key string for use with the application. The access key
// string is stored in an environment variable on the machine running the application 
// called COMMUNICATION_SERVICES_ACCESS_KEY.
String accessKey = System.getenv("COMMUNICATION_SERVICES_ACCESS_KEY");

// Instantiate the http client
HttpClient httpClient = null; // Your HttpClient

CommunicationClientCredential credential = null;
try {
    credential = new CommunicationClientCredential(accessKey);
} catch (NoSuchAlgorithmException e) {
    System.out.println(e.getMessage());
} catch (InvalidKeyException e) {
    System.out.println(e.getMessage());
}

// Create a new SmsClientBuilder to instantiate an SmsClient
SmsClientBuilder smsClientBuilder = new SmsClientBuilder();

// Set the endpoint, access key, and the HttpClient
smsClientBuilder.endpoint(endpoint)
    .credential(credential)
    .httpClient(httpClient);

// Build a new SmsClient
SmsClient smsClient = smsClientBuilder.buildClient();
```

## Examples

### Sending a message
Use the `sendMessage` function to send a new message to a list of phone numbers.
(Currently SMS Services only supports one phone number).
Once you send the message, you'll receive a response where you can access several
properties such as the message id with the `response.getMessageId()` function.

<!-- embedme src/samples/java/com/azure/communication/sms/samples/quickstart/App.java#L58-L74 -->
```java
// Currently Sms services only supports one phone number
List<PhoneNumber> to = new ArrayList<PhoneNumber>();
to.add(new PhoneNumber("<to-phone-number>"));

// SendSmsOptions is an optional field. It can be used
// to enable a delivery report to the Azure Event Grid
SendSmsOptions options = new SendSmsOptions();
options.setEnableDeliveryReport(true);

// Send the message and check the response for a message id
SendSmsResponse response = smsClient.sendMessage(
    new PhoneNumber("<leased-phone-number>"), 
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

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Feng%2Fazure-communications-sms%2FREADME.png)