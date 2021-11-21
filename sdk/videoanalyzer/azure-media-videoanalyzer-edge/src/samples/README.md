# Azure Video Analyzer Edge client library for Java

This document explains samples and how to use them.

## Getting started

Getting started explained in detail [here][sdk_readme_getting_started].

## Key concepts

Key concepts are explained in detail [here][sdk_readme_key_concepts].

## Examples

### Creating a pipeline topology

To create a pipeline topology you need to define parameters, sources, and sinks.

<!-- embedme C:\azure-sdk-for-java\sdk\videoanalyzer\azure-media-videoanalyzer-edge\src\samples\java\com\azure\media\videoanalyzer\edge\LvaInvokeModuleSample.java#L25-L72 -->

```java
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

    IotHubMessageSink msgSink = new IotHubMessageSink("msgSink", Arrays.asList(nodeInput),"${hubSinkOutputName}");

    ParameterDeclaration userName = new ParameterDeclaration("rtspUserName", ParameterType.STRING);

    ParameterDeclaration password = new ParameterDeclaration("rtspPassword", ParameterType.SECRET_STRING);
    ParameterDeclaration url = new ParameterDeclaration("rtspUrl", ParameterType.STRING);
    ParameterDeclaration hubOutput = new ParameterDeclaration("hubSinkOutputName", ParameterType.STRING);

    PipelineTopologyProperties pipeProps = new PipelineTopologyProperties()
        .setParameters(Arrays.asList(userName, password, url, hubOutput))
        .setSources(Arrays.asList(rtspSource))
        .setSinks(Arrays.asList(msgSink))
        .setProcessors(Arrays.asList(httpExtension));

    PipelineTopology pipelineTopology = new PipelineTopology(TOPOLOGY_NAME)
        .setProperties(pipeProps);

    return pipelineTopology;
}
```

### Creating a live pipeline

To create a live pipeline instance, you need to have an existing pipeline topology.

<!-- embedme C:\azure-sdk-for-java\sdk\videoanalyzer\azure-media-videoanalyzer-edge\src\samples\java\com\azure\media\videoanalyzer\edge\LvaInvokeModuleSample.java#L74-L92 -->

```java
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

    LivePipeline livePipeline = new LivePipeline(LIVE_PIPELINE_NAME)
        .setProperties(livePipelineProps);

    return livePipeline;
}
```

### Invoking a pipeline method request

<!-- embedme C:\azure-sdk-for-java\sdk\videoanalyzer\azure-media-videoanalyzer-edge\src\samples\java\com\azure\media\videoanalyzer\edge\LvaInvokeModuleSample.java#L94-L104 -->

```java
private static MethodResult invokeDirectMethodHelper(DeviceMethod client, String methodName, String payload) throws IOException, IotHubException {
    MethodResult result = null;
    try {
        result = client.invoke(DEVICE_ID, MODULE_ID, methodName, null, null, payload);
    } catch (IotHubException e) {
        System.out.println("An error has occurred.");
        System.out.println(e.toString());
    }

    return result;
}
```

<!-- embedme C:\azure-sdk-for-java\sdk\videoanalyzer\azure-media-videoanalyzer-edge\src\samples\java\com\azure\media\videoanalyzer\edge\LvaInvokeModuleSample.java#L111-L112 -->

```java
PipelineTopologySetRequest setPipelineTopologyRequest = new PipelineTopologySetRequest(pipelineTopology);
MethodResult setPipelineResult = invokeDirectMethodHelper(dClient, setPipelineTopologyRequest.getMethodName(), setPipelineTopologyRequest.getPayloadAsJson());
```

## Troubleshooting

Troubleshooting steps can be found [here][sdk_readme_troubleshooting].

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
[package]: TODO://link-to-published-package
[source]: TODO://link-to-published-source
[doc_direct_methods]: TODO://lilink-to-published-docnk
[doc_product]: TODO://link-to-published-doc
[doc_pipeline]: TODO://link-to-published-doc
[iot_device_connection_string]: TODO://link-to-published-doc
[iot-device-sdk]: https://search.maven.org/search?q=a:iot-service-client
[iot-hub-sdk]: https://github.com/Azure/azure-iot-sdk-java
[github-page-issues]: https://github.com/Azure/azure-sdk-for-java/issues
[sdk_readme_key_concepts]: TODO://link-to-published-readme
[sdk_readme_getting_started]: TODO://link-to-published-readme
[sdk_readme_troubleshooting]: TODO://link-to-published-readme
[samples]: TODO://link-to-published-samples

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fvideoanalyzer%2Fazure-media-videoanalyzer-edge%2FREADME.png)
