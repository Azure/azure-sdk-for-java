// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.ai.anomalydetector.models.AlignMode;
import com.azure.ai.anomalydetector.models.AlignPolicy;
import com.azure.ai.anomalydetector.models.DetectAnomalyResponse;
import com.azure.ai.anomalydetector.models.DetectionRequest;
import com.azure.ai.anomalydetector.models.DetectionResult;
import com.azure.ai.anomalydetector.models.DetectionStatus;
import com.azure.ai.anomalydetector.models.FillNaMethod;
import com.azure.ai.anomalydetector.models.Model;
import com.azure.ai.anomalydetector.models.ModelInfo;
import com.azure.ai.anomalydetector.models.ModelSnapshot;
import com.azure.ai.anomalydetector.models.ModelStatus;
import com.azure.ai.anomalydetector.models.TrainMultivariateModelResponse;
import com.azure.ai.anomalydetector.models.LastDetectionRequest;
import com.azure.ai.anomalydetector.models.LastDetectionResult;
import com.azure.ai.anomalydetector.models.AnomalyState;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.ContentType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.util.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class MultivariateSample {
    private static void close(FileOutputStream fos) {
        try {
            fos.close();
            System.out.println("closed");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void write(FileOutputStream fos, ByteBuffer b) {
        try {
            fos.write(b.array());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static AnomalyDetectorClient getClient(String endpoint, String key) {
        HttpHeaders headers = new HttpHeaders()
            .put("Accept", ContentType.APPLICATION_JSON);

        HttpPipelinePolicy authPolicy = new AzureKeyCredentialPolicy("Ocp-Apim-Subscription-Key",
            new AzureKeyCredential(key));
        AddHeadersPolicy addHeadersPolicy = new AddHeadersPolicy(headers);

        HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(HttpClient.createDefault())
            .policies(authPolicy, addHeadersPolicy).build();
        // Instantiate a client that will be used to call the service.
        HttpLogOptions httpLogOptions = new HttpLogOptions();
        httpLogOptions.setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);

        AnomalyDetectorClient anomalyDetectorClient = new AnomalyDetectorClientBuilder()
            .pipeline(httpPipeline)
            .endpoint(endpoint)
            .httpLogOptions(httpLogOptions)
            .buildClient();
        return anomalyDetectorClient;
    }

    private static UUID getMetricId(AnomalyDetectorClient client, ModelInfo request) {
        TrainMultivariateModelResponse trainMultivariateModelResponse = client.trainMultivariateModelWithResponse(request, Context.NONE);
        String header = trainMultivariateModelResponse.getDeserializedHeaders().getLocation();
        String[] model_ids = header.split("/");
        UUID model_id = UUID.fromString(model_ids[model_ids.length - 1]);
        return model_id;
    }

    private static ModelInfo getModelInfo(AnomalyDetectorClient client, UUID model_id) {
        Response<Model> response = client.getMultivariateModelWithResponse(model_id, Context.NONE);
        UUID model = response.getValue().getModelId();
        return response.getValue().getModelInfo();
    }

    private static UUID getResultId(AnomalyDetectorClient client, UUID modelId, DetectionRequest detectionRequest) {
        DetectAnomalyResponse detectAnomalyResponse = client.detectAnomalyWithResponse(modelId, detectionRequest, Context.NONE);
        String response = detectAnomalyResponse.getDeserializedHeaders().getLocation();
        String[] result = response.split("/");
        UUID resultId = UUID.fromString(result[result.length - 1]);
        return resultId;
    }

    private static DetectionStatus getInferenceStatus(AnomalyDetectorClient client, UUID resultId) {
        DetectionResult response = client.getDetectionResult(resultId);
        DetectionStatus status = response.getSummary().getStatus();
        return status;
    }

    private static void ExportResult(AnomalyDetectorClient client, UUID modelId, String path) throws FileNotFoundException {
        StreamResponse response = client.exportModelWithResponse(modelId, Context.NONE);
        Flux<ByteBuffer> value = response.getValue();
        FileOutputStream bw = new FileOutputStream(path);
        value.subscribe(s -> write(bw, s), (e) -> close(bw), () -> close(bw));
    }

    private static void GetModelList(AnomalyDetectorClient client, Integer skip, Integer top){
        PagedIterable<ModelSnapshot> response = client.listMultivariateModel(skip, top);
        Iterator<PagedResponse<ModelSnapshot>> ite = response.iterableByPage().iterator();
        int i =1;
        while(ite.hasNext()){
            PagedResponse<ModelSnapshot> items= ite.next();
            System.out.println("The result in the page "+i);
            i++;
            for (ModelSnapshot item: items.getValue()
            ) {
                System.out.println("\t"+item.getModelId());
            }
            break;
        }
    }

    private static Response<LastDetectionResult> GetLastDetectResult(AnomalyDetectorClient client, LastDetectionRequest body, UUID modelId){
        return client.lastDetectAnomalyWithResponse(modelId, body, Context.NONE);
    }

    public static void main(final String[] args) throws Exception {
        String endpoint = "<anomaly-detector-resource-endpoint>";
        String key = "<anomaly-detector-resource-key>";
        //Get multivariate client
        AnomalyDetectorClient client = getClient(endpoint, key);


        //Start training and get Model ID
        Integer window = 28;
        AlignMode alignMode = AlignMode.OUTER;
        FillNaMethod fillNaMethod = FillNaMethod.LINEAR;
        Float paddingValue = 0.0f;
        AlignPolicy alignPolicy =
                new AlignPolicy().setAlignMode(alignMode).setFillNaMethod(fillNaMethod).setPaddingValue(paddingValue);
        String source = "<Your own data source>";
        OffsetDateTime startTime = OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime endTime = OffsetDateTime.of(2021, 1, 2, 12, 0, 0, 0, ZoneOffset.UTC);
        String displayName = "<placeholder>";
        ModelInfo request = new ModelInfo().setSlidingWindow(window).setAlignPolicy(alignPolicy).setSource(source).setStartTime(startTime).setEndTime(endTime).setDisplayName(displayName);
        UUID modelId = getMetricId(client, request);
        System.out.println(modelId);

        //Check model status util the model get ready
        while (true) {
            ModelInfo modelInfo = getModelInfo(client, modelId);
            ModelStatus modelStatus = modelInfo.getStatus();
            TimeUnit.SECONDS.sleep(5);
            if (modelStatus == ModelStatus.READY) {
                System.out.println("READY");
                break;
            }else if(modelStatus == ModelStatus.FAILED){
                throw new Exception(modelInfo.getErrors().get(0).getMessage());
            }
            System.out.println("TRAINING");
        }

        //Start inference and get the Result ID
        DetectionRequest detectionRequest = new DetectionRequest().setSource(source).setStartTime(startTime).setEndTime(endTime);
        UUID resultId = getResultId(client, modelId, detectionRequest);


        //Check inference status util the result get ready
        while (true) {
            DetectionStatus detectionStatus = getInferenceStatus(client, resultId);
            TimeUnit.SECONDS.sleep(5);
            assert detectionStatus != DetectionStatus.FAILED;
            if (detectionStatus == DetectionStatus.READY) {
                break;
            }
        }


        // Synchronized anomaly detection
        ObjectMapper m = new ObjectMapper();
        // modify the variables according to your source data as the format below
        String variables = "{\"variables\": " +
            "[{\"name\": \"variables_name1\", \"timestamps\": [\"2021-01-01 00:00:00\", \"2021-01-01 01:00:00\", \"...\"], \"values\": [0, 0, ...]}, " +
            "{\"name\": \"variables_name2\", \"timestamps\": [\"2021-01-01 00:00:00\", \"2021-01-01 01:00:00\", \"...\"], \"values\": [0, 0, ...]}]," +
            " \"detectingPoints\": 2}";
        LastDetectionRequest lastDetectBody = m.readValue(variables, LastDetectionRequest.class);
        Response<LastDetectionResult> lastDetectResult = GetLastDetectResult(client, lastDetectBody, modelId);
        if (lastDetectResult.getStatusCode()==200) {
            for (AnomalyState anomalyState: lastDetectResult.getValue().getResults()) {
                System.out.print(
                        "\ntimestamp: " + anomalyState.getTimestamp().toString() +
                        ",  isAnomaly: " + anomalyState.getValue().isAnomaly() +
                        ",  Score: " + anomalyState.getValue().getScore());
            }
        }else{
            for (AnomalyState anomalyState: lastDetectResult.getValue().getResults()) {
                System.out.print(
                        "\ntimestamp: " + anomalyState.getTimestamp().toString() +
                        ",  errors: " + anomalyState.getErrors().get(0).getMessage());
            }
        }

        //Export result files to local
        String path = "<path for the saving zip file>";
        ExportResult(client, modelId, path);


        //Delete model
        Response<Void> deleteMultivariateModelWithResponse = client.deleteMultivariateModelWithResponse(modelId, Context.NONE);


        //Get model list
        Integer skip = 0;
        Integer top = 5;
        GetModelList(client, skip, top);
    }
}
