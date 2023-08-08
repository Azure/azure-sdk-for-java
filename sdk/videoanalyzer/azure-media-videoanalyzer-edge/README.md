# Deprecated. Azure Video Analyzer Edge client library for Java

Deprecated. We’re retiring the Azure Video Analyzer preview service, you're advised to transition your applications off of Video Analyzer by 01 December 2022. This SDK is not longer maintained. 

Azure Video Analyzer is an [Azure Applied AI Service][applied-ai-service] that provides a platform for you to build intelligent video applications that can span both edge and cloud infrastructures. The platform offers the capability to capture, record, and analyze live video along with publishing the results, video and video analytics, to Azure services at the edge or in the cloud. It is designed to be an extensible platform, enabling you to connect different video inferencing edge modules such as Cognitive services modules, or custom inferencing modules that have been trained with your own data using either open-source machine learning or [Azure Machine Learning][machine-learning].

Use the client library for Video Analyzer Edge to:

-   Simplify interactions with the [Microsoft Azure IoT SDKs](https://github.com/azure/azure-iot-sdks)
-   Programmatically construct pipeline topologies and live pipelines

[Product documentation][doc_product] | [Direct methods][doc_direct_methods] | [Source code][source]

## Getting started

### Prerequisites

-   A Java Development Kit, version 8 or later.
-   You need an active [Azure subscription][azure_sub] and a IoT device connection string to use this package.
-   To interact with Azure IoT Hub you will need to add their dependency to your `pom.xml`

### Include the package

Install the Azure Video Analyzer Edge client library for Java with Maven:

#<!-- {x-version-update;com.microsoft.azure.sdk.iot:iot-service-client;external_dependency} -->

```xml
<dependency>
 <groupId>com.microsoft.azure.sdk.iot</groupId>
 <artifactId>iot-service-client</artifactId>
 <version>1.28.0</version>
</dependency>
<dependency>
 <groupId>com.azure</groupId>
 <artifactId>azure-media-videoanalyzer-edge</artifactId>
 <version>1.0.0-beta.3</version>
</dependency>
```

-   You will need to use the version of the SDK that corresponds to the version of the Video Analyzer edge module you are using.

    | SDK          | Video Analyzer edge module |
    | ------------ | -------------------------- |
    | 1.0.0-beta.5 | 1.1                        |
    | 1.0.0-beta.4 | 1.0                        |
    | 1.0.0-beta.3 | 1.0                        |
    | 1.0.0-beta.2 | 1.0                        |
    | 1.0.0-beta.1 | 1.0                        |

### Creating a pipeline topology and making requests

Please visit the [Examples](#examples) for starter code.

## Key concepts

### Pipeline topology vs live pipeline

A _pipeline topology_ is a blueprint or template for creating live pipelines. It defines the parameters of the pipeline using placeholders as values for them. A _live pipeline_ references a pipeline topology and specifies the parameters. This way you are able to have multiple live pipelines referencing the same topology but with different values for parameters. For more information please visit [pipeline topologies and live pipelines][doc_pipelines].

## Examples

### Creating a pipeline topology

To create a pipeline topology you need to define sources and sinks.

```java readme-sample-buildPipelineTopology
private static PipelineTopology buildPipeLineTopology() {
    IotHubMessageSource msgSource = new IotHubMessageSource("iotMsgSource")
        .setHubInputName("${hubSourceInput}");

    UsernamePasswordCredentials creds = new UsernamePasswordCredentials("${rtspUsername}", "${rtspPassword}");

    UnsecuredEndpoint endpoint = new UnsecuredEndpoint("${rtspUrl}")
        .setCredentials(creds);

    RtspSource rtspSource = new RtspSource("rtspSource", endpoint);

    NodeInput rtspInput = new NodeInput("rtspSource");

    OutputSelector rtspOutputSelector = new OutputSelector()
        .setProperty(OutputSelectorProperty.MEDIA_TYPE)
        .setOperator(OutputSelectorOperator.IS)
        .setValue("video");
    ImageScale imageScale = new ImageScale()
        .setMode(ImageScaleMode.PRESERVE_ASPECT_RATIO)
        .setHeight("416")
        .setWidth("416");
    ImageFormatBmp imageFormat = new ImageFormatBmp();
    ImageProperties image = new ImageProperties()
        .setScale(imageScale)
        .setFormat(imageFormat);
    ExtensionProcessorBase httpExtension = new HttpExtension("inferenceClient", Arrays.asList(rtspInput), endpoint, image);

    NodeInput nodeInput = new NodeInput("inferenceClient");

    IotHubMessageSink msgSink = new IotHubMessageSink("msgSink",
        Arrays.asList(nodeInput),
        "${hubSinkOutputName}");

    ParameterDeclaration userName = new ParameterDeclaration("rtspUserName", ParameterType.STRING);

    ParameterDeclaration password = new ParameterDeclaration("rtspPassword", ParameterType.SECRET_STRING);
    ParameterDeclaration url = new ParameterDeclaration("rtspUrl", ParameterType.STRING);
    ParameterDeclaration hubOutput = new ParameterDeclaration("hubSinkOutputName", ParameterType.STRING);

    PipelineTopologyProperties pipeProps = new PipelineTopologyProperties()
        .setParameters(Arrays.asList(userName, password, url, hubOutput))
        .setSources(Arrays.asList(rtspSource))
        .setSinks(Arrays.asList(msgSink))
        .setProcessors(Arrays.asList(httpExtension));

    return new PipelineTopology(TOPOLOGY_NAME)
        .setProperties(pipeProps);
}
```

### Creating a live pipeline

To create a live pipeline, you need to have an existing pipeline topology.

```java readme-sample-buildLivePipeline
private static LivePipeline buildLivePipeline() {
    ParameterDefinition hubParam = new ParameterDefinition("hubSinkOutputName")
        .setValue("testHubOutput");
    ParameterDefinition userParam = new ParameterDefinition("rtspUserName")
        .setValue("testuser");
    ParameterDefinition urlParam = new ParameterDefinition("rtspUrl")
        .setValue("rtsp://sample-url-from-camera");
    ParameterDefinition passParam = new ParameterDefinition("rtspPassword")
        .setValue("testpass");

    LivePipelineProperties livePipelineProps = new LivePipelineProperties()
        .setParameters(Arrays.asList(urlParam, userParam, passParam, hubParam))
        .setTopologyName(TOPOLOGY_NAME);

    return new LivePipeline(LIVE_PIPELINE_NAME)
        .setProperties(livePipelineProps);
}
```

### Invoking a direct method

```java readme-sample-invokeDirectMethodHelper
private static MethodResult invokeDirectMethodHelper(DeviceMethod client, String methodName, String payload) throws IOException, IotHubException {
    MethodResult result = null;
    try {
        result = client.invoke(iothubDeviceid, iothubModuleid, methodName, null, null, payload);
    } catch (IotHubException e) {
        System.out.println("An error has occurred.");
        System.out.println(e.toString());
    }

    return result;
}
```

```java readme-sample-setPipelineTopologyRequest
PipelineTopologySetRequest setPipelineTopologyRequest = new PipelineTopologySetRequest(pipelineTopology);
MethodResult setPipelineResult = invokeDirectMethodHelper(dClient, setPipelineTopologyRequest.getMethodName(), setPipelineTopologyRequest.getPayloadAsJson());
System.out.println(setPipelineResult.getPayload());
```

## Troubleshooting

When sending a method request using the IoT Hub's `invoke` remember to not type in the method request name directly. Instead use `MethodRequestName.getMethodName()`


## Next steps

-   [Samples][samples]
-   [Azure IoT Device SDK][iot-device-sdk]
-   [Azure IoTHub Service SDK][iot-hub-sdk]

## Contributing

This project welcomes contributions and suggestions. Most contributions require
you to agree to a Contributor License Agreement (CLA) declaring that you have
the right to, and actually do, grant us the rights to use your contribution.
For details, visit https://cla.microsoft.com.

If you encounter any issues, please open an issue on our [Github][github-page-issues].

When you submit a pull request, a CLA-bot will automatically determine whether
you need to provide a CLA and decorate the PR appropriately (e.g., label,
comment). Simply follow the instructions provided by the bot. You will only
need to do this once across all repos using our CLA.

This project has adopted the
[Microsoft Open Source Code of Conduct][code_of_conduct]. For more information,
see the Code of Conduct FAQ or contact opencode@microsoft.com with any
additional questions or comments.

<!-- LINKS -->

[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_sub]: https://azure.microsoft.com/free/
[cla]: https://cla.microsoft.com
[code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[package]: https://aka.ms/ava/sdk/client/java
[samples]: https://aka.ms/video-analyzer-sample
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/videoanalyzer
[doc_direct_methods]: https://go.microsoft.com/fwlink/?linkid=2162396
[doc_pipelines]: https://go.microsoft.com/fwlink/?linkid=2162396
[doc_product]: https://go.microsoft.com/fwlink/?linkid=2162396
[iot-device-sdk]: https://search.maven.org/search?q=a:iot-service-client
[iot-hub-sdk]: https://github.com/Azure/azure-iot-sdk-java
[github-page-issues]: https://github.com/Azure/azure-sdk-for-java/issues
[applied-ai-service]: https://azure.microsoft.com/product-categories/applied-ai-services/#services
[machine-learning]: https://azure.microsoft.com/services/machine-learning

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fvideoanalyzer%2Fazure-media-videoanalyzer-edge%2FREADME.png)
