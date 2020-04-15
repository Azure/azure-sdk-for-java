# Azure Form Recognizer client library for Java
Azure Cognitive Services Form Recognizer is a cloud service that uses machine learning to recognize text and table data
from form documents. Form Recognizer is made up of the following services:

* Custom models - Extract name/value pairs and table data from forms. These models are trained with your own data, so they're tailored to your forms.
* Prebuilt receipt model - Extract data from USA receipts using a prebuilt model.
* Layout API - Extract text and table structures, along with their bounding box coordinates, from documents.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][samples_readme]

## Getting started

### Prerequisites
- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- [Cognitive Services or Form Recognizer account][form_recognizer_account] to use this package.

### Include the Package

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
The `credential` parameter may be provided as a [`AzureKeyCredential`][azure_key_credential] from [azure-core][azure_core].

##### Create FormRecognizerClient with AzureKeyCredential
To use an [API key][api_key], provide the key as a string to the AzureKeyCredential. This can be found in the [Azure Portal][azure_portal] 
   under the "Quickstart" section or by running the following Azure CLI command:

```bash
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
``` 
Use the API key as the credential parameter to authenticate the client:
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L39-L42 -->
```java
FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
    .apiKey(new AzureKeyCredential("{api_key}"))
    .endpoint("{endpoint}")
    .buildClient();
```
The Azure Form Recognizer client library provides a way to **rotate the existing API key**.

<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L59-L65 -->
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
The Form Recognizer client library provides a [FormRecognizerClient][form_recognizer_sync_client], 
[FormRecognizerAsyncClient][form_recognizer_async_client] and [FormTrainingClient][form_training_sync_client],
[FormTrainingAsyncClient][form_training_async_client]. It also supports model management operations to
get custom model info, delete model, list models and get account details.

### FormRecognizerClient
A `FormRecognizerClient` and `FormRecognizerAsynClient` provides both synchronous and asynchronous operations to access
 a specific use of Form Recognizer, such as recognizing layout, receipt and custom form data from documents.
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L39-L42 -->
```java
```

### FormTrainingClient
A `FormTrainingClient` is the Form Recognizer interface to use for creating, using, and managing custom machine-learned models.
It provides both synchronous and asynchronous operations for training custom models, retrieving and deleting models, 
as well as understanding how close you are to reaching subscription limits for the number of models you can train.
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L39-L42 -->
```java
```

### Long-Running Operations
Long-running operations are operations which consist of an initial request sent to the service to start an operation,
followed by polling the service at intervals to determine whether the operation has completed or failed, and if it has
succeeded, to get the result.

