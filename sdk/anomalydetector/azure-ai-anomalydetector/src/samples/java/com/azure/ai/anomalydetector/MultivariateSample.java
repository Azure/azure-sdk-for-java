// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.ai.anomalydetector.models.AlignMode;
import com.azure.ai.anomalydetector.models.AlignPolicy;
import com.azure.ai.anomalydetector.models.AnomalyDetectionModel;
import com.azure.ai.anomalydetector.models.AnomalyState;
import com.azure.ai.anomalydetector.models.DataSchema;
import com.azure.ai.anomalydetector.models.ErrorResponse;
import com.azure.ai.anomalydetector.models.FillNAMethod;
import com.azure.ai.anomalydetector.models.ModelInfo;
import com.azure.ai.anomalydetector.models.ModelStatus;
import com.azure.ai.anomalydetector.models.MultivariateBatchDetectionOptions;
import com.azure.ai.anomalydetector.models.MultivariateBatchDetectionStatus;
import com.azure.ai.anomalydetector.models.MultivariateDetectionResult;
import com.azure.ai.anomalydetector.models.MultivariateLastDetectionOptions;
import com.azure.ai.anomalydetector.models.MultivariateLastDetectionResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
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

    private static MultivariateClient getClient(String endpoint, String key) {
        return new AnomalyDetectorClientBuilder().credential(new AzureKeyCredential(key))
            .endpoint(endpoint)
            .buildMultivariateClient();
    }

    private static UUID createModel(MultivariateClient client, ModelInfo modelInfo) {
        AnomalyDetectionModel model = client.trainMultivariateModel(modelInfo);
        return UUID.fromString(model.getModelId());
    }

    private static ModelInfo getModelInfo(MultivariateClient client, UUID modelId) {
        return client.getMultivariateModel(modelId.toString()).getModelInfo();
    }

    private static UUID getResultId(MultivariateClient client, MultivariateBatchDetectionOptions body, UUID modelId) {
        MultivariateDetectionResult detectionResult = client.detectMultivariateBatchAnomaly(modelId.toString(), body);
        return UUID.fromString(detectionResult.getResultId());
    }

    private static MultivariateBatchDetectionStatus getInferenceStatus(MultivariateClient client, UUID resultId) {
        return client.getMultivariateBatchDetectionResult(resultId.toString()).getSummary().getStatus();
    }

    private static void getModelList(MultivariateClient client) {
        PagedIterable<AnomalyDetectionModel> response = client.listMultivariateModels();

        System.out.println("ModelList: ");
        response.streamByPage().forEach(models -> {
            for (AnomalyDetectionModel item : models.getValue()) {
                System.out.println("\t" + item.getModelId());
            }
        });
    }

    private static MultivariateLastDetectionResult getLastDetectResult(MultivariateClient client,
        MultivariateLastDetectionOptions body, UUID modelId) {
        return client.detectMultivariateLastAnomaly(modelId.toString(), body);
    }

    public static void run(String datasource, DataSchema dataSchema) throws Exception {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_ANOMALY_DETECTOR_ENDPOINT");
        String key = Configuration.getGlobalConfiguration().get("AZURE_ANOMALY_DETECTOR_API_KEY");

        // Get multivariate client
        MultivariateClient client = getClient(endpoint, key);

        // set training request
        OffsetDateTime startTime = OffsetDateTime.parse("2021-01-02T00:00:00Z");
        OffsetDateTime endTime = OffsetDateTime.parse("2021-01-02T05:00:00Z");
        ModelInfo trainRequest = new ModelInfo(datasource, startTime, endTime);
        trainRequest.setSlidingWindow(200)
            .setAlignPolicy(new AlignPolicy().setAlignMode(AlignMode.OUTER)
                .setFillNAMethod(FillNAMethod.LINEAR)
                .setPaddingValue(0.0))
            .setDataSchema(dataSchema)
            .setDisplayName("SampleRequest");

        // Start training and get Model ID
        UUID modelId = createModel(client, trainRequest);
        System.out.println("modelId: " + modelId);
        // Check model status util the model get ready
        while (true) {
            ModelInfo modelInfo = getModelInfo(client, modelId);
            ModelStatus modelStatus = modelInfo.getStatus();
            if (modelStatus == ModelStatus.READY) {
                System.out.println("READY");
                break;
            } else if (modelStatus == ModelStatus.FAILED) {
                System.out.println("FAILED");
                StringBuilder errorStr = new StringBuilder();
                for (ErrorResponse errorResponse : modelInfo.getErrors()) {
                    System.out.println(errorResponse.getCode() + errorResponse.getMessage());
                    errorStr.append(";").append(errorResponse.getCode()).append(errorResponse.getMessage());
                }
                throw new RuntimeException("Training Failed. Error: " + errorStr);
            }
            System.out.println("TRAINING");
            TimeUnit.SECONDS.sleep(5);
        }

        // Start inference and get the Result ID
        OffsetDateTime endTimeDetect = OffsetDateTime.parse("2021-01-02T12:00:00Z");
        MultivariateBatchDetectionOptions detectionRequest = new MultivariateBatchDetectionOptions(datasource,
            startTime, endTimeDetect)
            .setTopContributorCount(10);
        UUID resultId = getResultId(client, detectionRequest, modelId);
        System.out.println("resultId: " + resultId);
        while (true) { // Check inference status util the result get ready
            MultivariateBatchDetectionStatus detectionStatus = getInferenceStatus(client, resultId);
            if (detectionStatus == MultivariateBatchDetectionStatus.READY) {
                System.out.println("READY");
                break;
            } else if (detectionStatus == MultivariateBatchDetectionStatus.FAILED) {
                System.out.println("FAILED");
                throw new RuntimeException("Inference Failed.");
            }
            System.out.println("INFERRING");
            TimeUnit.SECONDS.sleep(5);
        }

        // Synchronized anomaly detection
        MultivariateLastDetectionOptions lastDetectionRequest;
        try (InputStream fileInputStream = new FileInputStream(
            "azure-ai-anomalydetector\\src\\samples\\java\\sample_data\\sync_infer_body.json");
            JsonReader jsonReader = JsonProviders.createReader(fileInputStream)) {
            lastDetectionRequest = MultivariateLastDetectionOptions.fromJson(jsonReader);
        }

        MultivariateLastDetectionResult lastDetectionResult = getLastDetectResult(client, lastDetectionRequest,
            modelId);
        for (AnomalyState anomalyState : lastDetectionResult.getResults()) {
            System.out.println(
                "timestamp: " + anomalyState.getTimestamp().toString() + ", isAnomaly: " + anomalyState.getValue()
                    .isAnomaly() + ", Score: " + anomalyState.getValue().getScore());
        }

        //Delete model
        client.deleteMultivariateModel(modelId.toString());

        //Get model list
        getModelList(client);
    }

    public static void main(final String[] args) throws Exception {
        System.out.println("Test MultiTables");
        run("https://mvaddataset.blob.core.windows.net/sample-multitable/sample_data_20_3000", DataSchema.MULTI_TABLE);

        System.out.println("Test OneTable");
        run("https://mvaddataset.blob.core.windows.net/sample-onetable/sample_data_20_3000.csv", DataSchema.ONE_TABLE);
    }
}
