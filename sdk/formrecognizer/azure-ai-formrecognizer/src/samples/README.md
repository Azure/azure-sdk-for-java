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
|[AnalyzeBusinessCards][analyze_business_cards] and [AnalyzeBusinessCardsAsync][analyze_business_cards_async]|Analyze business cards from an input stream|
|[AnalyzeBusinessCardsFromUrl][analyze_business_cards_from_url] and [AnalyzeBusinessCardsFromUrlAsync][analyze_business_cards_from_url_async]|Analyze business cards from a URL|
|[AnalyzeLayout][analyze_layout] and [AnalyzeLayoutAsync][analyze_layout_async]|Analyze document layout, such as tables, lines, words, and selection marks like radio buttons and check boxes from a file stream|
|[AnalyzeLayoutFromUrl][analyze_layout_from_url] and [AnalyzeLayoutFromUrlAsync][analyze_layout_from_url_async]|Extract document layout such as tables, lines, words, and selection marks like radio buttons and check boxes from a URL|
|[AnalyzeIdentityDocuments][analyze_id_documents] and [AnalyzeIdentityDocumentsAsync][analyze_id_documents_async]|Analyze data from an identity document like a passport or a US drivers license using a prebuilt model|
|[AnalyzeIdentityDocumentsFromUrl][analyze_id_documents_from_url] and [AnalyzeIdentityDocumentsFromUrlAsync][analyze_id_documents_from_url_async]|Analyze data from a URL of a passport or a US drivers license using a prebuilt model|
|[AnalyzeInvoices][analyze_invoices] and [AnalyzeInvoiceAsync][analyze_invoices_async]|Analyze invoices from an input stream|
|[AnalyzeInvoicesFromUrl][analyze_invoices_from_url] and [AnalyzeInvoicesFromUrlAsync][analyze_invoices_from_url_async]|Analyze invoices from a URL|
|[AnalyzeReceipts][analyze_receipts] and [AnalyzeReceiptsAsync][analyze_receipts_async]|Analyze data from a file of a US sales receipt using a prebuilt model|
|[AnalyzeReceiptsFromUrl][analyze_receipts_from_url] and [AnalyzeReceiptsFromUrlAsync][analyze_receipts_from_url_async]|Analyze data from a URL of a US sales receipt using a prebuilt model|
|[AnalyzeCustomDocumentFromUrl][analyze_custom_documents] and [AnalyzeCustomDocumentAsync][analyze_custom_documents_async]|Analyze forms with your custom model|
|[BuildModel][build_model] and [BuildModelAsync][build_model_async]|Build a custom document analysis model|
|[ManageCustomModels][manage_custom_models] and [ManageCustomModelsAsync][manage_custom_models_async]|Manage the custom models in your account|
|[CopyModel][copy_model] and [CopyModelAsync][copy_model_async]|Copy custom model from one Form Recognizer resource to another|
|[CreateComposedModel][create_composed_model] and [CreateComposedModelAsync][create_composed_model_async]|Creates a composed model from a collection of existing built models with labels|
|[GetOperation][get_operation] and [GetOperationAsync][get_operation_async]| Get/list all document model associated with the Form Recognizer resource|

## Troubleshooting
Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps
Check out the [API reference documentation][java_fr_ref_docs] to learn more about
what you can do with the Azure Form Recognizer client library.

## Contributing
If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines][SDK_README_CONTRIBUTING] for more information.

<!-- LINKS -->
[SDK_README_CONTRIBUTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/README.md#contributing
[SDK_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/README.md#getting-started
[SDK_README_TROUBLESHOOTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/README.md#troubleshooting
[SDK_README_KEY_CONCEPTS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/README.md#key-concepts
[SDK_README_DEPENDENCY]: ../../README.md#include-the-package
[SDK_README_NEXT_STEPS]: ../../README.md#next-steps
[java_fr_ref_docs]: https://aka.ms/azsdk-java-formrecognizer-ref-docs
[get-endpoint-instructions]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/formrecognizer/azure-ai-formrecognizer#create-a-form-recognizer-resource
[get-key-instructions]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/formrecognizer/azure-ai-formrecognizer#create-a-form-recognizer-client-using-azurekeycredential

[create_composed_model]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/CreateComposedModel.java
[create_composed_model_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/CreateComposedModelAsync.java
[authentication_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/Authentication.java
[manage_custom_models]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/ManageCustomModels.java
[manage_custom_models_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/ManageCustomModelsAsync.java
[analyze_business_cards]:https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeBusinessCard.java
[analyze_business_cards_async]:https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeBusinessCardAsync.java
[analyze_business_cards_from_url]:https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeBusinessCardFromUrl.java
[analyze_business_cards_from_url_async]:https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeBusinessCardFromUrlAsync.java
[analyze_layout]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeLayout.java
[analyze_layout_async]:https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeLayoutAsync.java
[analyze_layout_from_url]:https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeLayoutFromUrl.java
[analyze_layout_from_url_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeLayoutFromUrlAsync.java
[analyze_custom_documents]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeCustomDocumentFromUrl.java
[analyze_custom_documents_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeCustomDocumentAsync.java
[analyze_id_documents]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeIdentityDocuments.java
[analyze_id_documents_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeIdentityDocumentsAsync.java
[analyze_id_documents_from_url]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeIdentityDocumentsFromUrl.java
[analyze_id_documents_from_url_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeIdentityDocumentsFromUrlAsync.java
[analyze_invoices]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeInvoices.java
[analyze_invoices_async]:  https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeInvoicesAsync.java
[analyze_invoices_from_url]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeInvoicesFromUrl.java
[analyze_invoices_from_url_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeInvoicesFromUrlAsync.java
[analyze_receipts]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeReceipts.java
[analyze_receipts_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeReceiptsAsync.java
[analyze_receipts_from_url]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeReceiptsFromUrl.java
[analyze_receipts_from_url_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeReceiptsFromUrlAsync.java
[build_model]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/BuildModel.java
[build_model_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/BuildModelAsync.java
[copy_model]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/CopyModel.java
[copy_model_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/CopyModelAsync.java
[get_operation]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/GetOperationInfo.java
[get_operation_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/GetOperationInfoAsync.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fformrecognizer%2Fazure-ai-formrecognizer%2FREADME.png)
