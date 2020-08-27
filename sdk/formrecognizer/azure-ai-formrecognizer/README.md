# Azure Form Recognizer client library for Java
Azure Cognitive Services Form Recognizer is a cloud service that uses machine learning to recognize text and table data
from form documents. It includes the following main functionalities:

* Custom models - Recognize field values and table data from forms. These models are trained with your own data, so they're tailored to your forms. You can then take these custom models and recognize forms. You can also manage the custom models you've created and see how close you are to the limit of custom models your account can hold.
* Content API - Recognize text and table structures, along with their bounding box coordinates, from documents. Corresponds to the REST service's Layout API.
* Prebuilt receipt model - Recognize data from USA sales receipts using a prebuilt model.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][sample_readme]

## Getting started

### Prerequisites
- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- [Cognitive Services or Form Recognizer account][form_recognizer_account] to use this package.

### Include the Package
**Note:** This beta version targets Azure Form Recognizer service API version v2.0.

[//]: # ({x-version-update-start;com.azure:azure-ai-formrecognizer;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-formrecognizer</artifactId>
    <version>3.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

#### Create a Form Recognizer resource
Form Recognizer supports both [multi-service and single-service access][service_access]. Create a Cognitive Service's
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
    --name form-recognizer-resource \
    --resource-group my-resource-group \
    --kind FormRecognizer \
    --sku S0 \
    --location westus2 \
    --yes
```
### Authenticate the client
In order to interact with the Form Recognizer service, you will need to create an instance of the Form Recognizer client.
Both the asynchronous and synchronous clients can be created by using `FormRecognizerClientBuilder`. Invoking `buildClient()`
will create the synchronous client, while invoking `buildAsyncClient` will create its asynchronous counterpart.

You will need an **endpoint** and a **key** to instantiate a client object.

##### Looking up the endpoint
You can find the **endpoint** for your Form Recognizer resource in the [Azure Portal][azure_portal],
or [Azure CLI][azure_cli_endpoint].
```bash
# Get the endpoint for the resource
az cognitiveservices account show --name "resource-name" --resource-group "resource-group-name" --query "endpoint"
```

#### Create a Form Recognizer client using AzureKeyCredential
To use `AzureKeyCredential` authentication, provide the [key][key] as a string to the [AzureKeyCredential][azure_key_credential].
This key can be found in the [Azure Portal][azure_portal] in your created Form Recognizer
resource, or by running the following Azure CLI command to get the key from the Form Recognizer resource:

```bash
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
```
Use the API key as the credential parameter to authenticate the client:
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L47-L50 -->
```java
FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L57-L60 -->
```java
FormTrainingClient formTrainingClient = new FormTrainingClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```

The Azure Form Recognizer client library provides a way to **rotate the existing key**.

<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L67-L73 -->
```java
AzureKeyCredential credential = new AzureKeyCredential("{key}");
FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
    .credential(credential)
    .endpoint("{endpoint}")
    .buildClient();

credential.update("{new_key}");
```

#### Create a Form Recognizer client with Azure Active Directory credential
Azure SDK for Java supports an Azure Identity package, making it easy to get credentials from Microsoft identity
platform.

Authentication with AAD requires some initial setup:
* Add the Azure Identity package

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.0.9</version>
</dependency>
```
[//]: # ({x-version-update-end})
* [Register a new Azure Active Directory application][register_AAD_application]
* [Grant access][grant_access] to Form Recognizer by assigning the `"Cognitive Services User"` role to your service principal.

After setup, you can choose which type of [credential][azure_identity_credential_type] from azure.identity to use.
As an example, [DefaultAzureCredential][wiki_identity] can be used to authenticate the client:
Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables:
AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET.

Authorization is easiest using [DefaultAzureCredential][wiki_identity]. It finds the best credential to use in its
running environment. For more information about using Azure Active Directory authorization with Form Recognizer, please
refer to [the associated documentation][aad_authorization].

<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L80-L84 -->
```java
TokenCredential credential = new DefaultAzureCredentialBuilder().build();
FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
    .endpoint("{endpoint}")
    .credential(credential)
    .buildClient();
```

## Key concepts
### FormRecognizerClient
The [FormRecognizerClient][form_recognizer_sync_client] and [FormRecognizerAsyncClient][form_recognizer_async_client]
provide both synchronous and asynchronous operations
 - Recognizing form fields and content using custom models trained to recognize your custom forms.
 These values are returned in a collection of `RecognizedForm` objects. See example [Recognize Custom Forms](#recognize-forms-using-a-custom-model).
 - Recognizing form content, including tables, lines and words, without the need to train a model.
 Form content is returned in a collection of `FormPage` objects. See example [Recognize Content](#recognize-content).
 - Recognizing common fields from US receipts, using a pre-trained receipt model on the Form Recognizer service.
 These fields and meta-data are returned in a collection of `RecognizedForm` objects. See example [Recognize Receipts](#recognize-receipts).

### FormTrainingClient
The [FormTrainingClient][form_training_sync_client] and
[FormTrainingAsyncClient][form_training_async_client] provide both synchronous and asynchronous operations
- Training custom models to recognize all fields and values found in your custom forms. See example [Train a model](#train-a-model).
 A `CustomFormModel` is returned indicating the form types the model will recognize, and the fields it will extract for
  each form type. See the [service's documents][fr_train_without_labels] for a more detailed explanation.
- Training custom models to recognize specific fields and values you specify by labeling your custom forms.
A `CustomFormModel` is returned indicating the fields the model will extract, as well as the estimated accuracy for
each field. See the [service's documents][fr_train_with_labels] for a more detailed explanation.
- Managing models created in your account. See example [Manage models](#manage-your-models).
- Copying a custom model from one Form Recognizer resource to another.

Please note that models can also be trained using a graphical user interface such as the [Form Recognizer Labeling Tool][fr_labeling_tool].

### Long-Running Operations
Long-running operations are operations which consist of an initial request sent to the service to start an operation,
followed by polling the service at intervals to determine whether the operation has completed or failed, and if it has
succeeded, to get the result.

Methods that train models or recognize values from forms are modeled as long-running operations. The client exposes
a `begin<MethodName>` method that returns a `SyncPoller` or `PollerFlux` instance.
Callers should wait for the operation to completed by calling `getFinalResult()` on the returned operation from the
`begin<MethodName>` method. Sample code snippets are provided to illustrate using long-running operations
[below](#Examples).

## Examples

The following section provides several code snippets covering some of the most common Form Recognizer tasks, including:

* [Recognize Forms Using a Custom Model](#recognize-forms-using-a-custom-model "Recognize Forms Using a Custom Model")
* [Recognize Content](#recognize-content "Recognize Content")
* [Recognize Receipts](#recognize-receipts "Recognize receipts")
* [Train a Model](#train-a-model "Train a model")
* [Manage Your Models](#manage-your-models "Manage Your Models")

### Recognize Forms Using a Custom Model
Recognize name/value pairs and table data from forms. These models are trained with your own data,
so they're tailored to your forms. You should only recognize forms of the same form type that the custom model was trained on.
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L88-L104 -->
```java
String formUrl = "{form_url}";
String modelId = "{custom_trained_model_id}";
SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> recognizeFormPoller =
    formRecognizerClient.beginRecognizeCustomFormsFromUrl(modelId, formUrl);

List<RecognizedForm> recognizedForms = recognizeFormPoller.getFinalResult();

for (int i = 0; i < recognizedForms.size(); i++) {
    RecognizedForm form = recognizedForms.get(i);
    System.out.printf("----------- Recognized custom form info for page %d -----------%n", i);
    System.out.printf("Form type: %s%n", form.getFormType());
    form.getFields().forEach((label, formField) ->
        System.out.printf("Field %s has value %s with confidence score of %f.%n", label,
            formField.getValueData().getText(),
            formField.getConfidence())
    );
}
```

### Recognize Content
Recognize text and table structures, along with their bounding box coordinates, from documents.
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L113-L136 -->
```java
// recognize form content using file input stream
File form = new File("local/file_path/filename.png");
byte[] fileContent = Files.readAllBytes(form.toPath());
InputStream inputStream = new ByteArrayInputStream(fileContent);

SyncPoller<FormRecognizerOperationResult, List<FormPage>> recognizeContentPoller =
    formRecognizerClient.beginRecognizeContent(inputStream, form.length());

List<FormPage> contentPageResults = recognizeContentPoller.getFinalResult();

for (int i = 0; i < contentPageResults.size(); i++) {
    FormPage formPage = contentPageResults.get(i);
    System.out.printf("----Recognizing content info for page %d ----%n", i);
    // Table information
    System.out.printf("Has width: %f and height: %f, measured with unit: %s.%n", formPage.getWidth(),
        formPage.getHeight(),
        formPage.getUnit());
    formPage.getTables().forEach(formTable -> {
        System.out.printf("Table has %d rows and %d columns.%n", formTable.getRowCount(),
            formTable.getColumnCount());
        formTable.getCells().forEach(formTableCell ->
            System.out.printf("Cell has text %s.%n", formTableCell.getText()));
    });
}
```

### Recognize receipts
Recognize data from a USA sales receipts using a prebuilt model. Receipt fields recognized by the service 
can be found [here][service_recognize_receipt].
See [StronglyTypedRecognizedForm][strongly_typed_sample] for a suggested approach to extract
information from receipts.

<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L140-L196-->
```java
String receiptUrl = "https://docs.microsoft.com/azure/cognitive-services/form-recognizer/media"
    + "/contoso-allinone.jpg";
SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
    formRecognizerClient.beginRecognizeReceiptsFromUrl(receiptUrl);
List<RecognizedForm> receiptPageResults = syncPoller.getFinalResult();

for (int i = 0; i < receiptPageResults.size(); i++) {
    RecognizedForm recognizedForm = receiptPageResults.get(i);
    Map<String, FormField> recognizedFields = recognizedForm.getFields();
    System.out.printf("----------- Recognizing receipt info for page %d -----------%n", i);
    FormField merchantNameField = recognizedFields.get("MerchantName");
    if (merchantNameField != null) {
        if (FieldValueType.STRING == merchantNameField.getValue().getValueType()) {
            String merchantName = merchantNameField.getValue().asString();
            System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                merchantName, merchantNameField.getConfidence());
        }
    }

    FormField merchantPhoneNumberField = recognizedFields.get("MerchantPhoneNumber");
    if (merchantPhoneNumberField != null) {
        if (FieldValueType.PHONE_NUMBER == merchantPhoneNumberField.getValue().getValueType()) {
            String merchantAddress = merchantPhoneNumberField.getValue().asPhoneNumber();
            System.out.printf("Merchant Phone number: %s, confidence: %.2f%n",
                merchantAddress, merchantPhoneNumberField.getConfidence());
        }
    }

    FormField transactionDateField = recognizedFields.get("TransactionDate");
    if (transactionDateField != null) {
        if (FieldValueType.DATE == transactionDateField.getValue().getValueType()) {
            LocalDate transactionDate = transactionDateField.getValue().asDate();
            System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                transactionDate, transactionDateField.getConfidence());
        }
    }

    FormField receiptItemsField = recognizedFields.get("Items");
    if (receiptItemsField != null) {
        System.out.printf("Receipt Items: %n");
        if (FieldValueType.LIST == receiptItemsField.getValue().getValueType()) {
            List<FormField> receiptItems = receiptItemsField.getValue().asList();
            receiptItems.stream()
                .filter(receiptItem -> FieldValueType.MAP == receiptItem.getValue().getValueType())
                .map(formField -> formField.getValue().asMap())
                .forEach(formFieldMap -> formFieldMap.forEach((key, formField) -> {
                    if ("Quantity".equals(key)) {
                        if (FieldValueType.FLOAT == formField.getValue().getValueType()) {
                            Float quantity = formField.getValue().asFloat();
                            System.out.printf("Quantity: %f, confidence: %.2f%n",
                                quantity, formField.getConfidence());
                        }
                    }
                }));
        }
    }
}
```

### Train a model
Train a machine-learned model on your own form type. The resulting model will be able to recognize values from the types of forms it was trained on.
Provide a container SAS url to your Azure Storage Blob container where you're storing the training documents. See details on setting this up
in the [service quickstart documentation][quickstart_training].
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L200-L220 -->
```java
String trainingFilesUrl = "{SAS_URL_of_your_container_in_blob_storage}";
SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
    formTrainingClient.beginTraining(trainingFilesUrl, false);

