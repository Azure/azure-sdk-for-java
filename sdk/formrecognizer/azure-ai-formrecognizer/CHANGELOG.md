# Release History

## 3.1.0-beta.1 (Unreleased)


## 3.0.8 (2021-05-13)
### Dependency Updates
- Updated `azure-core` from `1.15.0` to `1.16.0`.
- Updated `azure-core-http-netty` from `1.9.1` to `1.9.2`.
- Updated `azure-core-serializer-json-jackson` from `1.2.2` to `1.2.3`.

## 3.0.7 (2021-04-07)
### Dependency Updates
- Update dependency version, `azure-core` to `1.15.0`, `azure-core-http-netty` to `1.9.1` and `azure-identity` to `1.2.5`.

## 3.0.6 (2021-03-10)
### Dependency Updates
- Update dependency version, `azure-core` to `1.14.0`, `azure-core-http-netty` to `1.9.0` and `azure-identity` to `1.2.4`.

## 3.0.5 (2021-02-09)
### Dependency Updates
- Update dependency version, `azure-core` to `1.13.0`, `azure-core-http-netty` to `1.8.0` and `azure-identity` to `1.2.3`.
- 
## 3.0.4 (2021-01-14)
### Dependency Updates
- Update dependency version, `azure-core` to `1.12.0`, `azure-core-http-netty` to `1.7.1` and `azure-identity` to `1.2.2`.

## 3.0.3 (2020-11-10)
### Dependency Updates
- Update dependency version, `azure-core`, `azure-core-http-netty` and `azure-identity`.

## 3.0.2 (2020-10-06)
### Dependency Updates
- Update dependency version, `azure-core` to 1.9.0 and `azure-core-http-netty` to 1.6.2.

## 3.0.1 (2020-09-10)
### Dependency Updates
- Updated dependency version, `azure-core` to 1.8.1 and `azure-core-http-netty` to 1.6.1.

## 3.0.0 (2020-08-20)
First stable release of the azure-ai-formrecognizer client library supporting Azure Form Recognizer service API version v2.0.

### Breaking Changes
- Renamed `BoundingBox` model to `FieldBoundingBox`

## 3.0.0-beta.1 (2020-08-11)
This beta version targets Azure Form Recognizer service API version v2.0.

### Breaking Changes
- Updated version number to 3.0.0-beta.1 from 1.0.0-beta.4.
- Added models `RecognizeCustomFormOptions`, `RecognizeReceiptOptions`, `RecognizeContentOptions` and
`TrainingOptions` to support passing configurable options to training and recognize API's.
- Added support for context passing.
- Moved training client models under `com.azure.ai.formrecognizer.training.models` namespace
- Renamed accessors for property `includeFieldElements` to `isFieldElementsIncluded` and `setFieldElementsIncluded`
- Renamed property `type` on `FieldValue` model to `valueType`
- Renamed property `formWords` on `FormLine` model to `words`
- Renamed property `code` on `FormRecognizerError` model to `errorCode`
- Renamed accessors for property `includeSubFolders` to `isSubfoldersIncluded` and `setSubfoldersIncluded`
- Renamed property `trainingStatus` and `documentErrors` on `TrainingDocumentInfo` model to `status` and `errors` 
respectively
- Renamed property `formPageRange` on `RecognizedForm` model to `pageRange`
- Renamed model `ErrorInformation` to `FormRecognizerErrorInformation`
- Renamed model `OperationResult` to `FormRecognizerOperationResult`
- Changed param ordering for methods `beginRecognizeCustomForms` and `beginRecognizeCustomFormsFromUrl`

### Key Bug Fixes
- Fixed `getFields()` to preserve service side ordering of fields.

