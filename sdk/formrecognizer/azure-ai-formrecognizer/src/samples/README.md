---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-cognitive-services
  - azure-form-recognizer
urlFragment: formrecognizer-java-samples
---

# Azure Form Recognizer client library samples for Java

Azure Form Recognizer samples are a set of self-contained Java programs that demonstrate interacting with Azure Form Recognizer service
using the client library. Each sample focuses on a specific scenario and can be executed independently. 

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].

## Examples
The following sections provide code samples covering common scenario operations with the Azure Form Recognizer client library.

All of these samples need the endpoint to your Form Recognizer resource ([instructions on how to get endpoint][get-endpoint-instructions]), and your Form Recognizer API key ([instructions on how to get key][get-key-instructions]).

|**File Name**|**Description**|
|----------------|-------------|
|[Authentication][authentication_sample]|Authenticate the client|
|[RecognizeContent][recognize_content] and [RecognizeContentFromUrlAsync][recognize_content_from_url_async]|Recognize text and table structures of a document|
|[RecognizeReceipts][recognize_receipts] and [RecognizeReceiptsAsync][recognize_receipts_async]|Recognize data from a file of a US sales receipt using a prebuilt model|
|[RecognizeReceiptsFromUrl][recognize_receipts_from_url] and [RecognizeReceiptsFromUrlAsync][recognize_receipts_from_url_async]|Recognize data from a URL of a US sales receipt using a prebuilt model|
|[RecognizeCustomFormsFromUrl][recognize_custom_forms] and [RecognizeCustomFormsAsync][recognize_custom_forms_async]|Recognize forms with your custom model|
|[TrainLabeledModel][train_labeled_model] and [TrainLabeledModelAsync][train_labeled_model_async]|Train a custom model with labeled data|
|[TrainUnlabeledModel][train_unlabeled_model] and [TrainUnlabeledModelAsync][train_unlabeled_model_async]|Train a custom model with unlabeled data|
|[ManageCustomModels][manage_custom_models] and [ManageCustomModelsAsync][manage_custom_models_async]|Manage the custom models in your account|
|[CopyModel][copy_model] and [CopyModelAsync][copy_model_async]|Copy custom model from one Form Recognizer resource to another|

## Troubleshooting
Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps
Check out the [API reference documentation][java_fr_ref_docs] to learn more about
what you can do with the Azure Form Recognizer client library.
Following section provides code samples for some of the advanced scenarios in Form Recognizer Client library:
|**Advanced Sample File Name**|**Description**|
|----------------|-------------|
|[StronglyTypedRecognizedForm][strongly_typed_sample]|Use the fields in your recognized forms to create a receipt object with strongly-typed US receipt fields|
|[GetBoundingBoxes][get_bounding_boxes] and [GetBoundingBoxesAsync][get_bounding_boxes_async]|Get info to visualize the outlines of form content and fields, which can be used for manual validation|
|[AdvancedDiffCustomFormsLabeledUnlabeledData][differentiate_custom_forms_with_labeled_and_unlabeled_models] and [AdvancedDiffCustomFormsLabeledUnlabeledDataAsync][differentiate_custom_forms_with_labeled_and_unlabeled_models_async]|See the differences in output when using a custom model trained with labeled data and one trained with unlabeled data|

## Contributing
If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines][SDK_README_CONTRIBUTING] for more information.

<!-- LINKS -->
[SDK_README_CONTRIBUTING]: ../../README.md#contributing
[SDK_README_GETTING_STARTED]: ../../README.md#getting-started
[SDK_README_TROUBLESHOOTING]: ../../README.md#troubleshooting
[SDK_README_KEY_CONCEPTS]: ../../README.md#key-concepts
[SDK_README_DEPENDENCY]: ../../README.md#include-the-package
[SDK_README_NEXT_STEPS]: ../../README.md#next-steps
[java_fr_ref_docs]: https://aka.ms/azsdk-java-formrecognizer-ref-docs

[authentication_sample]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/Authentication.java
[differentiate_custom_forms_with_labeled_and_unlabeled_models]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AdvancedDiffLabeledUnlabeledData.java
[differentiate_custom_forms_with_labeled_and_unlabeled_models_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AdvancedDiffLabeledUnlabeledDataAsync.java
[get_bounding_boxes]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/GetBoundingBoxes.java
[get_bounding_boxes_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/GetBoundingBoxesAsync.java
[manage_custom_models]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/ManageCustomModels.java
[manage_custom_models_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/ManageCustomModelsAsync.java
[recognize_receipts]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/RecognizeReceipts.java
[recognize_receipts_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/RecognizeReceiptsAsync.java
[recognize_receipts_from_url]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/RecognizeReceiptsFromUrl.java
[recognize_receipts_from_url_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/RecognizeReceiptsFromUrlAsync.java
[recognize_content]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/RecognizeContent.java
[recognize_content_from_url_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/RecognizeContentFromUrlAsync.java
[recognize_custom_forms]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/RecognizeCustomFormsFromUrl.java
[recognize_custom_forms_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/RecognizeCustomFormsAsync.java
[train_unlabeled_model]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/TrainModelWithoutLabels.java
[train_unlabeled_model_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/TrainModelWithoutLabelsAsync.java
[train_labeled_model]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/TrainModelWithLabels.java
[train_labeled_model_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/TrainModelWithLabelsAsync.java
[copy_model]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/CopyModel.java
[copy_model_async]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/CopyModelAsync.java
[strongly_typed_sample]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/StronglyTypedRecognizedForm.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fformrecognizer%2Fazure-ai-formrecognizer%2FREADME.png)
