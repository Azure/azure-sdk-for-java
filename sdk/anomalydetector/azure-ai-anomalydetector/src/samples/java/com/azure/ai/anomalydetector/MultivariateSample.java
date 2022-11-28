// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.ai.anomalydetector.models.ModelInfo;
import com.azure.ai.anomalydetector.models.Model;
import com.azure.ai.anomalydetector.models.ModelStatus;
import com.azure.ai.anomalydetector.models.DetectionRequest;
import com.azure.ai.anomalydetector.models.DetectionResult;
import com.azure.ai.anomalydetector.models.DetectionStatus;
import com.azure.ai.anomalydetector.models.LastDetectionRequest;
import com.azure.ai.anomalydetector.models.LastDetectionResult;
import com.azure.ai.anomalydetector.models.DataSchema;
import com.azure.ai.anomalydetector.models.AlignPolicy;
import com.azure.ai.anomalydetector.models.ErrorResponse;
import com.azure.ai.anomalydetector.models.AnomalyState;
import com.azure.ai.anomalydetector.models.AlignMode;
import com.azure.ai.anomalydetector.models.FillNAMethod;

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
import com.azure.core.util.BinaryData;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.InputStream;

import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.json.*;


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

    private static UUID createModel(AnomalyDetectorClient client, ModelInfo modelInfo) {
        Model model = client.createAndTrainMultivariateModel(modelInfo);
        UUID modelId = UUID.fromString(model.getModelId());
        return modelId;
    }

    private static ModelInfo getModelInfo(AnomalyDetectorClient client, UUID modelId) {
        Model model = client.getMultivariateModel(modelId.toString());
        return model.getModelInfo();
    }

    private static UUID getResultId(AnomalyDetectorClient client, DetectionRequest body, UUID modelId) {
        DetectionResult detectionResult = client.detectMultivariateBatchAnomaly(modelId.toString(), body);
        UUID resultId = UUID.fromString(detectionResult.getResultId());
        return resultId;
    }

    private static DetectionStatus getInferenceStatus(AnomalyDetectorClient client, UUID resultId) {
        DetectionResult detectionResult = client.getMultivariateBatchDetectionResult(resultId.toString());
        return detectionResult.getSummary().getStatus();
    }

    private static void getModelList(AnomalyDetectorClient client, Integer skip, Integer top) {
        PagedIterable<Model> response = client.listMultivariateModels(skip, top);
        Iterator<PagedResponse<Model>> ite = response.iterableByPage().iterator();
        int i = 1;
        while (ite.hasNext()) {
            PagedResponse<Model> items = ite.next();
            System.out.println("The result in the page " + i);
            i++;
            for (Model item : items.getValue()) {
                System.out.println("\t" + item.getModelId());
            }
        }
    }

    private static LastDetectionResult getLastDetectResult(AnomalyDetectorClient client, LastDetectionRequest body, UUID modelId) {
        LastDetectionResult res = client.detectMultivariateLastAnomaly(modelId.toString(), body);
        return res;
    }

    public static void run(String datasource, DataSchema dataSchema) throws Exception {
        String endpoint = "<anomaly-detector-resource-endpoint>";
        String key = "<anomaly-detector-resource-key>";

        // Get multivariate client
        AnomalyDetectorClient client = getClient(endpoint, key);

        // set training request
        OffsetDateTime startTime = OffsetDateTime.of(2021, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime endTime = OffsetDateTime.of(2021, 1, 2, 5, 0, 0, 0, ZoneOffset.UTC);
        ModelInfo trainRequest = new ModelInfo(datasource, startTime, endTime);
        trainRequest.setSlidingWindow(200);
        AlignPolicy alignPolicy = new AlignPolicy();
        alignPolicy.setAlignMode(AlignMode.OUTER);
        alignPolicy.setFillNAMethod(FillNAMethod.LINEAR);
        alignPolicy.setPaddingValue(0.0);
        trainRequest.setAlignPolicy(alignPolicy);
        trainRequest.setDataSchema(dataSchema);
        trainRequest.setDisplayName("SampleRequest");

        // Start training and get Model ID
        UUID modelId = createModel(client, trainRequest);
        System.out.println("modelId: " + modelId);
        // Check model status util the model get ready
        while (true) {
            ModelInfo modelInfo = getModelInfo(client, modelId);
            ModelStatus modelStatus = ModelStatus.valueOf(modelInfo.getStatus().toString());
            if (modelStatus == ModelStatus.READY) {
                System.out.println("READY");
                break;
            } else if (modelStatus == ModelStatus.FAILED) {
                System.out.println("FAILED");
                String errorStr = "";
                for (ErrorResponse errorResponse : modelInfo.getErrors()) {
                    System.out.println(errorResponse.getCode() + errorResponse.getMessage());
                    errorStr += ";" + errorResponse.getCode() + errorResponse.getMessage();
                }
                throw new Exception(errorStr);
            }
            System.out.println("TRAINING");
            TimeUnit.SECONDS.sleep(5);
        }

        // Start inference and get the Result ID
        OffsetDateTime endTimeDetect = OffsetDateTime.of(2021, 1, 2, 12, 0, 0, 0, ZoneOffset.UTC);
        DetectionRequest detectionRequest = new DetectionRequest(datasource, 10, startTime, endTimeDetect);
        UUID resultId = getResultId(client, detectionRequest, modelId);
        System.out.println("resultId: " + resultId);
        while (true) { // Check inference status util the result get ready
            DetectionStatus detectionStatus = getInferenceStatus(client, resultId);
            if (detectionStatus == DetectionStatus.READY) {
                System.out.println("READY");
                break;
            } else if (detectionStatus == DetectionStatus.FAILED) {
                System.out.println("FAILED");
                throw new Exception("Inference Failed.");
            }
            System.out.println("INFERRING");
            TimeUnit.SECONDS.sleep(5);
        }

        // Synchronized anomaly detection
        InputStream fileInputStream = new FileInputStream("azure-ai-anomalydetector\\src\\samples\\java\\sample_data\\sync_infer_body.json");
        JsonReader reader = Json.createReader(fileInputStream);
        BinaryData detectBody = BinaryData.fromString(reader.readObject().toString());
        LastDetectionRequest lastDetectionRequest = detectBody.toObject(LastDetectionRequest.class);
        LastDetectionResult lastDetectionResult = getLastDetectResult(client, lastDetectionRequest, modelId);
        for (AnomalyState anomalyState : lastDetectionResult.getResults()) {
            System.out.println("timestamp: " + anomalyState.getTimestamp().toString()
                + ", isAnomaly: " + anomalyState.getValue().isAnomaly()
                + ", Score: " + anomalyState.getValue().getScore()
            );
        }

        //Delete model
        client.deleteMultivariateModel(modelId.toString());

        //Get model list
        Integer skip = 0;
        Integer top = 5;
        getModelList(client, skip, top);
    }

    public static void main(final String[] args) throws Exception {
        System.out.println("Test MultiTables");
        run("https://mvaddataset.blob.core.windows.net/sample-multitable/sample_data_20_3000", DataSchema.MULTI_TABLE);

        System.out.println("Test OneTable");
        run("https://mvaddataset.blob.core.windows.net/sample-onetable/sample_data_20_3000.csv", DataSchema.ONE_TABLE);
    }
}
