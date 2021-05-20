// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.media.videoanalyzer.edge;

import com.azure.media.videoanalyzer.edge.models.*;

import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethod;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import java.io.IOException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.util.Arrays;

/***
 * Public class showing code how to utilize the sdk
 */
public class LvaInvokeModuleSample {

    private static final String CONNECTION_STRING = "connectionString";
    private static final String DEVICE_ID = "lva-sample-device";
    private static final String MODULE_ID = "mediaEdge";
    private static final String TOPOLOGY_NAME = "javaPipelineTopology";
    private static final String LIVE_PIPELINE_NAME = "javaLivePipeline";

    /***
     * Build a pipeLine topology including its parameters, sources, and sinks
     * @return PipelineTopology
     */
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

        PipelineTopology pipelineTopology = new PipelineTopology(TOPOLOGY_NAME)
            .setProperties(pipeProps);

        return pipelineTopology;
    }

    /***
     * Build a live pipeline using a pipeline topology
     * @return LivePipeline
     */
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

    /***
     * Helper method to invoke module method on iot hub device
     * @param client Iot Hub Service Client
     * @param methodName method name to invoke on module
     * @param payload payload to send to module
     * @return Result from invoke method call
     * @throws IOException IOException
     * @throws IotHubException IotHubException
     */
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

    /***
     * Main method to run sample
     * @param args args
     * @throws IOException IOException
     * @throws IotHubException IotHubException
     */
    public static void main(String[] args) throws IOException, IotHubException {
        PipelineTopology pipelineTopology = buildPipeLineTopology();
        LivePipeline livePipeline = buildLivePipeline();
        DeviceMethod dClient = DeviceMethod.createFromConnectionString(CONNECTION_STRING);

        PipelineTopologySetRequest setPipelineTopologyRequest = new PipelineTopologySetRequest(pipelineTopology);
        MethodResult setPipelineResult = invokeDirectMethodHelper(dClient, setPipelineTopologyRequest.getMethodName(), setPipelineTopologyRequest.getPayloadAsJson());
        System.out.println(setPipelineResult);

        PipelineTopologyGetRequest getTopologyRequest = new PipelineTopologyGetRequest(pipelineTopology.getName());
        MethodResult getTopologyResult = invokeDirectMethodHelper(dClient, getTopologyRequest.getMethodName(), getTopologyRequest.getPayloadAsJson());
        System.out.println(getTopologyResult);

        PipelineTopologyListRequest listTopologyRequest = new PipelineTopologyListRequest();
        MethodResult listPipelineResult = invokeDirectMethodHelper(dClient, listTopologyRequest.getMethodName(), listTopologyRequest.getPayloadAsJson());
        System.out.println(listPipelineResult);

        LivePipelineSetRequest setLivePipelineRequest = new LivePipelineSetRequest(livePipeline);
        MethodResult setLivePipelineResult = invokeDirectMethodHelper(dClient, setLivePipelineRequest.getMethodName(), setLivePipelineRequest.getPayloadAsJson());
        System.out.println(setLivePipelineResult);

        LivePipelineListRequest listLivePipelineRequest = new LivePipelineListRequest();
        MethodResult liveLivePipelineResult = invokeDirectMethodHelper(dClient, listLivePipelineRequest.getMethodName(), listLivePipelineRequest.getPayloadAsJson());

        LivePipelineActivateRequest activateLivePipelineRequest = new LivePipelineActivateRequest(livePipeline.getName());
        MethodResult activateLivePipelineResult = invokeDirectMethodHelper(dClient, activateLivePipelineRequest.getMethodName(), activateLivePipelineRequest.getPayloadAsJson());

        LivePipelineGetRequest getLivePipelineRequest = new LivePipelineGetRequest(livePipeline.getName());
        MethodResult getLivePipelineResult = invokeDirectMethodHelper(dClient, getLivePipelineRequest.getMethodName(), getLivePipelineRequest.getPayloadAsJson());

        LivePipelineDeactivateRequest deactivateLivePipelineRequest = new LivePipelineDeactivateRequest(livePipeline.getName());
        MethodResult deactivateLivePipelineResult = invokeDirectMethodHelper(dClient, deactivateLivePipelineRequest.getMethodName(), deactivateLivePipelineRequest.getPayloadAsJson());

        LivePipelineDeleteRequest deleteLivePipelineRequest = new LivePipelineDeleteRequest(livePipeline.getName());
        MethodResult deleteLivePipelineResult = invokeDirectMethodHelper(dClient, deleteLivePipelineRequest.getMethodName(), deleteLivePipelineRequest.getPayloadAsJson());

        PipelineTopologyDeleteRequest deletePipelineRequest = new PipelineTopologyDeleteRequest(livePipeline.getName());
        MethodResult deletePipelineResult = invokeDirectMethodHelper(dClient, deletePipelineRequest.getMethodName(), deleteLivePipelineRequest.getPayloadAsJson());

    }
}