CustomFormModel customFormModel = trainingPoller.getFinalResult();

// Model Info
System.out.printf("Model Id: %s%n", customFormModel.getModelId());
System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
System.out.printf("Training started on: %s%n", customFormModel.getTrainingStartedOn());
System.out.printf("Training completed on: %s%n%n", customFormModel.getTrainingCompletedOn());

System.out.println("Recognized Fields:");
// looping through the subModels, which contains the fields they were trained on
// Since the given training documents are unlabeled, we still group them but they do not have a label.
customFormModel.getSubmodels().forEach(customFormSubmodel -> {
    // Since the training data is unlabeled, we are unable to return the accuracy of this model
    customFormSubmodel.getFields().forEach((field, customFormModelField) ->
        System.out.printf("Field: %s Field Label: %s%n",
            field, customFormModelField.getLabel()));
});
```

### Manage your models
Manage the custom models in your Form Recognizer account.
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L224-L252 -->
```java
// First, we see how many custom models we have, and what our limit is
AccountProperties accountProperties = formTrainingClient.getAccountProperties();
System.out.printf("The account has %d custom models, and we can have at most %d custom models",
    accountProperties.getCustomModelCount(), accountProperties.getCustomModelLimit());

// Next, we get a paged list of all of our custom models
PagedIterable<CustomFormModelInfo> customModels = formTrainingClient.listCustomModels();
System.out.println("We have following models in the account:");
customModels.forEach(customFormModelInfo -> {
    System.out.printf("Model Id: %s%n", customFormModelInfo.getModelId());
    // get specific custom model info
    CustomFormModel customModel = formTrainingClient.getCustomModel(customFormModelInfo.getModelId());
    System.out.printf("Model Status: %s%n", customModel.getModelStatus());
    System.out.printf("Training started on: %s%n", customModel.getTrainingStartedOn());
    System.out.printf("Training completed on: %s%n", customModel.getTrainingCompletedOn());
    customModel.getSubmodels().forEach(customFormSubmodel -> {
        System.out.printf("Custom Model Form type: %s%n", customFormSubmodel.getFormType());
        System.out.printf("Custom Model Accuracy: %f%n", customFormSubmodel.getAccuracy());
        if (customFormSubmodel.getFields() != null) {
            customFormSubmodel.getFields().forEach((fieldText, customFormModelField) -> {
                System.out.printf("Field Text: %s%n", fieldText);
                System.out.printf("Field Accuracy: %f%n", customFormModelField.getAccuracy());
            });
        }
    });
});

