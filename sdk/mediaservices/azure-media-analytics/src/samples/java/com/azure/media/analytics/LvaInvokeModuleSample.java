
package com.azure.media.analytics;

import com.azure.core.util.serializer.JsonSerializerProviders;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.media.analytics.models.*;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import java.io.IOException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.util.Arrays;

public class LvaInvokeModuleSample {

    private static final String connectionString = "connectionString" ;
    private static final String deviceId = "lva-sample-device";
    private static final String moduleId = "mediaEdge";
    private static final String topologyName = "javaPipelineTopology";
    private static final String livePipelineName = "javaLivePipeline";

    private static PipelineTopology buildPipeLineTopology() {
        Source msgSource = new IotHubMessageSource()
            .setHubInputName("${hubSourceInput}")
            .setName("iotMsgSource");

        UsernamePasswordCredentials creds = new UsernamePasswordCredentials()
            .setUsername("${rtspUsername}")
            .setPassword("${rtspPassword}");
        Endpoint endpoint = new UnsecuredEndpoint()
            .setUrl("${rtspUrl}")
            .setCredentials(creds);
        RtspSource rtspSource = new RtspSource()
            .setEndpoint(endpoint);
        rtspSource.setName("rtspSource");

        NodeInput rtspInput = new NodeInput()
            .setNodeName("rtspSource");

        OutputSelector rtspOutputSelector = new OutputSelector()
            .setProperty(OutputSelectorProperty.MEDIA_TYPE)
            .setOperator(OutputSelectorOperator.IS)
            .setValue("video");
        ImageScale imageScale = new ImageScale()
            .setMode(ImageScaleMode.PRESERVE_ASPECT_RATIO)
            .setHeight("416")
            .setWidth("416");
        ImageFormatBmp imageFormat = new ImageFormatBmp();
        Image image = new Image()
            .setScale(imageScale)
            .setFormat(imageFormat);
        ExtensionProcessorBase httpExtension = new HttpExtension()
            .setEndpoint(endpoint)
            .setImage(image);
        httpExtension.setName("inferenceClient");
        httpExtension.setInputs(Arrays.asList(rtspInput));

        NodeInput nodeInput = new NodeInput()
            .setNodeName("inferenceClient");

        IotHubMessageSink msgSink = new IotHubMessageSink()
            .setHubOutputName("${hubSinkOutputName}");
        msgSink.setName("msgSink");
        msgSink.setInputs(Arrays.asList(nodeInput));

        ParameterDeclaration userName = new ParameterDeclaration()
            .setName("rtspUserName")
            .setType(ParameterType.STRING);
        ParameterDeclaration password = new ParameterDeclaration()
            .setName("rtspPassword")
            .setType(ParameterType.SECRET_STRING);
        ParameterDeclaration url = new ParameterDeclaration()
            .setName("rtspUrl")
            .setType(ParameterType.STRING);
        ParameterDeclaration hubOutput = new ParameterDeclaration()
            .setName("hubSinkOutputName")
            .setType(ParameterType.STRING);

        PipelineTopologyProperties pipeProps = new PipelineTopologyProperties()
            .setParameters(Arrays.asList(userName, password, url, hubOutput))
            .setSources(Arrays.asList(rtspSource))
            .setSinks(Arrays.asList(msgSink))
            .setProcessors(Arrays.asList(httpExtension));

        PipelineTopology pipelineTopology = new PipelineTopology()
            .setName(topologyName)
            .setProperties(pipeProps);

        return pipelineTopology;
    }

    private static LivePipeline buildLivePipeline() {
        ParameterDefinition hubParam = new ParameterDefinition()
            .setName("hubSinkOutputName")
            .setValue("testHubOutput");
        ParameterDefinition userParam = new ParameterDefinition()
            .setName("rtspUserName")
            .setValue("testuser");
        ParameterDefinition urlParam = new ParameterDefinition()
            .setName("rtspUrl")
            .setValue("rtsp://sample-url-from-camera");
        ParameterDefinition passParam = new ParameterDefinition()
            .setName("rtspPassword")
            .setValue("testpass");

        LivePipelineProperties livePipelineProps = new LivePipelineProperties()
            .setParameters(Arrays.asList(urlParam, userParam, passParam, hubParam))
            .setTopologyName(topologyName);

        LivePipeline livePipeline = new LivePipeline()
        .setName(livePipelineName)
        .setProperties(livePipelineProps);

        return livePipeline;
    }

