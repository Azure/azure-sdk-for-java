## Azure Communications SMS Service client library for Java

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
  <version>1.0.0-beta.4</version>
</dependency>
```

## Authenticate the client

### Azure Active Directory Token Authentication
A `DefaultAzureCredential` object must be passed to the `SmsClientBuilder` via the credential() function. Endpoint and httpClient must also be set via the endpoint() and httpClient() functions respectively.

`AZURE_CLIENT_SECRET`, `AZURE_CLIENT_ID` and `AZURE_TENANT_ID` environment variables
are needed to create a DefaultAzureCredential object.

<!-- embedme src/samples/java/com/azure/communication/sms/samples/quickstart/ReadmeSamples.java#L70-L80 -->
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
```

### Access Key Authentication
SMS uses HMAC authentication with the resource access key.
The access key must be provided to the `SmsClientBuilder` via the credential() function. Endpoint and httpClient must also be set via the endpoint() and httpClient() functions respectively.

<!-- embedme src/samples/java/com/azure/communication/sms/samples/quickstart/ReadmeSamples.java#L21-L32 -->
```java
// You can find your endpoint and access key from your resource in the Azure Portal
String endpoint = "https://<resource-name>.communication.azure.com";
AzureKeyCredential azureKeyCredential = new AzureKeyCredential("<access-key>");

// Create an HttpClient builder of your choice and customize it
HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

SmsClient smsClient = new SmsClientBuilder()
    .endpoint(endpoint)
    .credential(azureKeyCredential)
    .httpClient(httpClient)
    .buildClient();
```

Alternatively, you can provide the entire connection string using the connectionString() function instead of providing the endpoint and access key.
<!-- embedme src/samples/java/com/azure/communication/sms/samples/quickstart/ReadmeSamples.java#L55-L64 -->
```java
// You can find your connection string from your resource in the Azure Portal
String connectionString = "https://<resource-name>.communication.azure.com/;<access-key>";

// Create an HttpClient builder of your choice and customize it
HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

SmsClient smsClient = new SmsClientBuilder()
    .connectionString(connectionString)
    .httpClient(httpClient)
    .buildClient();
```

## Key concepts

There are two different forms of authentication to use the Azure Communication SMS Service.

## Examples

### Send a 1:1 SMS Message
Use the `send` or `sendWithResponse` function to send a SMS message to a single phone number.

<!-- embedme src/samples/java/com/azure/communication/sms/samples/quickstart/ReadmeSamples.java#L103-L110 -->
```java
SmsSendResult sendResult = smsClient.send(
    "<from-phone-number>",
    "<to-phone-number>",
    "Weekly Promotion");

System.out.println("Message Id: " + sendResult.getMessageId());
System.out.println("Recipient Number: " + sendResult.getTo());
System.out.println("Send Result Successful:" + sendResult.isSuccessful());
```
### Send a 1:N SMS Message
To send a SMS message to a list of recipients, call the `send` or `sendWithResponse` function with a list of recipient phone numbers. You may also add pass in an options object to specify whether the delivery report should be enabled and set custom tags.

<!-- embedme src/samples/java/com/azure/communication/sms/samples/quickstart/ReadmeSamples.java#L116-L131 -->
```java
SmsSendOptions options = new SmsSendOptions();
options.setDeliveryReportEnabled(true);
options.setTag("Marketing");

Iterable<SmsSendResult> sendResults = smsClient.sendWithResponse(
    "<from-phone-number>",
    Arrays.asList("<to-phone-number1>", "<to-phone-number2>"),
    "Weekly Promotion",
    options /* Optional */,
    Context.NONE).getValue();

for (SmsSendResult result : sendResults) {
    System.out.println("Message Id: " + result.getMessageId());
    System.out.println("Recipient Number: " + result.getTo());
    System.out.println("Send Result Successful:" + result.isSuccessful());
}
```

## Troubleshooting

SMS operations will throw an exception if the request to the server fails.
Exceptions will not be thrown if the error is caused by an individual message, only if something fails with the overall request.
Please use the `isSuccessful()` flag to validate each individual result to verify if the message was sent.
<!-- embedme src/samples/java/com/azure/communication/sms/samples/quickstart/ReadmeSamples.java#L153-L176 -->
```java
try {
    SmsSendOptions options = new SmsSendOptions();
    options.setDeliveryReportEnabled(true);
    options.setTag("Marketing");

    Response<Iterable<SmsSendResult>> sendResults = smsClient.sendWithResponse(
        "<from-phone-number>",
        Arrays.asList("<to-phone-number1>", "<to-phone-number2>"),
        "Weekly Promotion",
        options /* Optional */,
        Context.NONE);

    Iterable<SmsSendResult> smsSendResults = sendResults.getValue();
    for (SmsSendResult result : smsSendResults) {
        if (result.isSuccessful()) {
            System.out.println("Successfully sent this message: " + result.getMessageId() + " to " + result.getTo());
        } else {
            System.out.println("Something went wrong when trying to send this message " + result.getMessageId() + " to " + result.getTo());
            System.out.println("Status code " + result.getHttpStatusCode() + " and error message " + result.getErrorMessage());
        }
    }
} catch (RuntimeException ex) {
    System.out.println(ex.getMessage());
}
```

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

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.
