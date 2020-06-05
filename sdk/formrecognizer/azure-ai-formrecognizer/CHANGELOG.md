# Release History

## 1.0.0-beta.3 (Unreleased)
- Rename enum type `DimensionUnit` to `LengthUnit` on `FormPage`
- `USReceipt`, `USReceiptItem`, `USReceiptType` and `FormField{T}` types removed. Information about a `RecognizedReceipt` 
must now be extracted from its `RecognizedForm`.
- Rename parameters data and sourceUrl parameters found on methods for FormRecognizerClient to form and formUrl, respectively.
- Rename parameters for receipt API methods to receipt and receiptUrl.
- Raise `FormRecognizerException` when a model with `ModelStatus.Invalid` is returned from the `beginTraining()` API's
- Raise `FormRecognizerException` when invalid analyze status returned from service for recognize API's
- Update FormField property `transactionTime` on `USReceipt` to return `LocalTime` instead of `String`
- Rename model `PageRange` to `FormPageRange`
- Rename property `startPageNumber` to `firstPageNumber` and `endPageNumber` to `lastPageNumber` in model `PageRange`
- Rename `getCustomModelInfos` to `listCustomModels`
- Rename property `lastUpdatedOn` to `completedOn` and `createdOn` to `requestedOn` in `CustomFormModel` and
`CustomFormModelInfo` model
- Rename model `CustomFormSubModel` to `CustomFormSubmodel`
- Rename `subModels` property on CustomFormModel to `submodels`
- Add `pageNumber` property to `FormPage` and `FormTable` model
- Remove `pageNumber` property from `FormField` model
- Update FormRecognizer API calls to return a `List` instead of `IterableStream`.
- Fix bug in FormRecognizer API's to support multipage document recognition.
- Add `getFormRecognizerClient()` and `getFormRecognizerAsyncClient()` in FormTrainingClient and FormTrainingAsyncClient
- Add `FormTrainingClientBuilder` to build `FormTrainingAsyncClient` and `FormTrainingClient`
- Adopt the `training` namespace for Form Recognizer Training Clients
- Rename parameter `fileSourceUrl` to `trainingFilesUrl` on `beginTraining` method in FormTrainingClients
- Rename parameter `useLabelFile` to `useTrainingLabels` on `beginTraining` method in FormTrainingClients
- Replace parameters `filePrefix` and `includeSubFolders` with `TrainingFileFilter` model
- Rename AccountProperties `count` and `limit` to `customModelCount` and `customModelLimit`
- Added support for AAD Authentication.

### New Features
- Support to copy a custom model from one Form Recognizer resource to another

## 1.0.0-beta.2 (2020-05-06)
- Fixed Receipt type bug to select the valueString field via fieldValue.
- Rename `apiKey()` to `credential()` on FormRecognizerClientBuilder.

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-ai-formrecognizer_1.0.0-beta.2/sdk/formrecognizer/azure-ai-formrecognizer/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-ai-formrecognizer_1.0.0-beta.2/sdk/formrecognizer/azure-ai-formrecognizer/src/samples)
demonstrate the new API.

## 1.0.0-beta.1 (2020-04-23)
Version 1.0.0-beta.1 is a preview of our efforts in creating a Azure Form Recognizer client library that is developer-friendly
and idiomatic to the Java ecosystem. The principles that guide
our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

For more information about this, and preview releases of other Azure SDK libraries, please visit
https://azure.github.io/azure-sdk/releases/latest/java.html.

- It uses the Form Recognizer service `v2.0-preview.1` API.
- Two client design:
    - `FormRecognizerClient` to analyze fields/values on custom forms, receipts, and form content/layout
    - `FormTrainingClient` to train custom models (with/without labels), and manage the custom models on your account
- Different analyze methods based on input type: file stream or URL.
    - URL input should use the method with suffix `fromUrl`
    - Stream methods will automatically detect content-type of the input file if not provided.
- Authentication with API key supported using `AzureKeyCredential("<api_key>")` from `com.azure.core.credential`
- All service errors use the base type: `com.azure.ai.formrecognizer.models.ErrorResponseException`
- Reactive streams support using [Project Reactor](https://projectreactor.io/).

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-ai-formrecognizer_1.0.0-beta.1/sdk/formrecognizer/azure-ai-formrecognizer/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-ai-formrecognizer_1.0.0-beta.1/sdk/formrecognizer/azure-ai-formrecognizer/src/samples)
demonstrate the new API.
