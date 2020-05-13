# Release History

## 1.0.0-beta.3 (Unreleased)


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
