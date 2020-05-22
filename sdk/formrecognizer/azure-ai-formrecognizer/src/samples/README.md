---
topic: sample
languages:
  - java
products:
  - azure
  - azure-cognitive-service
---

# Azure Form Recognizer client library for Java Samples

Azure Form Recognizer samples are a set of self-contained Java programs that demonstrate interacting with Azure Form Recognizer service
using the client library. Each sample focuses on a specific scenario and can be executed independently. 

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].

## Examples
The following sections provide code samples covering common scenario operations with the Azure Form Recognizer client library.
- Recognize data from a file of a US sales receipt using a prebuilt model
  - [RecognizeReceipts][recognize_receipts] and [RecognizeReceiptsAsync][recognize_receipts_async]
- Recognize data from a URL of a US sales receipt using a prebuilt model
  - [RecognizeReceiptsFromUrl][recognize_receipts_from_url] and [RecognizeReceiptsFromUrlAsync][recognize_receipts_from_url_async]
- Recognize text and table structures of a document
  - [RecognizeContent][recognize_content] and [RecognizeContentAsync][recognize_content_async]
- Recognize forms with your custom model
  - [RecognizeCustomForms][recognize_custom_forms] and [RecognizeCustomFormsAsync][recognize_custom_forms_async]
- Train a custom model with labeled data
  - [TrainLabeledModel][train_labeled_model] and [TrainLabeledModelAsync][train_labeled_model_async]
- Train a custom model with unlabeled data
  - [TrainUnlabeledModel][train_unlabeled_model] and [TrainUnlabeledModelAsync][train_unlabeled_model_async]
- Manage the custom models in your account
  - [ManageCustomModels][manage_custom_models] and [ManageCustomModelsAsync][manage_custom_models_async]

## Troubleshooting
Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps
Check out the [API reference documentation][java_fr_ref_docs] to learn more about
what you can do with the Azure Form Recognizer client library.
Following section provides code samples for some of the advanced scenarios in Form Recognizer Client library:
- Get info to visualize the outlines of form content and fields, which can be used for manual validation
  - [GetBoundingBoxes][get_bounding_boxes] and [GetBoundingBoxesAsync][get_bounding_boxes_async]
- See the differences in output when using a custom model trained with labeled data and one trained with unlabeled data
  - [AdvancedDiffCustomFormsLabeledUnlabeledData][differentiate_custom_forms_with_labeled_and_unlabeled_models] and [AdvancedDiffCustomFormsLabeledUnlabeledDataAsync][differentiate_custom_forms_with_labeled_and_unlabeled_models_async]

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

[differentiate_custom_forms_with_labeled_and_unlabeled_models]: java/com/azure/ai/formrecognizer/AdvancedDiffLabeledUnlabeledData.java
[differentiate_custom_forms_with_labeled_and_unlabeled_models_async]: java/com/azure/ai/formrecognizer/AdvancedDiffLabeledUnlabeledDataAsync.java
[get_bounding_boxes]: java/com/azure/ai/formrecognizer/GetBoundingBoxes.java
[get_bounding_boxes_async]: java/com/azure/ai/formrecognizer/GetBoundingBoxesAsync.java
[manage_custom_models]: java/com/azure/ai/formrecognizer/ManageCustomModels.java
[manage_custom_models_async]: java/com/azure/ai/formrecognizer/ManageCustomModelsAsync.java
[recognize_receipts]: java/com/azure/ai/formrecognizer/RecognizeReceipts.java
[recognize_receipts_async]: java/com/azure/ai/formrecognizer/RecognizeReceiptsAsync.java
[recognize_receipts_from_url]: java/com/azure/ai/formrecognizer/RecognizeReceiptsFromUrl.java
[recognize_receipts_from_url_async]: java/com/azure/ai/formrecognizer/RecognizeReceiptsFromUrlAsync.java
[recognize_content]: java/com/azure/ai/formrecognizer/RecognizeContent.java
[recognize_content_async]: java/com/azure/ai/formrecognizer/RecognizeContentAsync.java
[recognize_custom_forms]: java/com/azure/ai/formrecognizer/RecognizeCustomForms.java
[recognize_custom_forms_async]: java/com/azure/ai/formrecognizer/RecognizeCustomFormsAsync.java
[train_unlabeled_model]: java/com/azure/ai/formrecognizer/TrainModelWithoutLabels.java
[train_unlabeled_model_async]: java/com/azure/ai/formrecognizer/TrainModelWithoutLabels.java
[train_labeled_model]: java/com/azure/ai/formrecognizer/TrainModelWithLabels.java
[train_labeled_model_async]: java/com/azure/ai/formrecognizer/TrainModelWithLabelsAsync.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fformrecognizer%2Fazure-ai-formrecognizer%2FREADME.png)
