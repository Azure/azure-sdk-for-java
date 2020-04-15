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
  - [sample_recognize_receipts][sample_recognize_receipts] and [sample_recognize_receipts_async][sample_recognize_receipts_async]
- Recognize data from a URL of a US sales receipt using a prebuilt model
  - [sample_recognize_receipts_from_url][sample_recognize_receipts_from_url] and [sample_recognize_receipts_from_url_async][sample_recognize_receipts_from_url_async]|
- Recognize text and table structures of a document
  - [sample_recognize_content][sample_recognize_content] and [sample_recognize_content_async][sample_recognize_content_async]
- Recognize forms with your custom model
  - [sample_recognize_custom_forms][sample_recognize_custom_forms] and [sample_recognize_custom_forms_async][sample_recognize_custom_forms_async]
- Train a custom model with labeled data
  - [sample_train_labeled_model][sample_train_labeled_model] and [sample_train_labeled_model_async][sample_train_labeled_model_async]
- Train a custom model with unlabeled data
  - [sample_train_unlabeled_model][sample_train_unlabeled_model] and [sample_train_unlabeled_model_async][sample_train_unlabeled_model_async]
- Manage the custom models in your account
 - [sample_manage_custom_models][sample_manage_custom_models] and [sample_manage_custom_models_async][sample_manage_custom_models_async]

## Troubleshooting
Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps
See [Next steps][SDK_README_NEXT_STEPS].
Following section provides code samples for some of the advanced scenarios in Form Recognizer Client library:
- Get info to help with manually validating the output of the `FormRecognizer` client
  - [sample_get_manual_validation_info][sample_get_manual_validation_info] and [sample_get_manual_validation_info_async][sample_get_manual_validation_info_async]
- See the differences in output when using a custom model trained with labeled data and one trained with unlabeled data
 - [sample_differentiate_custom_forms_with_labeled_and_unlabeled_models][sample_differentiate_custom_forms_with_labeled_and_unlabeled_models] and [sample_differentiate_custom_forms_with_labeled_and_unlabeled_models_async][sample_differentiate_custom_forms_with_labeled_and_unlabeled_models_async]

## Contributing
If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines][SDK_README_CONTRIBUTING] for more information.

<!-- LINKS -->
[KEYS_SDK_README]: ../../README.md
[SDK_README_CONTRIBUTING]: ../../README.md#contributing
[SDK_README_GETTING_STARTED]: ../../README.md#getting-started
[SDK_README_TROUBLESHOOTING]: ../../README.md#troubleshooting
[SDK_README_KEY_CONCEPTS]: ../../README.md#key-concepts
[SDK_README_DEPENDENCY]: ../../README.md#include-the-package
[SDK_README_NEXT_STEPS]: ../../README.md#next-steps

[sample_differentiate_custom_forms_with_labeled_and_unlabeled_models]: java/com/azure/ai/formrecognizer/advanced/DiffCustomFormsLabeledUnlabeledData.java
[sample_differentiate_custom_forms_with_labeled_and_unlabeled_models_async]: java/com/azure/ai/formrecognizer/advanced/DiffCustomFormsLabeledUnlabeledDataAsync.java
[sample_get_manual_validation_info]: java/com/azure/ai/formrecognizer/advanced/GetManualValidationInfo.java
[sample_get_manual_validation_info_async]: java/com/azure/ai/formrecognizer/advanced/GetManualValidationInfoAsync.java
[sample_manage_custom_models]: java/com/azure/ai/formrecognizer/ManageCustomModels.java
[sample_manage_custom_models_async]: java/com/azure/ai/formrecognizer/ManageCustomModelsAsync.java
[sample_recognize_receipts]: java/com/azure/ai/formrecognizer/RecognizeReceipts.java
[sample_recognize_receipts_async]: java/com/azure/ai/formrecognizer/RecognizeReceiptsAsync.java
[sample_recognize_receipts_from_url]: java/com/azure/ai/formrecognizer/RecognizeReceiptsFromUrl.java
[sample_recognize_receipts_from_url_async]: java/com/azure/ai/formrecognizer/RecognizeReceiptsFromUrlAsync.java
[sample_recognize_content]: java/com/azure/ai/formrecognizer/RecognizeContent.java
[sample_recognize_content_async]: java/com/azure/ai/formrecognizer/RecognizeContentAsync.java
[sample_recognize_custom_forms]: java/com/azure/ai/formrecognizer/RecognizeCustomForms.java
[sample_recognize_custom_forms_async]: java/com/azure/ai/formrecognizer/RecognizeCustomFormsAsync.java
[sample_train_unlabeled_model]: java/com/azure/ai/formrecognizer/TrainUnlabeledCustomModel.java
[sample_train_unlabeled_model_async]: java/com/azure/ai/formrecognizer/TrainUnlabeledCustomModelAsync.java
[sample_train_labeled_model]: java/com/azure/ai/formrecognizer/TrainLabeledCustomModel.java
[sample_train_labeled_model_async]: java/com/azure/ai/formrecognizer/TrainLabeledCustomModelAsync.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fformrecognizer%2Fazure-ai-formrecognizer%2FREADME.png)
