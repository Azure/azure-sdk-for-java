# Azure Image Analysis client library for Java

The Image Analysis service provides AI algorithms for processing images and returning information about their content. In a single service call, you can extract one or more visual features from the image simultaneously, including getting a caption for the image, extracting text shown in the image (OCR) and detecting objects. For more information on the service and the supported visual features, see [Image Analysis overview][image_analysis_overview], and the [Concepts][image_analysis_concepts] page.

Use the Image Analysis client library to:
* Authenticate against the service
* Set what features you would like to extract
* Upload an image for analysis, or send an image URL
* Get the analysis result

[Product documentation][image_analysis_overview] 
| [Samples][samples]
| [Vision Studio][vision_studio]
| [API reference documentation](https://learn.microsoft.com/java/api/com.azure.ai.vision.imageanalysis)
| [Maven Package](https://mvnrepository.com/artifact/com.azure/azure-ai-vision-imageanalysis)
| [SDK source code][sdk_source_code]

## Getting started

### Prerequisites

*  [Java Development Kit (JDK)](https://learn.microsoft.com/azure/developer/java/fundamentals/java-jdk-install) with version 8 or above.
* An [Azure subscription](https://azure.microsoft.com/free).
* A [Computer Vision resource](https://portal.azure.com/#create/Microsoft.CognitiveServicesComputerVision) in your Azure subscription.
  * You will need the key and endpoint from this resource to authenticate against the service.
  * You can use the free pricing tier (`F0`) to try the service, and upgrade later to a paid tier for production.
  * Note that in order to run Image Analysis with the `Caption` or `Dense Captions` features, the Azure resource needs to be from one of the following GPU-supported regions: `East US`, `France Central`, `Korea Central`, `North Europe`, `Southeast Asia`, `West Europe`, or `West US`.

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-vision-imageanalysis;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-vision-imageanalysis</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Set environment variables

To authenticate the `ImageAnalysisClient`, you will need the endpoint and key from your Azure Computer Vision resource in the [Azure Portal](https://portal.azure.com). The code snippet below assumes these values are stored in environment variables:

* Set the environment variable `VISION_ENDPOINT` to the endpoint URL. It has the form `https://your-resource-name.cognitiveservices.azure.com`, where `your-resource-name` is your unique Azure Computer Vision resource name.

* Set the environment variable `VISION_KEY` to the key. The key is a 32-character Hexadecimal number.

### Create and authenticate the client

Once you define the environment variables, this Java code will create and authenticate a synchronous `ImageAnalysisClient`:

```java imports-for-create-client-snippet
import com.azure.core.credential.KeyCredential;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClient;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClientBuilder;
```
```java create-client-snippet
String endpoint = System.getenv("VISION_ENDPOINT");
String key = System.getenv("VISION_KEY");

if (endpoint == null || key == null) {
    System.out.println("Missing environment variable 'VISION_ENDPOINT' or 'VISION_KEY'.");
    System.out.println("Set them before running this sample.");
    System.exit(1);
}

// Create a synchronous Image Analysis client.
ImageAnalysisClient client = new ImageAnalysisClientBuilder()
    .endpoint(endpoint)
    .credential(new KeyCredential(key))
    .buildClient();
```

A synchronous client supports synchronous analysis methods, meaning they will block until the service responds with analysis results. The code snippets below all use synchronous methods because it's easier for a getting-started guide. The SDK offers equivalent asynchronous APIs which are often preferred. To create an `ImageAnalysisAsyncClient`, simply  `import com.azure.ai.vision.imageanalysis.ImageAnalysisAsyncClient` and call `buildAsyncClient()` instead
of `buildClient()`:

```java create-async-client-snippet
// Create an asynchronous Image Analysis client.
ImageAnalysisAsyncClient client = new ImageAnalysisClientBuilder()
    .endpoint(endpoint)
    .credential(new KeyCredential(key))
    .buildAsyncClient();
```

## Key concepts

### Visual features

Once you've initialized an `ImageAnalysisClient`, you need to select one or more visual features to analyze. The options are specified by the enum class `VisualFeatures`. The following features are supported:

1. `VisualFeatures.CAPTION` ([Examples](#generate-an-image-caption-for-an-image-file) | [Samples][samples]): Generate a human-readable sentence that describes the content of an image.
1. `VisualFeatures.READ` ([Examples](#extract-text-from-the-image-file) | [Samples][samples]): Also known as Optical Character Recognition (OCR). Extract printed or handwritten text from images. **Note**: For extracting text from PDF, Office, and HTML documents and document images, use the Document Intelligence service with the [Read model](https://learn.microsoft.com/azure/ai-services/document-intelligence/concept-read?view=doc-intel-4.0.0). This model is optimized for text-heavy digital and scanned documents with an asynchronous REST API that makes it easy to power your intelligent document processing scenarios. This service is separate from the Image Analysis service and has its own SDK.
1. `VisualFeatures.DENSE_CAPTIONS` ([Samples][samples]): Dense Captions provides more details by generating one-sentence captions for up to 10 different regions in the image, including one for the whole image. 
1. `VisualFeatures.TAGS` ([Samples][samples]): Extract content tags for thousands of recognizable objects, living beings, scenery, and actions that appear in images.
1. `VisualFeatures.OBJECTS` ([Samples][samples]): Object detection. This is similar to tagging, but focused on detecting physical objects in the image and returning their location.
1. `VisualFeatures.SMART_CROPS` ([Samples][samples]): Used to find a representative sub-region of the image for thumbnail generation, with priority given to include faces.
1. `VisualFeatures.PEOPLE` ([Samples][samples]): Detect people in the image and return their location.

For more information about these features, see [Image Analysis overview][image_analysis_overview], and the [Concepts][image_analysis_concepts] page.

### Analyze from image buffer or URL

The `ImageAnalysisClient` has two overloads for the method `analyze`:
* Analyze an image from a memory buffer, using the [BinaryData](https://learn.microsoft.com/java/api/com.azure.core.util.binarydata) object. The client will upload the image to the service as the REQUEST body. 
* Analyze an image from a publicly-accessible URL, using the [java.lang.URL](https://docs.oracle.com/en/java/javase/20/docs/api/java.base/java/net/URL.html) object. The client will send the image URL to the service. The service will fetch the image.

The examples below show how to do both. The `analyze` examples populate the input `BinaryData` object by loading an image from a file on disk.

### Supported image formats

Image Analysis works on images that meet the following requirements:
* The image must be presented in JPEG, PNG, GIF, BMP, WEBP, ICO, TIFF, or MPO format
* The file size of the image must be less than 20 megabytes (MB)
* The dimensions of the image must be greater than 50 x 50 pixels and less than 16,000 x 16,000 pixels


## Examples

The following sections provide code snippets covering these common Image Analysis scenarios:

* [Generate an image caption for an image file](#generate-an-image-caption-for-an-image-file)
* [Generate an image caption for an image URL](#generate-an-image-caption-for-an-image-url)
* [Extract text (OCR) from an image file](#extract-text-from-an-image-file)
* [Extract text (OCR) from an image URL](#extract-text-from-an-image-url)

These snippets use the synchronous `client` from [Create and authenticate the client](#3-create-and-authenticate-the-client).

See the [Samples][samples] folder for fully working samples for all visual features, including asynchronous clients.

### Generate an image caption for an image file

This example demonstrates how to generate a one-sentence caption for the image file `sample.jpg` using the `ImageAnalysisClient`. The synchronous (blocking) `analyze` method call returns an `ImageAnalysisResult` object.
A call to `getCaption()` on this result will return a `CaptionResult` object.
It contains the generated caption and its confidence score in the range [0, 1]. By default the caption may contain gender terms such as "man", "woman", or "boy", "girl". You have the option to request gender-neutral terms such as "person" or "child" by setting `genderNeutralCaption` to `true` when calling `analyze`, as shown in this example.

Notes:
* Caption is only available in some Azure regions. See [Prerequisites](#prerequisites).
* Caption is only supported in English at the moment.

```java caption-file-snippet
ImageAnalysisResult result = client.analyze(
    BinaryData.fromFile(new File("sample.jpg").toPath()), // imageBuffer: Image file loaded into memory as BinaryData
    Arrays.asList(VisualFeatures.CAPTION), // visualFeatures
    new ImageAnalysisOptionsBuilder().setGenderNeutralCaption(true).build()); // options:  Set to 'true' or 'false' (relevant for CAPTION or DENSE_CAPTIONS visual features)

// Print analysis results to the console
System.out.println("Image analysis results:");
System.out.println(" Caption:");
System.out.println("   \"" + result.getCaption().getText() + "\", Confidence " 
    + String.format("%.4f", result.getCaption().getConfidence()));
```

To generate captions for additional images, simply call the `analyze` multiple times. You can use the same `ImageAnalysisClient` do to multiple analysis calls.

### Generate an image caption for an image URL

This example is similar to the above, expect it calls the `analyze` method and provides a [publicly accessible image URL](https://aka.ms/azai/vision/image-analysis-sample.jpg) instead of a file name.

```java caption-url-snippet
ImageAnalysisResult result = client.analyze(
    new URL("https://aka.ms/azai/vision/image-analysis-sample.jpg"), // imageUrl: the URL of the image to analyze
    Arrays.asList(VisualFeatures.CAPTION), // visualFeatures
    new ImageAnalysisOptionsBuilder().setGenderNeutralCaption(true).build()); // options:  Set to 'true' or 'false' (relevant for CAPTION or DENSE_CAPTIONS visual features)

// Print analysis results to the console
System.out.println("Image analysis results:");
System.out.println(" Caption:");
System.out.println("   \"" + result.getCaption().getText() + "\", Confidence "
    + String.format("%.4f", result.getCaption().getConfidence()));
```

### Extract text from an image file

This example demonstrates how to extract printed or hand-written text for the image file `sample.jpg` using the `ImageAnalysisClient`. The synchronous (blocking) `analyze` method call returns an `ImageAnalysisResult` object.
A call to `getRead()` on the result will return a `ReadResult` object.
It includes a list of text lines and a bounding polygon surrounding each text line. For each line, it also returns a list of words in the text line and a bounding polygon surrounding each word.

```java ocr-file-snippet
ImageAnalysisResult result = client.analyze(
    BinaryData.fromFile(new File("sample.jpg").toPath()), // imageBuffer: Image file loaded into memory as BinaryData
    Arrays.asList(VisualFeatures.READ), // visualFeatures
    null); // options: There are no options for READ visual feature

// Print analysis results to the console
System.out.println("Image analysis results:");
System.out.println(" Read:");
for (DetectedTextLine line : result.getRead().getBlocks().get(0).getLines()) {
    System.out.println("   Line: '" + line.getText()
        + "', Bounding polygon " + line.getBoundingPolygon());
    for (DetectedTextWord word : line.getWords()) {
        System.out.println("     Word: '" + word.getText()
            + "', Bounding polygon " + word.getBoundingPolygon()
            + ", Confidence " + String.format("%.4f", word.getConfidence()));
    }
}
```
To extract text for additional images, simply call the `analyze` multiple times. You can use the same ImageAnalysisClient do to multiple analysis calls.

**Note**: For extracting text from PDF, Office, and HTML documents and document images, use the Document Intelligence service with the [Read model](https://learn.microsoft.com/azure/ai-services/document-intelligence/concept-read?view=doc-intel-4.0.0). This model is optimized for text-heavy digital and scanned documents with an asynchronous REST API that makes it easy to power your intelligent document processing scenarios. This service is separate from the Image Analysis service and has its own SDK.

### Extract text from an image URL

This example is similar to the above, expect it calls the `analyze` method and provides a [publicly accessible image URL](https://aka.ms/azai/vision/image-analysis-sample.jpg) instead of a file name.

```java ocr-url-snippet
ImageAnalysisResult result = client.analyze(
    new URL("https://aka.ms/azai/vision/image-analysis-sample.jpg"), // imageContent: the URL of the image to analyze
    Arrays.asList(VisualFeatures.READ), // visualFeatures
    null); // options: There are no options for READ visual feature

// Print analysis results to the console
System.out.println("Image analysis results:");
System.out.println(" Read:");
for (DetectedTextLine line : result.getRead().getBlocks().get(0).getLines()) {
    System.out.println("   Line: '" + line.getText()
        + "', Bounding polygon " + line.getBoundingPolygon());
    for (DetectedTextWord word : line.getWords()) {
        System.out.println("     Word: '" + word.getText()
            + "', Bounding polygon " + word.getBoundingPolygon()
            + ", Confidence " + String.format("%.4f", word.getConfidence()));
    }
}
```

## Troubleshooting

### Exceptions

The `analyze` methods throw exceptions from [com.azure.core.exception](https://learn.microsoft.com/java/api/com.azure.core.exception) when the service responds with a non-success HTTP status code. The exception's `getResponse().getStatusCode()` will hold the HTTP response status code. The exception's `getMessage()` contains a detailed message that will allow you to diagnose the issue:

```java
try {
    ImageAnalysisResult result = client.analyze(...)
} catch (HttpResponseException e) {
    System.out.println("Exception: " + e.getClass().getSimpleName());
    System.out.println("Status code: " + e.getResponse().getStatusCode());
    System.out.println("Message: " + e.getMessage());
} catch (Exception e) {
    System.out.println("Message: " + e.getMessage());
}
```

For example, when you provide a wrong authentication key:
```
Exception: ClientAuthenticationException
Status code: 401
Message: Status code 401, "{"error":{"code":"401","message":"Access denied due to invalid subscription key or wrong API endpoint. Make sure to provide a valid key for an active subscription and use a correct regional API endpoint for your resource."}}"
```

Or when you provide an image in a format that is not recognized:
```
Exception: HttpResponseException
Status code: 400
Message: Status code 400, "{"error":{"code":"InvalidRequest","message":"Image format is not valid.","innererror":{"code":"InvalidImageFormat","message":"Input data is not a valid image."}}}"
```

### Enable HTTP request/response logging

Reviewing the HTTP request sent or response received over the wire to the Image Analysis service can be useful in troubleshooting. To enable console logging, create the `ImageAnalysisClient` by setting `httpLogOptions` in the the builder:

```java
ImageAnalysisClient client = new ImageAnalysisClientBuilder()
    .endpoint(endpoint)
    .credential(new KeyCredential(key))
    .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
    .buildClient();
```

The enum [HttpLogDetailLevel](https://learn.microsoft.com/java/api/com.azure.core.http.policy.httplogdetaillevel) defines the supported logging levels.

By default, when logging, certain HTTP header values are redacted. It is possible to override this default by specifying which headers values are safe to log:
```java
ImageAnalysisClient client = new ImageAnalysisClientBuilder()
    .endpoint(endpoint)
    .credential(new KeyCredential(key))
    .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS).addAllowedHeaderName("safe-to-log-header-name"))
    .buildClient();
```

## Next steps

* Have a look at the [Samples][samples] folder, containing fully runnable Java code for Image Analysis (all visual features, synchronous and asynchronous clients, from image file or URL).

## Contributing

This project welcomes contributions and suggestions. Most contributions require
you to agree to a Contributor License Agreement (CLA) declaring that you have
the right to, and actually do, grant us the rights to use your contribution.
For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether
you need to provide a CLA and decorate the PR appropriately (e.g., label,
comment). Simply follow the instructions provided by the bot. You will only
need to do this once across all repos using our CLA.

This project has adopted the
[Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct). For more information,
see the Code of Conduct FAQ or contact opencode@microsoft.com with any
additional questions or comments.

<!-- LINKS -->
[image_analysis_overview]: https://learn.microsoft.com/azure/ai-services/computer-vision/overview-image-analysis?tabs=4-0
[image_analysis_concepts]: https://learn.microsoft.com/azure/ai-services/computer-vision/concept-tag-images-40
[vision_studio]: https://portal.vision.cognitive.azure.com/gallery/imageanalysis
[samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples)
[sdk_source_code](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/vision/azure-ai-vision-imageanalysis/src/main/java/com/azure/ai/vision/imageanalysis)