## 1.0.0-beta.4 (2020-07-07)
### Breaking Changes
- `beginRecognizeReceipt` APIs now return a `RecognizedForm` model instead of a `RecognizedReceipt`. See
[this](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/StronglyTypedRecognizedForm.java)
suggested approach for extracting information from receipts.
- Methods returning `textContent` have been renamed to `fieldElements` on `FieldData` and `FormTableCell`
- Renamed `FormContent` to `FormElement`
- Renamed `FieldText` to `FieldData`
- Renamed properties `requestedOn` to `trainingStartedOn` and `completedOn` to `trainingCompletedOn` on model
- Throw `HttpResponseException` instead of `ErrorResponseException` to model service side exceptions
`CustomFormModel` and `CustomFormModelInfo`.
- Changed `CopyAuthorization.getExpiresOn()` to return a `OffsetDateTime` instead of a `long` value
- Added `RecognizeOptions` to pass configurable options when using recognize APIs on FormRecognizerClient.
- Changed `submodels` property on `CustomFormModel` to return a `List` instead of `IterableStream`
- Renamed `fieldMap` property to `fields` on `CustomFormSubmodel` model
- Renamed `elements` property on model `FormTableCell` to `textContent`
- Renamed `includeTextDetails` references in parameter and model properties to `includeFieldElements`
- Removed `TextContentType` model and use `instanceOf` to detect the FormContent type

### Key Bug Fixes
- Fixed `textAngle` to be returned between `(-180, 180]`.

## 1.0.0-beta.3 (2020-06-10)
### New Features
- Support to copy a custom model from one Form Recognizer resource to another
- Added support for AAD Authentication.
- Raise `FormRecognizerException` when a model with `ModelStatus.Invalid` is returned from the `beginTraining()` API's
- Raise `FormRecognizerException` when an invalid analyze status is returned from the service for recognize API's
- Add `pageNumber` property to `FormPage` and `FormTable` model
- Add `getFormRecognizerClient()` and `getFormRecognizerAsyncClient()` in FormTrainingClient and FormTrainingAsyncClient
- Add `FormTrainingClientBuilder` to build `FormTrainingAsyncClient` and `FormTrainingClient`

### Breaking Changes
- Update FormRecognizer API calls to return a `List` instead of `IterableStream`.
- Adopt the `training` namespace for Form Recognizer Training Clients
- Rename enum type `DimensionUnit` to `LengthUnit` on `FormPage`
- `USReceipt`, `USReceiptItem`, `USReceiptType` and `FormField{T}` types removed. Information about a `RecognizedReceipt`
must now be extracted from its `RecognizedForm`.
- Rename parameters data and sourceUrl parameters found on methods for FormRecognizerClient to form and formUrl, respectively.
- Rename parameters for receipt API methods to receipt and receiptUrl.
- Update FormField property `transactionTime` on `USReceipt` to return `LocalTime` instead of `String`
- Rename model `PageRange` to `FormPageRange`
- Rename property `startPageNumber` to `firstPageNumber` and `endPageNumber` to `lastPageNumber` in model `PageRange`
- Rename `getCustomModelInfos` to `listCustomModels`
- Rename property `lastUpdatedOn` to `completedOn` and `createdOn` to `requestedOn` in `CustomFormModel` and
`CustomFormModelInfo` model
- Rename model `CustomFormSubModel` to `CustomFormSubmodel`
- Rename `subModels` property on CustomFormModel to `submodels`
- Remove `pageNumber` property from `FormField` model
- Rename parameter `fileSourceUrl` to `trainingFilesUrl` on `beginTraining` method in FormTrainingClients
- Rename parameter `useLabelFile` to `useTrainingLabels` on `beginTraining` method in FormTrainingClients
- Replace parameters `filePrefix` and `includeSubFolders` with `TrainingFileFilter` model
- Rename AccountProperties `count` and `limit` to `customModelCount` and `customModelLimit`
- Rename `apiKey()` to `credential()` on FormRecognizerClientBuilder.

### Key Bug Fixes
- Fix bug in FormRecognizer API's to support multipage document recognition.
- Fix Receipt type to select the valueString field via fieldValue.

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-ai-formrecognizer_1.0.0-beta.3/sdk/formrecognizer/azure-ai-formrecognizer/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-ai-formrecognizer_1.0.0-beta.3/sdk/formrecognizer/azure-ai-formrecognizer/src/samples)
demonstrate the new API.

## 1.0.0-beta.2 (2020-05-06)

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