// Delete Custom Model
formTrainingClient.deleteModel("{modelId}");
```
For more detailed examples, refer to [samples][sample_readme].

## Troubleshooting
### General
Form Recognizer clients raises `HttpResponseException` [exceptions][http_response_exception]. For example, if you try
to provide an invalid file source URL an `HttpResponseException` would be raised with an error indicating the failure cause.
In the following code snippet, the error is handled
gracefully by catching the exception and display the additional information about the error.

<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L259-L263 -->
```java
try {
    formRecognizerClient.beginRecognizeContentFromUrl("invalidSourceUrl");
} catch (HttpResponseException e) {
    System.out.println(e.getMessage());
}
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
The following section provides several code snippets illustrating common patterns used in the Form Recognizer API.
These code samples show common scenario operations with the Azure Form Recognizer client library.

* Recognize receipts: [RecognizeReceipts][recognize_receipts]
* Recognize receipts from a URL: [RecognizeReceiptsFromUrl][recognize_receipts_from_url]
* Recognize content: [RecognizeContent][recognize_content]
* Recognize custom forms from a URL: [RecognizeCustomFormsFromUrl][recognize_custom_forms]
* Train a model without labels: [TrainModelWithoutLabels][train_unlabeled_model]
* Train a model with labels: [TrainModelWithLabels][train_labeled_model]
* Manage custom models: [ManageCustomModels][manage_custom_models]
* Copy a model between Form Recognizer resources: [CopyModel][copy_model]

