# Azure Form Recognizer client library for Java
Form Recognizer is a cloud-based cognitive service that uses machine learning technology to identify and extract text, 
key-value pairs and table data from form documents.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][samples_readme]

## Getting started

### Prerequisites
- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- [Cognitive Services or Form Recognizer account][form_recognizer_account] to use this package.

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-formrecognizer;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-formrecognizer</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Create a Form Recognizer resource
Form Recognizer supports both [multi-service and single-service access][service_access]. Create a Cognitive Services 
resource if you plan to access multiple cognitive services under a single endpoint/key. For Form Recognizer access only,
create a Form Recognizer resource.

You can create either resource using the 

**Option 1:** [Azure Portal][create_new_resource] 

**Option 2:** [Azure CLI][azure_cli]

Below is an example of how you can create a Form Recognizer resource using the CLI:

```bash
# Create a new resource group to hold the Form Recognizer resource -
# if using an existing resource group, skip this step
az group create --name my-resource-group --location westus2
```

```bash
# Create Form Recognizer
az cognitiveservices account create \
    --name text-analytics-resource \
    --resource-group my-resource-group \
    --kind TextAnalytics \
    --sku F0 \
    --location westus2 \
    --yes
```
### Authenticate the client
In order to interact with the Form Recognizer service, you will need to create an instance of the `FormRecognizerClient` 
class. You will need an **endpoint** and an **API key** to instantiate a client object, 
they can be found in the [Azure Portal][azure_portal] under the "Quickstart" in your created
Form Recognizer resource. See the full details regarding [authentication][authentication] of Cognitive Services.

#### Get credentials
The authentication credential may be provided as the API key to your resource.

##### Create FormRecognizerClient with API Key Credential
To use an [API key][api_key], provide the key as a string. This can be found in the [Azure Portal][azure_portal] 
   under the "Quickstart" section or by running the following Azure CLI command:

```bash
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
``` 
Use the API key as the credential parameter to authenticate the client:
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L38-L41 -->
```java
FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
    .apiKey(new AzureKeyCredential("{api_key}"))
    .endpoint("{endpoint}")
    .buildClient();
```
The Azure Form Recognizer client library provides a way to **rotate the existing API key**.

<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L58-L64 -->
```java
AzureKeyCredential credential = new AzureKeyCredential("{api_key}");
FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
    .apiKey(credential)
    .endpoint("{endpoint}")
    .buildClient();

credential.update("{new_api_key}");
```

## Key concepts
### Client
The Form Recognizer client library provides a [FormRecognizerClient][form_recognizer_sync_client] and 
[FormRecognizerAsyncClient][form_recognizer_async_client]. It provides both synchronous and
asynchronous operations to access a specific use of Form Recognizer, such as training custom models,
extracting layout and receipt information from documents.

## Examples
The following sections provide several code snippets covering some of the most common Form Recognizer tasks, including:

### Form Recognizer Client
Form Recognizer support both synchronous and asynchronous client creation by using
`FormRecognizerClientBuilder`,

<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L38-L41 -->
``` java
FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
    .apiKey(new AzureKeyCredential("{api_key}"))
    .endpoint("{endpoint}")
    .buildClient();
```
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L48-L51 -->
``` java
FormRecognizerAsyncClient formRecognizerAsyncClient = new FormRecognizerClientBuilder()
    .apiKey(new AzureKeyCredential("{api_key}"))
    .endpoint("{endpoint}")
    .buildAsyncClient();
```

### Extract receipt information
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L68-L86 -->
```java
String receiptSourceUrl = "https://docs.microsoft.com/en-us/azure/cognitive-services/form-recognizer/media/contoso-allinone.jpg";
SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller =
    formRecognizerClient.beginExtractReceipt(receiptSourceUrl);
IterableStream<ExtractedReceipt> extractedReceipts = syncPoller.getFinalResult();

for (ExtractedReceipt extractedReceiptItem : extractedReceipts) {
    System.out.printf("Page Number %s%n", extractedReceiptItem.getPageMetadata().getPageNumber());
    System.out.printf("Merchant Name %s%n", extractedReceiptItem.getMerchantName().getText());
    System.out.printf("Merchant Address %s%n", extractedReceiptItem.getMerchantAddress().getText());
    System.out.printf("Merchant Phone Number %s%n", extractedReceiptItem.getMerchantPhoneNumber().getText());
    System.out.printf("Total: %s%n", extractedReceiptItem.getTotal().getText());
    System.out.printf("Receipt Items: %n");
    extractedReceiptItem.getReceiptItems().forEach(receiptItem -> {
        System.out.printf("Name: %s%n", receiptItem.getName().getText());
        System.out.printf("Quantity: %s%n", receiptItem.getQuantity().getText());
        System.out.printf("Total Price: %s%n", receiptItem.getTotalPrice().getText());
        System.out.println();
    });
}
```
For more detailed examples, refer to [here][samples_readme].

## Troubleshooting
### General
Form Recognizer clients raise exceptions. 
TODO

### Enable client logging
Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite 
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help 
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Default HTTP Client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure 
the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients).

## Next steps
- Samples are explained in detail [here][samples_readme].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[api_key]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#get-the-keys-for-your-resource
[api_reference_doc]: https://aka.ms/azsdk-java-formrecognizer-ref-docs
[authentication]: https://docs.microsoft.com/azure/cognitive-services/authentication
[azure_cli]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account-cli?tabs=windows
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity#credentials
[azure_portal]: https://ms.portal.azure.com
[azure_subscription]: https://azure.microsoft.com/free
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[create_new_resource]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#create-a-new-azure-cognitive-services-resource
[package]: TODO
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[product_documentation]: https://docs.microsoft.com/azure/cognitive-services/form-recognizer/overview
[samples_readme]: src/samples/README.md
[service_access]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows
[source_code]: src
[form_recognizer_account]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows
[form_recognizer_async_client]: src/main/java/com/azure/ai/formrecognizer/FormRecognizerAsyncClient.java
[form_recognizer_sync_client]: src/main/java/com/azure/ai/formrecognizer/FormRecognizerClient.java
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fformrecognizer%2Fazure-ai-formrecognizer%2FREADME.png)
