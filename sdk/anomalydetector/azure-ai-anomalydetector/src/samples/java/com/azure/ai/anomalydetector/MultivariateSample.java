// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.ai.anomalydetector.models.DetectionRequest;
import com.azure.ai.anomalydetector.models.DetectionResult;
import com.azure.ai.anomalydetector.models.DetectionStatus;
import com.azure.ai.anomalydetector.models.LastDetectionRequest;
import com.azure.ai.anomalydetector.models.LastDetectionResult;
import com.azure.ai.anomalydetector.models.Model;
import com.azure.ai.anomalydetector.models.ModelInfo;
import com.azure.ai.anomalydetector.models.ModelStatus;
import com.azure.ai.anomalydetector.models.ErrorResponse;
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
import com.azure.core.util.BinaryData;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.InputStream;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonReader;


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

    public static void run(BinaryData trainBody, BinaryData beginInferBody) throws Exception {
        String endpoint = "<anomaly-detector-resource-endpoint>";
        String key = "<anomaly-detector-resource-key>";

        // Get multivariate client
        AnomalyDetectorClient client = getClient(endpoint, key);

        // Start training and get Model ID
        ModelInfo trainRequest = trainBody.toObject(ModelInfo.class);
        UUID modelId = createModel(client, trainRequest);
        System.out.println(modelId);
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
        DetectionRequest detectionRequest = beginInferBody.toObject(DetectionRequest.class);
        UUID resultId = getResultId(client, detectionRequest, modelId);
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
        // test MultiTables
        System.out.println("============================== Test MultiTables =========================================");
        BinaryData trainBodyMultiTables = BinaryData.fromString("{\"slidingWindow\":200,\"alignPolicy\":{\"alignMode\":\"Outer\",\"fillNAMethod\":\"Linear\",\"paddingValue\":0},\"dataSource\":\"https://mvaddataset.blob.core.windows.net/sample-multitable/sample_data_20_3000\",\"dataSchema\":\"MultiTable\",\"startTime\":\"2021-01-02T00:00:00Z\",\"endTime\":\"2021-01-02T05:00:00Z\",\"displayName\":\"SampleRequest\"}");
        BinaryData beginInferBodyMultiTables = BinaryData.fromString("{\"dataSource\":\"https://mvaddataset.blob.core.windows.net/sample-multitable/sample_data_20_3000\",\"topContributorCount\":10,\"startTime\":\"2021-01-01T00:00:00Z\",\"endTime\":\"2021-01-01T12:00:00Z\"}");
        run(trainBodyMultiTables, beginInferBodyMultiTables);

        // test OneTable
        System.out.println("============================= Test OneTable =============================================");
        BinaryData trainBodyOneTable = BinaryData.fromString("{\"slidingWindow\":200,\"alignPolicy\":{\"alignMode\":\"Outer\",\"fillNAMethod\":\"Linear\",\"paddingValue\":0},\"dataSource\":\"https://mvaddataset.blob.core.windows.net/sample-onetable/sample_data_20_3000.csv\",\"dataSchema\":\"OneTable\",\"startTime\":\"2021-01-02T00:00:00Z\",\"endTime\":\"2021-01-02T05:00:00Z\",\"displayName\":\"SampleRequest\"}");
        BinaryData beginInferBodyOneTable = BinaryData.fromString("{\"dataSource\":\"https://mvaddataset.blob.core.windows.net/sample-onetable/sample_data_20_3000.csv\",\"topContributorCount\":10,\"startTime\":\"2021-01-01T00:00:00Z\",\"endTime\":\"2021-01-01T12:00:00Z\"}");
        run(trainBodyOneTable, beginInferBodyOneTable);

    }
}
