# Release History

## 1.0.0-beta.1 (2023-11-16)

Version 1.0.0-beta.1 is a preview of our efforts in creating an Azure AI Document Intelligence client library that is developer-friendly
and idiomatic to the Java ecosystem. The principles that guide
our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

For more information about this, and preview releases of other Azure SDK libraries, please visit
https://azure.github.io/azure-sdk/releases/latest/java.html.

- It uses the Document Intelligence service `2023-10-31-preview` API.
- Two client design:
    - `DocumentIntelligenceClient` to analyze fields/values on custom documents, receipts, and document layout
    - `DocumentIntelligenceAdministrationClient` to build custom models, manage the custom models on your account and build classifiers
- Authentication with API key supported using `AzureKeyCredential("<api_key>")` from `com.azure.core.credential`
- Reactive streams support using [Project Reactor](https://projectreactor.io/).

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-ai-documentintelligence_1.0.0-beta.1/sdk/formrecognizer/azure-ai-documentintelligence/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-ai-documentintelligence_1.0.0-beta.1/sdk/formrecognizer/azure-ai-documentintelligence/src/samples)
demonstrate the new API.
