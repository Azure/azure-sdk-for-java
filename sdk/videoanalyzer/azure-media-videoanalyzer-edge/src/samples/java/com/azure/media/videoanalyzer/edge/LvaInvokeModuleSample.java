// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.media.videoanalyzer.edge;

import com.azure.media.videoanalyzer.edge.models.*;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethod;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import java.io.IOException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import java.util.Arrays;

/***
 * Public class showing code how to utilize the sdk
 */
public class LvaInvokeModuleSample {

    private static String iothubConnectionstring = System.getenv("iothub_connectionstring");
    private static String iothubDeviceid = System.getenv("iothub_deviceid");
    private static String iothubModuleid = System.getenv("iothub_moduleid");
    private static final String TOPOLOGY_NAME = "javaPipelineTopology";
    private static final String LIVE_PIPELINE_NAME = "javaLivePipeline";
    private static final String REMOTE_DEVICE_ADAPTER_NAME = "RemoteDeviceAdapterSample1";

    /***
     * Build a pipeLine topology including its parameters, sources, and sinks
     * @return PipelineTopology
     */
    // BEGIN: readme-sample-buildPipelineTopology
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
    // END: readme-sample-buildPipelineTopology

    /***
     * Build a live pipeline using a pipeline topology
     * @return LivePipeline
     */
    // BEGIN: readme-sample-buildLivePipeline
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
    // END: readme-sample-buildLivePipeline

    private static RemoteDeviceAdapter createRemoteDeviceAdapter(String remoteDeviceName, String iotDeviceName) throws IOException, IotHubException {
        RegistryManager registryManager = new RegistryManager(iothubConnectionstring);
        Device iotDevice;
        try {
            iotDevice = registryManager.getDevice(iotDeviceName);
        } catch (IllegalArgumentException e) {
            iotDevice = registryManager.addDevice(Device.createFromId(iotDeviceName, null, null));
        }

        IotHubDeviceConnection iotHubDeviceConnection = new IotHubDeviceConnection(iotDeviceName)
            .setCredentials(new SymmetricKeyCredentials(iotDevice.getPrimaryKey()));

        RemoteDeviceAdapterProperties remoteDeviceAdapterProperties = new RemoteDeviceAdapterProperties(new RemoteDeviceAdapterTarget("camerasimulator"), iotHubDeviceConnection);

        return new RemoteDeviceAdapter(remoteDeviceName)
            .setProperties(remoteDeviceAdapterProperties);
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
    // BEGIN: readme-sample-invokeDirectMethodHelper
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
    // END: readme-sample-invokeDirectMethodHelper

    private static void initializeIotHubCredentials() {
        iothubConnectionstring = System.getenv("iothub_connectionstring");
        iothubDeviceid = System.getenv("iothub_deviceid");
        iothubModuleid = System.getenv("iothub_moduleid");
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
        DeviceMethod dClient = new DeviceMethod(iothubConnectionstring);

        // BEGIN: readme-sample-setPipelineTopologyRequest
        PipelineTopologySetRequest setPipelineTopologyRequest = new PipelineTopologySetRequest(pipelineTopology);
        MethodResult setPipelineResult = invokeDirectMethodHelper(dClient, setPipelineTopologyRequest.getMethodName(), setPipelineTopologyRequest.getPayloadAsJson());
        System.out.println(setPipelineResult.getPayload());
        // END: readme-sample-setPipelineTopologyRequest

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

        RemoteDeviceAdapter remoteDeviceAdapter = createRemoteDeviceAdapter(REMOTE_DEVICE_ADAPTER_NAME, "iotDeviceSample");
        RemoteDeviceAdapterSetRequest remoteDeviceAdapterSetRequest = new RemoteDeviceAdapterSetRequest(remoteDeviceAdapter);
        MethodResult remoteDeviceAdapterSetResult = invokeDirectMethodHelper(dClient, remoteDeviceAdapterSetRequest.getMethodName(), remoteDeviceAdapterSetRequest.getPayloadAsJson());
        System.out.println(remoteDeviceAdapterSetResult);

        RemoteDeviceAdapterGetRequest remoteDeviceGetRequest = new RemoteDeviceAdapterGetRequest(REMOTE_DEVICE_ADAPTER_NAME);
        MethodResult remoteDeviceGetResult = invokeDirectMethodHelper(dClient, remoteDeviceGetRequest.getMethodName(), remoteDeviceGetRequest.getPayloadAsJson());

        RemoteDeviceAdapterListRequest remoteDeviceAdapterListRequest = new RemoteDeviceAdapterListRequest();
        MethodResult remoteDeviceAdapterListResult = invokeDirectMethodHelper(dClient, remoteDeviceAdapterListRequest.getMethodName(), remoteDeviceAdapterListRequest.getPayloadAsJson());

        RemoteDeviceAdapterDeleteRequest remoteDeviceAdapterDeleteRequest = new RemoteDeviceAdapterDeleteRequest(REMOTE_DEVICE_ADAPTER_NAME);
        MethodResult remoteDeviceAdapterDeleteResult =  invokeDirectMethodHelper(dClient, remoteDeviceAdapterDeleteRequest.getMethodName(), remoteDeviceAdapterDeleteRequest.getPayloadAsJson());

        OnvifDeviceGetRequest onvifDeviceGetRequest = new OnvifDeviceGetRequest(new UnsecuredEndpoint("rtsp://camerasimulator:554"));
        MethodResult onvifDeviceGetResult = invokeDirectMethodHelper(dClient, onvifDeviceGetRequest.getMethodName(), onvifDeviceGetRequest.getPayloadAsJson());

        OnvifDeviceDiscoverRequest onvifDeviceDiscoverRequest = new OnvifDeviceDiscoverRequest();
        MethodResult onvifDeviceDiscoverResult = invokeDirectMethodHelper(dClient, onvifDeviceDiscoverRequest.getMethodName(), onvifDeviceGetRequest.getPayloadAsJson());
    }
}