Methods that train models or recognize values from forms are modeled as long-running operations. The client exposes
a `begin_<method-name>` method that returns a `SyncPoller` or `PollerFlux` instance. 
Callers should wait for the operation to completed by calling `getFinalResult()` on the returned operation from the
`begin_<method-name>` method. Sample code snippets are provided to illustrate using long-running operations
[below](#Examples).

### Training models
Using the `FormTrainingClient`, you can train a machine-learned model on your own form type. The resulting model will
be able to recognize values from the types of forms it was trained on.

#### Training without labels
A model trained without labels uses unsupervised learning to understand the layout and relationships between field
names and values in your forms. The learning algorithm clusters the training forms by type and learns what fields and
tables are present in each form type.

This approach doesn't require manual data labeling or intensive coding and maintenance, and we recommend you try this
method first when training custom models.

#### Training with labels
A model trained with labels uses supervised learning to recognize values you specify by adding labels to your training forms.
The learning algorithm uses a label file you provide to learn what fields are found at various locations in the form,
and learns to recognize just those values.

This approach can result in better-performing models, and those models can work with more complex form structures.

### Recognizing values from forms
Using the `FormRecognizerClient`, you can use your own trained models to recognize field values and locations, as well as
table data, from forms of the type you trained your models on. The output of models trained with and without labels
differs as described below.

#### Using models trained without labels
Models trained without labels consider each form page to be a different form type. For example, if you train your
model on 3-page forms, it will learn that these are three different types of forms. When you send a form to it for
analysis, it will return a collection of three pages, where each page contains the field names, values, and locations,
as well as table data, found on that page.

#### Using models trained with labels
Models trained with labels consider a form as a single unit. For example, if you train your model on 3-page forms
with labels, it will learn to recognize field values from the locations you've labeled across all pages in the form.
If you sent a document containing two forms to it for analysis, it would return a collection of two forms,
where each form contains the field names, values, and locations, as well as table data, found in that form.
Fields and tables have page numbers to identify the pages where they were found.

### Managing Custom Models
Using the `FormTrainingClient`, you can get, list, and delete the custom models you've trained.
You can also view the count of models you've trained and the maximum number of models your subscription will
allow you to store.

## Examples

The following section provides several code snippets covering some of the most common Form Recognizer tasks, including:

* [Recognize Receipts](#recognize-receipts "Recognize receipts")
* [Recognize Content](#recognize-content "Recognize Content")
* [Recognize Forms Using a Custom Model](#recognize-forms-using-a-custom-model "Recognize Forms Using a Custom Model")
* [Train a Model](#train-a-model "Train a model")
* [Manage Your Models](#manage-your-models "Manage Your Models")

### Extract receipt information
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L69-L85 -->
```java
String receiptSourceUrl = "https://docs.microsoft.com/en-us/azure/cognitive-services/form-recognizer/media/contoso-allinone.jpg";
SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
    formRecognizerClient.beginRecognizeReceiptsFromUrl(receiptSourceUrl);
IterableStream<RecognizedReceipt> receiptPageResults = syncPoller.getFinalResult();

receiptPageResults.forEach(recognizedReceipt -> {
    USReceipt usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
    System.out.printf("Page Number: %s%n", usReceipt.getMerchantName().getPageNumber());
    System.out.printf("Merchant Name %s%n", usReceipt.getMerchantName().getName());
    System.out.printf("Merchant Name Value: %s%n", usReceipt.getMerchantName().getFieldValue());
    System.out.printf("Merchant Address %s%n", usReceipt.getMerchantAddress().getName());
    System.out.printf("Merchant Address Value: %s%n", usReceipt.getMerchantAddress().getFieldValue());
    System.out.printf("Merchant Phone Number %s%n", usReceipt.getMerchantPhoneNumber().getName());
    System.out.printf("Merchant Phone Number Value: %s%n", usReceipt.getMerchantPhoneNumber().getFieldValue());
    System.out.printf("Total: %s%n", usReceipt.getTotal().getName());
    System.out.printf("Total Value: %s%n", usReceipt.getTotal().getFieldValue());
});
```
For more detailed examples, refer to [here][samples_readme].

## Troubleshooting
### General
Form Recognizer clients raise [HttpResponseException exceptions][http_response_exception]. For example, if you try to provide 
an invalid training SAS url an `HttpResponseException` would be raised with an error indicating the failure cause.
In the following code snippet, the error is handled 
gracefully by catching the exception and display the additional information about the error.

<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L86-L95 -->
```java
```

### Enable client logging
Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite 
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help 
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Default HTTP Client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure 
the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki][http_clients_wiki].

## Next steps
The following section provides several code snippets illustrating common patterns used in the Form Recognizer Python API.

### More sample code

These code samples show common scenario operations with the Azure Form Recognizer client library.
The async versions of the samples (the sample files appended with `_async`) show asynchronous operations
with Form Recognizer.

* Recognize receipts: [sample_recognize_receipts][sample_recognize_receipts] ([async version][sample_recognize_receipts_async])
* Recognize receipts from a URL: [sample_recognize_receipts_from_url][sample_recognize_receipts_from_url] ([async version][sample_recognize_receipts_from_url_async])
* Recognize content: [sample_recognize_content][sample_recognize_content] ([async version][sample_recognize_content_async])
* Recognize custom forms: [sample_recognize_custom_forms][sample_recognize_custom_forms] ([async version][sample_recognize_custom_forms_async])
* Train a model without labels: [sample_train_unlabeled_model][sample_train_unlabeled_model] ([async version][sample_train_unlabeled_model_async])
* Train a model with labels: [sample_train_labeled_model][sample_train_labeled_model] ([async version][sample_train_labeled_model_async])
* Manage custom models: [sample_manage_custom_models][sample_manage_custom_models] ([async_version][sample_manage_custom_models_async])

### Additional documentation

For more extensive documentation on Azure Cognitive Services Form Recognizer, see the [Form Recognizer documentation][api_reference_doc] on docs.microsoft.com.

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
[azure_key_credential]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/credential/AzureKeyCredential.java 
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[create_new_resource]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#create-a-new-azure-cognitive-services-resource
[http_clients_wiki]: https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients
[http_response_exception]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/exception/HttpResponseException.java
[package]: TODO
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[product_documentation]: https://docs.microsoft.com/azure/cognitive-services/form-recognizer/overview
[samples_readme]: src/samples/README.md
[sample_manage_custom_models]: src/samples/java/com/azure/ai/formrecognizer/ManageCustomModels.java
[sample_manage_custom_models_async]: src/samples/java/com/azure/ai/formrecognizer/ManageCustomModelsAsync.java
[sample_recognize_content]: src/samples/java/com/azure/ai/formrecognizer/RecognizeContent.java
[sample_recognize_content_async]: src/samples/java/com/azure/ai/formrecognizer/RecognizeContentAsync.java
[sample_recognize_receipts]: src/samples/java/com/azure/ai/formrecognizer/RecognizeReceipts.java
[sample_recognize_receipts_async]: src/samples/java/com/azure/ai/formrecognizer/RecognizeReceiptsAsync.java
[sample_recognize_receipts_from_url]: src/samples/java/com/azure/ai/formrecognizer/RecognizeReceiptsFromUrl.java
[sample_recognize_receipts_from_url_async]: src/samples/java/com/azure/ai/formrecognizer/RecognizeReceiptsFromUrlAsync.java
[sample_recognize_custom_forms]: src/samples/java/com/azure/ai/formrecognizer/RecognizeCustomForms.java
[sample_recognize_custom_forms_async]: src/samples/java/com/azure/ai/formrecognizer/RecognizeCustomFormsAsync.java
[sample_train_unlabeled_model]: src/samples/java/com/azure/ai/formrecognizer/TrainUnlabeledCustomModel.java
[sample_train_unlabeled_model_async]: src/samples/java/com/azure/ai/formrecognizer/TrainUnlabeledCustomModelAsync.java
[sample_train_labeled_model]: src/samples/java/com/azure/ai/formrecognizer/TrainLabeledCustomModel.java
[sample_train_labeled_model_async]: src/samples/java/com/azure/ai/formrecognizer/TrainLabeledCustomModelAsync.java
[service_access]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows
[source_code]: src
[form_recognizer_account]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows
[form_recognizer_async_client]: src/main/java/com/azure/ai/formrecognizer/FormRecognizerAsyncClient.java
[form_recognizer_sync_client]: src/main/java/com/azure/ai/formrecognizer/FormRecognizerClient.java
[form_training_async_client]: src/main/java/com/azure/ai/formrecognizer/FormTrainingAsyncClient.java
[form_training_sync_client]: src/main/java/com/azure/ai/formrecognizer/FormTrainingClient.java
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fformrecognizer%2Fazure-ai-formrecognizer%2FREADME.png)
