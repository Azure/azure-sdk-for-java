# Release History

## 4.0.0-beta.1 (Unreleased)

### Features Added
- Added new DocumentAnalysisClient with beginAnalyzeDocument and beginAnalyzeDocumentFromUrl methods. 
Use these methods with the latest Form Recognizer API version to analyze documents, with prebuilt and custom models.
- Added new models to use with the new DocumentAnalysisClient: AnalyzeResult, AnalyzedDocument, BoundingRegion, DocumentElement, 
DocumentEntity, DocumentField, DocumentKeyValuePair, DocumentKeyValueElement, DocumentLine, DocumentPage, DocumentSelectionMark,
DocumentSpan, DocumentStyle, DocumentTable, DocumentTableCell, DocumentWord, DocumentOperationResult.
- Added a new model `DocumentAnalysisServiceVersion` that supports Azure Form Recognizer service version "2021-09-30-preview" and onwards.
- Added new DocumentModelAdministrationClient with methods: beginBuildModel, beginCreateComposedModel, getCopyAuthorization,
deleteModel, getAccountProperties, beginCopyModel, listModels, getModel, getOperation, listOperations.
- Added new models to use with the new DocumentTrainingClient: DocumentModel, DocTypeInfo, DocumentFieldSchema, 
AccountProperties, CopyAuthorization, BuildModelOptions, CopyAuthorizationOptions, CreateComposedModelOptions,
DocumentModelInfo, ModelOperation, ModelOperationInfo, ModelOperationStatus, ModelOperationKind.

### Breaking Changes

### Bugs Fixed

### Other Changes

## 3.1.4 (2021-09-09)
### Other Changes
#### Dependency Updates
- Updated `azure-core` to `1.12.0`.
- Updated `azure-core-http-netty` to `1.11.0`.

## 3.1.3 (2021-08-11)
### Dependency Updates
- Updated `azure-core` to `1.19.0`.
- Updated `azure-core-http-netty` to `1.10.2`.

## 3.1.2 (2021-07-08)
### Other changes
- Updated test infrastructure to remove dependency on network call recorded data.

#### Dependency Updates
- Updated `azure-core` to `1.18.0`.
- Updated `azure-core-http-netty` to `1.10.1`.

## 3.1.1 (2021-06-09)

### Bug Fixes

- Fixed invoices and other recognition operations to avoid Null Pointer Exception when data returned with no
  sub-line item fields detected.
- Fixed invoices and other recognition operations that return a `FormField` with `text`
  and no `boundingBox` or `page` information.

## 3.1.0 (2021-05-26)
### Features Added
- This General Availability (GA) release marks the stability of the changes introduced in package versions `3.1.0-beta.1` through `3.1.0-beta.3`.
- Added `clientOptions()` and `getDefaultLogOptions()` methods to the `FormRecognizerClientBuilder` and `FormTrainingClientBuilder`.
- We are able to support multiple service API versions now: `V2_0` and `V2_1`.
- Add more static values to `FormRecognizerLanguage` expandable string class.

### Breaking Changes
- The client defaults to the latest supported service version, which currently is 2.1.
- The model `TextAppearance` now includes the properties `styleName` and `styleConfidence` that were part of the `TextStyle` object.
- Removed the model `TextStyle`.
- Removed `V2_1_PREVIEW_1` and `V2_1_PREVIEW_2` but only support latest service API version `V2_1`.
- Removed the `pollInterval` property from all endpoints' options bag, such as `RecognizeBusinessCardsOptions`, etc. Polling interval
  can be updated in the Azure Core `SyncPoller` or `PollerFlux`'s method, `setPollInterval()`, synchronously and asynchronously, respectively.
- Removed class type `FieldValueGender`.
- Removed value `Gender` from the model `FieldValuetype`.
- Renamed `ReadingOrder` model to `FormReadingOrder`, and refactor the class to be expandable string class.
- Renamed the method names and the method parameters, using `identity` to replace `id` keyword in the identity documents recognition API.
  For example, renamed `beginRecognizeIdDocuments` to `beginRecognizeIdentityDocuments`.
- Renamed the method `asCountry` to `asCountryRegion`.
- Renamed value `COUNTRY` to `COUNTRY_REGION` in the model `FieldValuetype`.
- Renamed the property `fieldBoundingBox` to `boundingBox` in the class `FormTable`.
- Make `FormLine`, `FormPage`, `FormTable`, `FormSelectionMark`, `TextAppearance`, `CustomFormModel`, `CustomFormModelInfo`, `CustomFormModelProperties`
  `CustomFormSubmodel`, `TrainingDocumentInfo` model class immutable.

## 3.0.8 (2021-05-13)
### Dependency Updates
- Updated `azure-core` from `1.15.0` to `1.16.0`.
- Updated `azure-core-http-netty` from `1.9.1` to `1.9.2`.
- Updated `azure-core-serializer-json-jackson` from `1.2.2` to `1.2.3`.

