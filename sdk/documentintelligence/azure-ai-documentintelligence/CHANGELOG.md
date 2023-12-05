# Release History

## 1.0.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.1 (2023-11-16)

_**Note: [Form Recognizer has been rebranded to Document Intelligence](https://mixedrealitywiki.com/display/VTI/Document+Intelligence+2023-10-31-preview#DocumentIntelligence20231031preview-Rebranding)**_

This marks the first preview of `azure-ai-documentintelligence` client library for the `Azure AI Document
Intelligence` service (formerly known as Form Recognizer), targeting service API version `"2023-10-31-preview"`.

It is developer-friendly and idiomatic to the Java ecosystem. The principles that guide our efforts can be found in the
[Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).
For more information about this, and preview releases of other Azure SDK libraries, please visit
https://azure.github.io/azure-sdk/releases/latest/java.html.

### Features Added

- [azure-ai-documentintelligence](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/documentintelligence/azure-ai-documentintelligence) 
  is the new package, replacing [azure-ai-formrecognizer](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/formrecognizer/azure-ai-formrecognizer)
  package. The new package supports a REST level client, which is part of the new generation of Azure SDKs to simplify 
  the development experience.
  The new package is not compatible with the previous `azure-ai-formrecognizer` package without necessary changes to your code.
- The new `"2023-10-31-preview"` service version comes with some new features
  and a few [breaking changes](https://mixedrealitywiki.com/display/VTI/Document+Intelligence+2023-10-31-preview#DocumentIntelligence20231031preview-BreakingChanges)
  when compared to the API versions supported by the `azure-ai-formrecognizer` library.
  - [Markdown content format](https://mixedrealitywiki.com/display/VTI/Document+Intelligence+2023-10-31-preview#DocumentIntelligence20231031preview-MarkdownConversion)
  
    Supports output with Markdown content format along with the default plain _text_. For now, this is only supported for 
    "prebuilt-layout". Markdown content format is deemed a more friendly format for LLM consumption in a chat or 
    automation use scenario. Service follows the GFM spec ([GitHub Flavored Markdown](https://github.github.com/gfm/))
    for the Markdown format. Also introduces a new _contentFormat_ property with value "text" or "markdown" to indicate 
    the result content format.

  - [Query Fields](https://mixedrealitywiki.com/display/VTI/Document+Intelligence+2023-10-31-preview#DocumentIntelligence20231031preview-QueryFields)

    When this feature flag is specified, the service will further extract the values of the fields specified via the 
    `queryFields` query parameter to supplement any existing fields defined by the model as fallback.

  - [Split Options](https://mixedrealitywiki.com/display/VTI/Document+Intelligence+2023-10-31-preview#DocumentIntelligence20231031preview-SplitOptions)

    In the previous API versions supported by the older `azure-ai-formrecognizer` library, document splitting and 
    classification operation (`"/documentClassifiers/{classifierId}:analyze"`) always tried to split the input file 
    into multiple documents.

    To enable a wider set of scenarios, service introduces a "split" query parameter with the new "2023-10-31-preview" 
    service version. The following values are supported:

    - `split: "auto"`

      Let service determine where to split.

    - `split: "none"`

      The entire file is treated as a single document. No splitting is performed.

    - `split: "perPage"`

      Each page is treated as a separate document. Each empty page is kept as its own document.

### Breaking Changes

- The SDKs targeting service API version `2023-10-31-preview` have renamed the clients to
    - `DocumentIntelligenceAdministrationClient`/`DocumentIntelligenceAdministrationAsyncClient`
    - `DocumentIntelligenceClient`/`DocumentIntelligenceAsyncClient`
  which is different from older `azure-ai-formrecognizer` SDKs which targeting service API version `2023-07-31` and `2022-08-31`.
    - `DocumentAnalysisClient`/`DocumentAnalysisAsyncClient`
    - `DocumentModelAdministrationClient`/`DocumentModelAdministrationAsyncClient`
  
- **prebuilt-receipt** - Currency related fields have been updated. Currency symbol ("$") and code ("USD") are returned along with the amount as shown below.

  ```json
  {
    "content": "$123.45",
    "confidence": 0.995,
    "type": "currency",
    "valueCurrency": {
      "amount": 123.45,
      "currencySymbol": "$",
      "currencyCode": "USD"
    }
  }
  ...
  ```
- API shapes have been designed from scratch to support the new Rest level client for the Document Intelligence service.
  Please refer to the [README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/README.md)
  and [Samples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/README.md) 
  for more understanding.

### Other Changes

#### Retirements/Deprecations

- `"prebuilt-businessCard"` model is retired.
- `"prebuilt-document"` model is retired, this model is essentially `"prebuilt-layout"` with `features=keyValuePairs` specified. _(This is only supported as an optional feature for "prebuilt-layout" and "prebuilt-invoice".)_

If you wish to still use these models, please rely on the older `azure-ai-formrecognizer` library through the older service API versions.
