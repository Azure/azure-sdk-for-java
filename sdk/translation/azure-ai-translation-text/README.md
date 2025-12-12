# Azure Text Translation client library for Java

Azure text translation is a cloud-based REST API provided by the Azure Translator service. It utilizes neural machine translation technology to deliver precise, contextually relevant, and semantically accurate real-time text translations across all supported languages.

Use the Text Translation client library for Java to:

- Retrieve the list of languages supported for translation and transliteration operations, as well as LLM models available for translations.

- Perform deterministic text translation from a specified source language to a target language, with configurable parameters to ensure precision and maintain contextual integrity.

- Execute transliteration by converting text from the original script to an alternative script representation.

- Use LLM models to produce translation output variants that are tone-specific and gender-aware.

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]
- [Product documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- An existing Translator service or Cognitive Services resource.

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-translation-text;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-translation-text</artifactId>
    <version>2.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

Interaction with the service using the client library begins with creating an instance of the [TextTranslationClient][translator_client_class] class. You will need an **API key** or ``TokenCredential`` to instantiate a client object. For more information regarding authenticating with cognitive services, see [Authenticate requests to Translator Service][translator_auth].

#### Get an API key

You can get the `endpoint`, `API key` and `Region` from the Cognitive Services resource or Translator service resource information in the [Azure Portal][azure_portal].

Alternatively, use the [Azure CLI][azure_cli] snippet below to get the API key from the Translator service resource.

```PowerShell
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
```

#### Create a `TextTranslationClient` using an API key and Region credential

Once you have the value for the API key and Region, create an `AzureKeyCredential`. This will allow you to
update the API key without creating a new client.

With the value of the endpoint, `AzureKeyCredential` and a `Region`, you can create the [TextTranslationClient][translator_client_class]:

```java createTextTranslationRestClient
String apiKey = System.getenv("TEXT_TRANSLATOR_API_KEY");
String region = System.getenv("TEXT_TRANSLATOR_API_REGION");
AzureKeyCredential credential = new AzureKeyCredential(apiKey);

TextTranslationClient client = new TextTranslationClientBuilder()
        .credential(credential)
        .region(region)
        .endpoint("https://api.cognitive.microsofttranslator.com")
        .buildClient();
```

## Key concepts

### `TextTranslationClient` and `TextTranslationAsyncClient`

A `TextTranslationClient` is the primary interface for developers using the Text Translator client library. It provides both synchronous operations to access a specific use of text translator, such as get supported languages detection or text translation.

For asynchronous operations use `TextTranslationAsyncClient`.

### Input

A **text element** (`InputTextItem`), is a single unit of input to be processed by the translation models in the Translator service. Operations on `TextTranslationClient` may take a single text element or a collection of text elements.
For text element length limits, maximum requests size, and supported text encoding see [here][translator_limits].

## Examples

The following section provides several code snippets using the `client` [created above](#create-a-texttranslationclient-using-an-api-key-and-region-credential), and covers the main features present in this client library. Although most of the snippets below make use of asynchronous service calls, keep in mind that the `Azure.AI.Translation.Text` package supports both synchronous and asynchronous APIs.

### Get Supported Languages

Gets the set of languages currently supported by other operations of the Translator.

```java getTextTranslationLanguages
GetSupportedLanguagesResult languages = client.getSupportedLanguages();

System.out.println("Number of supported languages for translate operation: " + languages.getTranslation().size() + ".");
System.out.println("Number of supported languages for transliterate operation: " + languages.getTransliteration().size() + ".");
System.out.println("Number of supported models for translate operation: " + languages.getModels().size() + ".");

System.out.println("Translation Languages:");
for (Map.Entry<String, TranslationLanguage> translationLanguage : languages.getTranslation().entrySet()) {
    System.out.println(translationLanguage.getKey() + " -- name: " + translationLanguage.getValue().getName() + " (" + translationLanguage.getValue().getNativeName() + ")");
}

System.out.println("Transliteration Languages:");
for (Map.Entry<String, TransliterationLanguage> transliterationLanguage : languages.getTransliteration().entrySet()) {
    System.out.println(transliterationLanguage.getKey() + " -- name: " + transliterationLanguage.getValue().getName() + ", supported script count: " + transliterationLanguage.getValue().getScripts().size());
}

System.out.println("Available models:");
for (String model : languages.getModels()) {
    System.out.println(model);
}
```

Please refer to the service documentation for a conceptual discussion of [languages][languages_doc].

### Translate

Renders single source-language text to multiple target-language texts with a single request.

```java getTextTranslationMultiple
TranslateInputItem input = new TranslateInputItem(
    "This is a test.", 
    Arrays.asList(new TranslationTarget("es"), new TranslationTarget("fr")));
input.setLanguage("en");

TranslatedTextItem translation = client.translate(Arrays.asList(input)).get(0);

for (TranslationText textTranslation : translation.getTranslations()) {
    System.out.println("Text was translated to: '" + textTranslation.getLanguage() + "' and the result is: '" + textTranslation.getText() + "'.");
}
```

Please refer to the service documentation for a conceptual discussion of [translate][translate_doc].

### Transliterate

Converts characters or letters of a source language to the corresponding characters or letters of a target language.

```java getTextTranslationTransliterate
String language = "zh-Hans";
String fromScript = "Hans";
String toScript = "Latn";
String content = "这是个测试。";

TransliteratedText transliteration = client.transliterate(language, fromScript, toScript, content);

System.out.println("Input text was transliterated to '" + transliteration.getScript() + "' script. Transliterated text: '" + transliteration.getText() + "'.");
```

Please refer to the service documentation for a conceptual discussion of [transliterate][transliterate_doc].


## Troubleshooting

When you interact with the Translator Service using the TextTranslator client library, errors returned by the Translator service correspond to the same HTTP status codes returned for REST API requests.

For example, if you submit a translation request without a target translate language, a `400` error is returned, indicating "Bad Request".

## Next steps

Samples showing how to use this client library are available in this GitHub repository.
Samples are provided for each main functional area, and for each area, samples are provided in both sync and async mode.

* [Translation][sample_translate]
* [Translation with Language Detection][sample_translatedetection]
* [Handling Profanities in Translation][sample_translateprofanity]
* [Translation to multiple languages][sample_translatetargets]
* [Translation of multiple sources][sample_translatesources]
* [Translation and Transliteration][sample_translatetransliteration]
* [Translation using LLM][sample_translatelargelanguagemodel]
* [Using Custom Translation Model][sample_translatecustom]
* [Translation with NoTranslate tag][sample_translatenotranslate]
* [Handling translation of HTML text][sample_translatetexttypes]
* [Transliteration][sample_transliterate]
* [Get Languages][sample_getlanguages]
* [Get Localized Languages][sample_getlanguagesaccept]
* [Get Scoped Languages][sample_getlanguagesscope]

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[product_documentation]: https://learn.microsoft.com/azure/ai-services/translator/text-translation/preview/overview
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity

[azure_cli]: https://learn.microsoft.com/cli/azure
[azure_portal]: https://portal.azure.com

[translator_auth]: https://learn.microsoft.com/azure/ai-services/translator/text-translation/reference/authentication
[translator_limits]: https://learn.microsoft.com/azure/cognitive-services/translator/request-limits

[languages_doc]: https://learn.microsoft.com/azure/ai-services/translator/text-translation/preview/get-languages
[translate_doc]: https://learn.microsoft.com/azure/ai-services/translator/text-translation/preview/translate-api
[transliterate_doc]: https://learn.microsoft.com/azure/ai-services/translator/text-translation/preview/transliterate-api

[sample_getlanguages]: https://github.com/azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-text/src/samples/java/com/azure/ai/translation/text/GetLanguages.java
[sample_getlanguagesaccept]: https://github.com/azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-text/src/samples/java/com/azure/ai/translation/text/GetLanguagesAcceptLanguage.java
[sample_getlanguagesscope]: https://github.com/azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-text/src/samples/java/com/azure/ai/translation/text/GetLanguagesScope.java
[sample_translate]: https://github.com/azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-text/src/samples/java/com/azure/ai/translation/text/Translate.java
[sample_translatelargelanguagemodel]: https://github.com/azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-text/src/samples/java/com/azure/ai/translation/text/TranslateLlm.java
[sample_translatecustom]: https://github.com/azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-text/src/samples/java/com/azure/ai/translation/text/TranslateCustom.java
[sample_translatedetection]: https://github.com/azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-text/src/samples/java/com/azure/ai/translation/text/TranslateDetection.java
[sample_translatesources]: https://github.com/azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-text/src/samples/java/com/azure/ai/translation/text/TranslateMultipleSources.java
[sample_translatetargets]: https://github.com/azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-text/src/samples/java/com/azure/ai/translation/text/TranslateMultipleTargets.java
[sample_translatenotranslate]: https://github.com/azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-text/src/samples/java/com/azure/ai/translation/text/TranslateNoTranslate.java
[sample_translateprofanity]: https://github.com/azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-text/src/samples/java/com/azure/ai/translation/text/TranslateProfanity.java
[sample_translatetexttypes]: https://github.com/azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-text/src/samples/java/com/azure/ai/translation/text/TranslateTextType.java
[sample_translatetransliteration]: https://github.com/azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-text/src/samples/java/com/azure/ai/translation/text/TranslateWithTransliteration.java
[sample_transliterate]: https://github.com/azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-text/src/samples/java/com/azure/ai/translation/text/Transliterate.java

[translator_client_class]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-text/src/main/java/com/azure/ai/translation/text/TextTranslationClient.java