## 3.0.7 (2021-04-07)
### Dependency updates
- Update dependency version, `azure-core` to `1.15.0`, `azure-core-http-netty` to `1.9.1` and `azure-identity` to `1.2.5`.

## 3.1.0-beta.3 (2021-04-06)
- Defaults to the latest supported API version, which currently is `2.1-preview.3`.
- Added property `Pages` to `RecognizeReceiptsOptions`, `RecognizeInvoicesOptions`, `RecognizeBusinessCardsOptions`
  and `RecognizeCustomFormOptions` to specify the page numbers to analyze.
- Added support for `FormContentType` `image/bmp` when analyzing custom forms.
- Added support for pre-built ID documents recognition.
- Added property `ReadingOrder` to `RecognizeContentOptions` to specify the order in which recognized text lines are returned.

## 3.0.6 (2021-03-10)
### Dependency updates
- Update dependency version, `azure-core` to `1.14.0`, `azure-core-http-netty` to `1.9.0` and `azure-identity` to `1.2.4`.

## 3.1.0-beta.2 (2021-02-10)

### Breaking Changes
- Renamed `Appearance`, `Style` and `TextStyle` models to `TextAppearance`, `TextStyle` and `TextStyleName` respectively.
- Changed the type of `Locale` from `String` to `FormRecognizerLocale` in `RecognizeBusinessCardsOptions`, `RecognizeInvoicesOptions`, and `RecognizeReceiptsOptions`.
- Changed the type of `Language` from `String` to `FormRecognizerLanguage` in `RecognizeContentOptions`.

## 3.0.5 (2021-02-09)
### Dependency updates
- Update dependency version, `azure-core` to `1.13.0`, `azure-core-http-netty` to `1.8.0` and `azure-identity` to `1.2.3`.

## 3.0.4 (2021-01-14)

### Dependency Updates
- Update dependency version, `azure-core` to `1.12.0`, `azure-core-http-netty` to `1.7.1` and `azure-identity` to `1.2.2`.

## 3.1.0-beta.1 (2020-11-23)

### Breaking changes

- Defaults to the latest supported API version, which currently is `2.1-preview.2`.

### New Features

- Added support for pre-built business card recognition.
- Added support for pre-built invoices recognition.
- Added implementation support to create a composed model from the `FormTrainingClient` by calling method `beginCreateComposedModel`.
- Added `language` to `RecognizeContentOptions` for users to specify a preferred language to process the document.
- Added support to `beginRecognizeContent` to recognize selection marks such as check boxes and radio buttons.
- Added support to train and recognize custom forms with selection marks such as check boxes and radio buttons.
This functionality is only available in trained with labels scenarios.
- When passing `includeFieldElements` as true in `RecognizeCustomFormsOptions`, the property `fieldElements` on `FieldData`
and `FormTableCell` will also be populated with any selection marks found on the page.
- Added support for providing locale info when recognizing receipts and business cards.
Supported locales include support EN-US, EN-AU, EN-CA, EN-GB, EN-IN.
- Added property `Appearance` to `FormLine` to indicate the style of the extracted text, for example, "handwriting" or "other".
- Added support for `FormContentType` `image/bmp` in recognize content and prebuilt models.
- Added property `Pages` to `RecognizeContentOptions` to specify the page numbers to analyze.
- Added property `BoundingBox` to `FormTable`.
- Added properties `modelName` and `customFormModelProperties` to types `CustomFormModel` and `CustomFormModelInfo`.
- Added property `modelName` to `TrainingOptions` and new type `CreateComposedModelOptions`.
- Added property `modelId` to `CustomFormSubmodel` and `TrainingDocumentInfo`.
- Added properties `modelId` and `formTypeConfidence` to `RecognizedForm`.

## 3.0.3 (2020-11-10)

### Dependency updates

- Update dependency version, `azure-core`, `azure-core-http-netty` and `azure-identity`.

## 3.0.2 (2020-10-06)
### Dependency updates
- Update dependency version, `azure-core` to 1.9.0 and `azure-core-http-netty` to 1.6.2.

## 3.0.1 (2020-09-10)
### Dependency updates
- Updated dependency version, `azure-core` to 1.8.1 and `azure-core-http-netty` to 1.6.1.

## 3.0.0 (2020-08-20)
First stable release of the azure-ai-formrecognizer client library supporting Azure Form Recognizer service API version v2.0.

### Breaking Changes
- Renamed `BoundingBox` model to `FieldBoundingBox`.

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

### Bug Fixes
- When using a security policy with `SecurityManager` turns on, it no longer throws the access denied error on 
  `java.lang.reflect.ReflectPermission`. More information on https://github.com/Azure/azure-sdk-for-java/issues/17368
  
## 1.0.0-beta.4 (2020-07-07)
### Breaking Changes
- `beginRecognizeReceipt` APIs now return a `RecognizedForm` model instead of a `RecognizedReceipt`. See
[this](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/StronglyTypedRecognizedForm.java)
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