    private static MethodResult invokeDirectMethodHelper(DeviceMethod client, String methodName, String payload) throws IOException, IotHubException {
        //CompletableFuture<Void> future = client.openAsync();
        //future.get();
        //Message msg = new Message(payload);
        //client.send(deviceId, moduleId, msg);
        return client.invoke(deviceId, moduleId, methodName, null, null, payload);
    }

    public static void main(String[] args) throws IOException, IotHubException {
        PipelineTopology pipelineTopology = buildPipeLineTopology();
        LivePipeline livePipeline = buildLivePipeline();
        DeviceMethod dClient = DeviceMethod.createFromConnectionString(connectionString);

        PipelineTopologySetRequest setPipelineTopologyRequest = new PipelineTopologySetRequest()
            .setPipelineTopology(pipelineTopology);
        MethodResult setPipelineResult = invokeDirectMethodHelper(dClient, setPipelineTopologyRequest.getMethodName(), setPipelineTopologyRequest.getPayloadAsJson());
        System.out.println(setPipelineResult);

        PipelineTopologyGetRequest getTopologyRequest = new PipelineTopologyGetRequest();
        getTopologyRequest.setName(pipelineTopology.getName());
        MethodResult getTopologyResult = invokeDirectMethodHelper(dClient, getTopologyRequest.getMethodName(), getTopologyRequest.getPayloadAsJson());
        System.out.println(getTopologyResult);

        PipelineTopologyListRequest listTopologyRequest = new PipelineTopologyListRequest();
        MethodResult listPipelineResult = invokeDirectMethodHelper(dClient, listTopologyRequest.getMethodName(), listTopologyRequest.getPayloadAsJson());
        System.out.println(listPipelineResult);

        LivePipelineSetRequest setLivePipelineRequest = new LivePipelineSetRequest();
        setLivePipelineRequest.setLivePipeline(livePipeline);
        MethodResult setLivePipelineResult = invokeDirectMethodHelper(dClient, setLivePipelineRequest.getMethodName(), setLivePipelineRequest.getPayloadAsJson());
        System.out.println(setLivePipelineResult);

        LivePipelineListRequest listLivePipelineRequest = new LivePipelineListRequest();
        MethodResult liveLivePipelineResult = invokeDirectMethodHelper(dClient, listLivePipelineRequest.getMethodName(), listLivePipelineRequest.getPayloadAsJson());

        LivePipelineActivateRequest activateLivePipelineRequest = new LivePipelineActivateRequest();
        activateLivePipelineRequest.setName(livePipeline.getName());
        MethodResult activateLivePipelineResult = invokeDirectMethodHelper(dClient,activateLivePipelineRequest.getMethodName(), activateLivePipelineRequest.getPayloadAsJson());

        LivePipelineGetRequest getLivePipelineRequest = new LivePipelineGetRequest();
        getLivePipelineRequest.setName(livePipeline.getName());
        MethodResult getLivePipelineResult = invokeDirectMethodHelper(dClient, getLivePipelineRequest.getMethodName(), getLivePipelineRequest.getPayloadAsJson());

        LivePipelineDeactivateRequest deactivateLivePipelineRequest = new LivePipelineDeactivateRequest();
        deactivateLivePipelineRequest.setName(livePipeline.getName());
        MethodResult deactivateLivePipelineResult = invokeDirectMethodHelper(dClient, deactivateLivePipelineRequest.getMethodName(), deactivateLivePipelineRequest.getPayloadAsJson());

        LivePipelineDeleteRequest deleteLivePipelineRequest = new LivePipelineDeleteRequest();
        deleteLivePipelineRequest.setName(livePipeline.getName());
        MethodResult deleteLivePipelineResult = invokeDirectMethodHelper(dClient, deleteLivePipelineRequest.getMethodName(), deleteLivePipelineRequest.getPayloadAsJson());

        PipelineTopologyDeleteRequest deletePipelineRequest = new PipelineTopologyDeleteRequest();
        deletePipelineRequest.setName(livePipeline.getName());
        MethodResult deletePipelineResult = invokeDirectMethodHelper(dClient, deletePipelineRequest.getMethodName(), deleteLivePipelineRequest.getPayloadAsJson());

    }
}
