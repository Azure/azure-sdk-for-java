# Azure Ink Recognizer client library for Java

The Ink Recognizer Cognitive Service provides a cloud-based REST API to analyze and recognize digital ink content. Unlike services that use Optical Character Recognition (OCR), the API requires digital ink stroke data as input. Digital ink strokes are time-ordered series of 2D points (X,Y coordinates) that represent the motion of input tools such as digital pens or fingers. It then recognizes the shapes and handwritten content from the input and returns a JSON response containing all recognized entities. To know more about the service, please see [Product documentation][inkrecognizer_docs]

With the Ink Recognizer SDK, you can easily connect to the Azure Ink Recognizer service and recognize handwritten content in your applications. Here are the features you can utilize:

* Handwriting recognition
* Layout recognition
* Shape recognition
* Combined shapes and text recognition

This SDK will:

* Take your ink stroke data and format it into valid JSON.
* Send a request to the Ink Recognizer API with your data.
* Process the API response by parsing the returned JSON message.

## Getting started

### Prerequisites

* Java Development Kit (JDK) with version 8 or above
* You must have a Cognitive Services API account. Please refer the [Ink Recognizer API][inkrecognizer_api] if you don't have one already.

## Key concepts

You will need to start off by adding this sdk library as a dependency to your project.

### Implement InkStroke and InkPoint

The InkStroke interface represents an ink stroke (a collection of ink points from the time a user places the writing instrument on the writing surface until the instrument is lifted). You will be expected to implement this interface so that the InkRecognizer Client object can use it to translate the ink to JSON for delivery to the Ink Recognizer service.

```Java
public interface InkStroke {
     Iterable<InkPoint> getInkPoints();
     InkStrokeKind getKind();
     long getId();
     String getLanguage();
} 
```

The InkPoint interface represents a single position on the path of an ink stroke. You are expected to implement this interface as well.

```Java
public interface InkPoint {
    float getX();
    float getY();
}
```

The StrokeKind enum represents the class a stroke belongs to. You are expected to set this value when it is known with absolute certainty. The default value is InkStrokeKind.UNKNOWN.

```Java
public enum InkStrokeKind {
    UNKNOWN("unknown"),
    DRAWING("inkDrawing"),
    WRITING("inkWriting");
}
```

### Create client

You will need to then create an InkRecognizerClient object, an example is as follows:

```Java
InkRecognizerClientBuilder inkRecognizerClientBuilder = new InkRecognizerClientBuilder();
InkRecognizerClient inkRecognizerClient = inkRecognizerClientBuilder.credentials(credential)
                .endpoint(endpoint)
                .buildClient();
```

or an InkRecognizerAsyncClient object as shown as:

```Java
InkRecognizerClientBuilder inkRecognizerClientBuilder = new InkRecognizerClientBuilder();
InkRecognizerAsyncClient inkRecognizerAsyncClient = inkRecognizerClientBuilder.credentials(credential)
                .endpoint(endpoint)
                .buildAsyncClient();
```

### Send request

You can then send the strokes for processing to the service through the client such as:

```Java
InkRecognitionRoot root = inkRecognizerClient.recognizeInk(strokes);
```

### Use the response

You can call methods on the InkRecognitionRoot obtained from before. For example, you can search for specific recognized words in the recognized text as follows:

```Java
Iterable<InkWord> wordsFound = root.findWord("hello");
```

## Examples

Putting all of this together, here is how you can use the sdk for searching words:

```Java
InkRecognizerClientBuilder inkRecognizerClientBuilder = new InkRecognizerClientBuilder();
InkRecognizerClient inkRecognizerClient = inkRecognizerClientBuilder.credentials(credential)
                .endpoint(endpoint)
                .buildClient();
InkRecognitionRoot root = inkRecognizerClient.recognizeInk(strokes);
Iterable<InkWord> wordsFound = root.findWord("hello");
```

## Troubleshooting

File an issue via [Github Issues][github_issues].

## Next steps

Please refer to the code to view the different ways you can use this sdk.

## Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

<!-- LINKS -->
[github_issues]: https://github.com/Azure/azure-sdk-for-java/issues/new/choose
[inkrecognizer_api]: https://docs.microsoft.com/en-us/azure/cognitive-services/ink-recognizer/overview
[inkrecognizer_docs]: https://docs.microsoft.com/en-us/azure/cognitive-services/ink-recognizer/