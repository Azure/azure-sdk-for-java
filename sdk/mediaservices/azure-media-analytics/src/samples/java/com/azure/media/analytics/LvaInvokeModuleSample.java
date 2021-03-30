
package com.azure.media.analytics;

import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

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
    private static final String moduleId = "lvaEdge";
    private static final IotHubServiceClientProtocol protocol =
        IotHubServiceClientProtocol.AMQPS;
    private static final String topologyName = "javaPipelineTopology";
    private static final String livePipelineName = "javaLivePipeline";

    //where/how to add methodName to payload? methodName is apart of class so when serialized is called here, methodName gets serialized into payload
    private static String serialize(MethodRequest request) {
        JsonSerializer serializer = JsonSerializerProviders.createInstance();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.serialize(outputStream, request);
        String payload = outputStream.toString(StandardCharsets.UTF_8);

        return payload;
    }

    private static PipelineTopology buildPipeLineTopology() {
        RtspSource rtspSource = new RtspSource();
        UnsecuredEndpoint endpoint = new UnsecuredEndpoint();
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials();
        creds.setUsername("${rtspUsername}");
        creds.setPassword("${rtspPassword}");
        endpoint.setCredentials(creds);
        rtspSource.setEndpoint(endpoint);

        NodeInput nodeInput = new NodeInput();
        AssetSink assetSink = new AssetSink();
        assetSink.setName("assetSink");
        assetSink.setInputs(Arrays.asList(nodeInput));
        assetSink.setAssetContainerSasUrl("sampleAsset-${System.GraphTopologyName}-${System.GraphInstanceName}");
        assetSink.setLocalMediaCachePath("/var/lib/azuremediaservices/tmp/");
        assetSink.setLocalMediaCacheMaximumSizeMiB("2048");

        PipelineTopology pipelineTopology = new PipelineTopology();
        pipelineTopology.setName(topologyName);
        PipelineTopologyProperties pipeProps = new PipelineTopologyProperties();
        ParameterDeclaration userName = new ParameterDeclaration();
        userName.setName("rtspUserName");
        userName.setType(ParameterType.STRING);
        ParameterDeclaration password = new ParameterDeclaration();
        password.setName("rtspPassword");
        password.setType(ParameterType.SECRET_STRING);
        ParameterDeclaration url = new ParameterDeclaration();
        url.setName("rtspUrl");
        url.setType(ParameterType.STRING);
        pipeProps.setParameters(Arrays.asList(userName, password, url));
        pipeProps.setSources(Arrays.asList(rtspSource));
        pipeProps.setSinks(Arrays.asList(assetSink));

        pipelineTopology.setProperties(pipeProps);

        return pipelineTopology;
    }

    private static LivePipeline buildLivePipeline() {
        ParameterDefinition urlParam = new ParameterDefinition();
        urlParam.setName("rtspUrl");
        urlParam.setValue("rtsp://sample-url-from-camera");

        ParameterDefinition passParam = new ParameterDefinition();
        passParam.setName("rtspPassword");
        passParam.setValue("testpass");

        LivePipelineProperties livePipelineProps = new LivePipelineProperties();
        livePipelineProps.setParameters(Arrays.asList(urlParam, passParam));
        livePipelineProps.setTopologyName(topologyName);

        LivePipeline livePipeline = new LivePipeline();
        livePipeline.setName(livePipelineName);
        livePipeline.setProperties(livePipelineProps);

        return livePipeline;
    }

    private static MethodResult invokeDirectMethod(DeviceMethod client, String payload) throws IOException, IotHubException {
        //ServiceClient client = ServiceClient.createFromConnectionString(connectionString, protocol);
        //CompletableFuture<Void> future = client.openAsync();
        //future.get();
        //Message msg = new Message(payload);
        //client.send(deviceId, moduleId, msg);
        return client.invoke(deviceId, moduleId, null, null, payload);
    }
    public static void main(String[] args) throws IOException, IotHubException {
        PipelineTopology pipelineTopology = buildPipeLineTopology();
        LivePipeline livePipeline = buildLivePipeline();
        DeviceMethod dClient = DeviceMethod.createFromConnectionString(connectionString);

        PipelineTopologySetRequest setPipelineTopologyRequest = new PipelineTopologySetRequest();
        setPipelineTopologyRequest.setPipelineTopology(pipelineTopology);
        MethodResult setPipelineResult = invokeDirectMethod(dClient, serialize(setPipelineTopologyRequest));

        PipelineTopologyListRequest listTopologyRequest = new PipelineTopologyListRequest();
        MethodResult listPipelineResult = invokeDirectMethod(dClient, serialize(listTopologyRequest));

        PipelineTopologyGetRequest getTopologyRequest = new PipelineTopologyGetRequest();
        getTopologyRequest.setName(pipelineTopology.getName());
        MethodResult getPipelineResult = invokeDirectMethod(dClient, serialize(getTopologyRequest));

        LivePipelineSetRequest setLivePipelineRequest = new LivePipelineSetRequest();
        setLivePipelineRequest.setLivePipeline(livePipeline);
        MethodResult setLivePipelineResult = invokeDirectMethod(dClient, serialize(setLivePipelineRequest));

        LivePipelineListRequest listLivePipelineRequest = new LivePipelineListRequest();
        MethodResult liveLivePipelineResult = invokeDirectMethod(dClient, serialize(listLivePipelineRequest));

        LivePipelineActivateRequest activateLivePipelineRequest = new LivePipelineActivateRequest();
        activateLivePipelineRequest.setName(livePipeline.getName());
        MethodResult activateLivePipelineResult = invokeDirectMethod(dClient, serialize(activateLivePipelineRequest));

        LivePipelineGetRequest getLivePipelineRequest = new LivePipelineGetRequest();
        getLivePipelineRequest.setName(livePipeline.getName());
        MethodResult getLivePipelineResult = invokeDirectMethod(dClient, serialize(getLivePipelineRequest));

        LivePipelineDeactivateRequest deactivateLivePipelineRequest = new LivePipelineDeactivateRequest();
        deactivateLivePipelineRequest.setName(livePipeline.getName());
        MethodResult deactivateLivePipelineResult = invokeDirectMethod(dClient, serialize(deactivateLivePipelineRequest));

        LivePipelineDeleteRequest deleteLivePipelineRequest = new LivePipelineDeleteRequest();
        deleteLivePipelineRequest.setName(livePipeline.getName());
        MethodResult deleteLivePipelineResult = invokeDirectMethod(dClient, serialize(deleteLivePipelineRequest));

        PipelineTopologyDeleteRequest deletePipelineRequest = new PipelineTopologyDeleteRequest();
        deletePipelineRequest.setName(livePipeline.getName());
        MethodResult deletePipelineResult = invokeDirectMethod(dClient, serialize(deletePipelineRequest));

    }
}
