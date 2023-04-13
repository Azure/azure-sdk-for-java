// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.ai.anomalydetector.models.*;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MultivariateAnomalyDetectorClientTest extends AnomalyDetectorClientTestBase {
    private static final String datasource = "https://mvaddataset.blob.core.windows.net/sample-multitable/sample_data_20_3000";
    private static final DataSchema dataSchema = DataSchema.MULTI_TABLE;

    private AnomalyDetectorClient getClient() {
        return getClientBuilder().buildClient();
    }

    private static List<String> getModelList(AnomalyDetectorClient client) {
        Integer skip = 0;
        Integer top = 5;
        PagedIterable<AnomalyDetectionModel> response = client.listMultivariateModels(skip, top);

        List<String> modelIds = new ArrayList<>();
        System.out.println("ModelList: ");
        response.streamByPage().forEach(models -> {
            for (AnomalyDetectionModel item : models.getValue()) {
                System.out.println("\t" + item.getModelId());
                modelIds.add(item.getModelId());
            }
        });

        return modelIds;
    }

    private static String trainModel(AnomalyDetectorClient client){
        OffsetDateTime startTime = OffsetDateTime.parse("2021-01-02T00:00:00Z");
        OffsetDateTime endTime = OffsetDateTime.parse("2021-01-02T05:00:00Z");
        ModelInfo trainRequest = new ModelInfo(datasource, startTime, endTime);
        trainRequest.setSlidingWindow(200)
            .setAlignPolicy(new AlignPolicy()
                .setAlignMode(AlignMode.OUTER)
                .setFillNAMethod(FillNAMethod.LINEAR)
                .setPaddingValue(0.0))
            .setDataSchema(dataSchema)
            .setDisplayName("SampleRequest");

        // Start training and get Model ID
        AnomalyDetectionModel model = client.trainMultivariateModel(trainRequest);
        String modelId = model.getModelId();
        System.out.println("modelId: " + modelId);
        return modelId;
    }

    private static void inferModel(AnomalyDetectorClient client, String modelId) throws Exception {
        OffsetDateTime startTime = OffsetDateTime.parse("2021-01-02T00:00:00Z");
        OffsetDateTime endTimeDetect = OffsetDateTime.parse("2021-01-02T12:00:00Z");
        MultivariateBatchDetectionOptions detectionRequest = new MultivariateBatchDetectionOptions(datasource, 10, startTime, endTimeDetect);

        MultivariateDetectionResult detectionResult = client.detectMultivariateBatchAnomaly(modelId, detectionRequest);
        UUID resultId = UUID.fromString(detectionResult.getResultId());

        System.out.println("resultId: " + resultId);
        while (true) { // Check inference status util the result get ready
            MultivariateDetectionResult getDetectionResult = client.getMultivariateBatchDetectionResult(resultId.toString());
            MultivariateBatchDetectionStatus detectionStatus = getDetectionResult.getSummary().getStatus();
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
    }

    private static String getModelId(AnomalyDetectorClient client){
        List<String> modelIds = getModelList(client);
        String modelId;
        if (modelIds.size() > 0){
            modelId = modelIds.get(0);
        }else{
            modelId = trainModel(client);
        }
        return modelId;
    }

    @Test
    public void testListMultivariateModels() {
        AnomalyDetectorClient client = getClient();
        getModelList(client);
    }

    @Test
    public void testTrainMultivariateModel(){
        AnomalyDetectorClient client = getClient();
        trainModel(client);
    }

    @Test
    public void testGetMultivariateModel(){
        AnomalyDetectorClient client = getClient();
        String modelId = getModelId(client);
        AnomalyDetectionModel model = client.getMultivariateModel(modelId);
        assertEquals(modelId, model.getModelId());
        System.out.println("modelId: " + model.getModelId());
        System.out.println("createdTime: " + model.getCreatedTime());
        System.out.println("lastUpdatedTime: " + model.getLastUpdatedTime());
    }

    @Test
    public void testDeleteMultivariateModel(){
        AnomalyDetectorClient client = getClient();
        String modelId = getModelId(client);
        client.deleteMultivariateModel(modelId);
        System.out.println("Deleted modelId: " + modelId);
    }

    @Test
    public void testDetectAndGetMultivariateBatchAnomaly(){
        AnomalyDetectorClient client = getClient();
        String modelId = getModelId(client);
        try{
            inferModel(client, modelId);
        }catch(Exception exception){
            System.out.println(exception.fillInStackTrace().toString());
        }
    }

    @Test
    public void testDetectMultivariateLastAnomaly() throws FileNotFoundException {
        AnomalyDetectorClient client = getClient();
        String modelId = getModelId(client);
        InputStream fileInputStream = new FileInputStream("src\\samples\\java\\sample_data\\sync_infer_body.json");
        JsonReader reader = Json.createReader(fileInputStream);
        BinaryData detectBody = BinaryData.fromString(reader.readObject().toString());
        MultivariateLastDetectionOptions lastDetectionRequest = detectBody.toObject(MultivariateLastDetectionOptions.class);
        MultivariateLastDetectionResult lastDetectionResult = client.detectMultivariateLastAnomaly(modelId, lastDetectionRequest);
        assertEquals(1, lastDetectionResult.getResults().size());
        for (AnomalyState anomalyState : lastDetectionResult.getResults()) {
            System.out.println("timestamp: " + anomalyState.getTimestamp().toString()
                + ", isAnomaly: " + anomalyState.getValue().isAnomaly()
                + ", Score: " + anomalyState.getValue().getScore()
            );
        }
    }

}