#### Async APIs
All the examples shown so far have been using synchronous APIs, but we provide full support for async APIs as well.
You'll need to use `FormRecognizerAsyncClient`
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L270-L273 -->
```java
FormRecognizerAsyncClient formRecognizerAsyncClient = new FormRecognizerClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildAsyncClient();
```

* Recognize receipts : [RecognizeReceiptsAsync][recognize_receipts_async]
* Recognize receipts from a URL: [RecognizeReceiptsFromUrlAsync][recognize_receipts_from_url_async]
* Recognize content from a URL: [RecognizeContentFromUrlAsync][recognize_content_from_url_async]
* Recognize custom forms: [RecognizeCustomFormsAsync][recognize_custom_forms_async]
* Train a model without labels: [TrainModelWithoutLabelsAsync][train_unlabeled_model_async]
* Train a model with labels: [TrainModelWithLabelsAsync][train_labeled_model_async]
* Manage custom models: [ManageCustomModelsAsync][manage_custom_models_async]
* Copy a model between Form Recognizer resources: [CopyModelAsync][copy_model_async]

### Additional documentation

For more extensive documentation on Azure Cognitive Services Form Recognizer, see the [Form Recognizer documentation][api_reference_doc].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[aad_authorization]: https://docs.microsoft.com/azure/cognitive-services/authentication#authenticate-with-azure-active-directory
[azure_key_credential]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/credential/AzureKeyCredential.java
[key]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#get-the-keys-for-your-resource
[api_reference_doc]: https://aka.ms/azsdk-java-formrecognizer-ref-docs
[azure_identity_credential_type]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity#credentials
[azure_cli]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account-cli?tabs=windows
[azure_cli_endpoint]: https://docs.microsoft.com/cli/azure/cognitiveservices/account?view=azure-cli-latest#az-cognitiveservices-account-show
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity#credentials
[azure_portal]: https://ms.portal.azure.com
[azure_subscription]: https://azure.microsoft.com/free
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[create_new_resource]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#create-a-new-azure-cognitive-services-resource
[differentiate_custom_forms_with_labeled_and_unlabeled_models]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AdvancedDiffLabeledUnlabeledData.java
[form_recognizer_account]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows
[form_recognizer_async_client]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/FormRecognizerAsyncClient.java
[form_recognizer_sync_client]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/FormRecognizerClient.java
[form_training_async_client]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/training/FormTrainingAsyncClient.java
[form_training_sync_client]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/training/FormTrainingClient.java
[grant_access]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[http_clients_wiki]: https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients
[fr_labeling_tool]: https://docs.microsoft.com/azure/cognitive-services/form-recognizer/quickstarts/label-tool
[fr_train_without_labels]: https://docs.microsoft.com/azure/cognitive-services/form-recognizer/overview#train-without-labels
[fr_train_with_labels]: https://docs.microsoft.com/azure/cognitive-services/form-recognizer/overview#train-with-labels
[http_response_exception]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/exception/HttpResponseException.java
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[package]: https://mvnrepository.com/artifact/com.azure/azure-ai-formrecognizer
[product_documentation]: https://docs.microsoft.com/azure/cognitive-services/form-recognizer/overview
[sample_readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/README.md
[manage_custom_models]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/ManageCustomModels.java
[manage_custom_models_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/ManageCustomModelsAsync.java
[recognize_content]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/RecognizeContent.java
[recognize_content_from_url_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/RecognizeContentFromUrlAsync.java
[recognize_receipts]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/RecognizeReceipts.java
[recognize_receipts_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/RecognizeReceiptsAsync.java
[recognize_receipts_from_url]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/RecognizeReceiptsFromUrl.java
[recognize_receipts_from_url_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/RecognizeReceiptsFromUrlAsync.java
[recognize_custom_forms]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/RecognizeCustomFormsFromUrl.java
[recognize_custom_forms_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/RecognizeCustomFormsAsync.java
[register_AAD_application]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[train_unlabeled_model]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/TrainModelWithoutLabels.java
[train_unlabeled_model_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/TrainModelWithoutLabelsAsync.java
[train_labeled_model]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/TrainModelWithLabels.java
[train_labeled_model_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/TrainModelWithLabelsAsync.java
[copy_model]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/CopyModel.java
[copy_model_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/CopyModelAsync.java
[service_access]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows
[service_recognize_receipt]: https://aka.ms/formrecognizer/receiptfields
[strongly_typed_sample]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/StronglyTypedRecognizedForm.java
[source_code]: src
[quickstart_training]: https://docs.microsoft.com/azure/cognitive-services/form-recognizer/quickstarts/curl-train-extract#train-a-form-recognizer-model
[wiki_identity]: https://github.com/Azure/azure-sdk-for-java/wiki/Identity-and-Authentication

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fformrecognizer%2Fazure-ai-formrecognizer%2FREADME.png)
