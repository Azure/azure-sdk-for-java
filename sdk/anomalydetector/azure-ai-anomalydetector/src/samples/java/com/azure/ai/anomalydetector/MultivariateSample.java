// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

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
import com.azure.core.util.BinaryData;
import com.azure.core.http.rest.RequestOptions;
import org.junit.jupiter.api.Assertions;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.StringReader;
import java.io.InputStream;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

enum ModelStatus {
    CREATED, RUNNING, READY, FAILED
}
enum DetectionStatus {
    CREATED, RUNNING, READY, FAILED
}

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

    private static UUID createModel(AnomalyDetectorClient client, BinaryData body) {
        RequestOptions requestOptions = new RequestOptions();
        Response<BinaryData> response = client.createAndTrainMultivariateModelWithResponse(body, requestOptions);
        String header = response.getHeaders().get("Location").getValue();
        String[] modelIds = header.split("/");
        UUID modelId = UUID.fromString(modelIds[modelIds.length - 1]);
        return modelId;
    }

    private static JsonObject getModelInfo(AnomalyDetectorClient client, UUID modelID) {
        RequestOptions requestOptions = new RequestOptions();
        Response<BinaryData> response = client.getMultivariateModelWithResponse(modelID.toString(), requestOptions);
        String responseBodyStr = response.getValue().toString();
        JsonObject jsonObject = Json.createReader(new StringReader(responseBodyStr)).readObject();

        JsonObject modelInfo = jsonObject.getJsonObject("modelInfo");

        return modelInfo;
    }

    private static UUID getResultId(AnomalyDetectorClient client, BinaryData body, UUID modelId) {
        RequestOptions requestOptions = new RequestOptions();
        Response<BinaryData> response = client.detectMultivariateBatchAnomalyWithResponse(modelId.toString(), body, requestOptions);
        String responseBodyStr = response.getValue().toString();
        JsonObject jsonObject = Json.createReader(new StringReader(responseBodyStr)).readObject();
        UUID resultId = UUID.fromString(jsonObject.getString("resultId"));
        return resultId;
    }

    private static String getInferenceStatus(AnomalyDetectorClient client, UUID resultId) {
        RequestOptions requestOptions = new RequestOptions();
        Response<BinaryData> response = client.getMultivariateBatchDetectionResultWithResponse(resultId.toString(), requestOptions);
        String responseBodyStr = response.getValue().toString();
        JsonObject jsonObject = Json.createReader(new StringReader(responseBodyStr)).readObject();
        String status = jsonObject.getJsonObject("summary").getString("status");
        return status;
    }

    private static void getModelList(AnomalyDetectorClient client, Integer skip, Integer top) {
        RequestOptions requestOptions = new RequestOptions()
            .addQueryParam("skip", skip.toString())
            .addQueryParam("top", top.toString());
        PagedIterable<BinaryData> response = client.listMultivariateModels(requestOptions);

        Iterator<PagedResponse<BinaryData>> ite = response.iterableByPage().iterator();
        int i = 1;
        while (ite.hasNext()) {
            PagedResponse<BinaryData> items = ite.next();
            System.out.println("The result in the page " + i);
            i++;
            for (BinaryData item : items.getValue()) {
                JsonObject jsonObject = Json.createReader(new StringReader(item.toString())).readObject();
                System.out.println("\t" + jsonObject.getString("modelId"));
            }
        }
    }

    private static Response<BinaryData> getLastDetectResult(AnomalyDetectorClient client, BinaryData body, UUID modelId) {
        RequestOptions requestOptions = new RequestOptions();
        Response<BinaryData> response = client.detectMultivariateLastAnomalyWithResponse(modelId.toString(), body, requestOptions);

        return response;
    }

    public static void run(BinaryData trainBody, BinaryData beginInferBody) throws Exception {
        String endpoint = "<anomaly-detector-resource-endpoint>";
        String key = "<anomaly-detector-resource-key>";

        // Get multivariate client
        AnomalyDetectorClient client = getClient(endpoint, key);

        // Start training and get Model ID
        UUID modelId = createModel(client, trainBody);
        System.out.println(modelId);
        // Check model status util the model get ready
        while (true) {
            JsonObject modelInfo = getModelInfo(client, modelId);
            ModelStatus modelStatus = ModelStatus.valueOf(modelInfo.getString("status"));
            if (modelStatus == ModelStatus.READY) {
                System.out.println("READY");
                break;
            } else if (modelStatus == ModelStatus.FAILED) {
                System.out.println("FAILED");
                throw new Exception(modelInfo.getJsonArray("errors").getString(0));
            }
            System.out.println("TRAINING");
            TimeUnit.SECONDS.sleep(5);
        }


        // Start inference and get the Result ID
        UUID resultId = getResultId(client, beginInferBody, modelId);
        // Check inference status util the result get ready
        while (true) {
            DetectionStatus detectionStatus = DetectionStatus.valueOf(getInferenceStatus(client, resultId));
            assert detectionStatus != DetectionStatus.FAILED;
            if (detectionStatus == DetectionStatus.READY) {
                System.out.println("READY");
                break;
            }
            System.out.println("INFERRING");
            TimeUnit.SECONDS.sleep(5);
        }

        // Synchronized anomaly detection
        InputStream fileInputStream = new FileInputStream("azure-ai-anomalydetector\\src\\samples\\java\\sample_data\\sync_infer_body.json");
        JsonReader reader = Json.createReader(fileInputStream);
        BinaryData detectBody = BinaryData.fromString(reader.readObject().toString());
        Response<BinaryData> lastDetectResponse = getLastDetectResult(client, detectBody, modelId);
        String responseBodyStr = lastDetectResponse.getValue().toString();
        JsonObject lastDetectJsonObject = Json.createReader(new StringReader(responseBodyStr)).readObject();
        JsonArray variableStates = lastDetectJsonObject.getJsonArray("variableStates");
        JsonArray results = lastDetectJsonObject.getJsonArray("results");
        if (lastDetectResponse.getStatusCode() == 200) {
            for (int i = 0; i < results.size(); i++) {
                JsonObject item = results.getJsonObject(i);
                System.out.print(
                    "\ntimestamp: "
                        + item.getString("timestamp")
                        + ",  isAnomaly: "
                        + item.getJsonObject("value").getBoolean("isAnomaly")
                        + ",  Score: "
                        + item.getJsonObject("value").getJsonNumber("score"));
            }
        } else {
            for (int i = 0; i < results.size(); i++) {
                JsonObject item = results.getJsonObject(i);
                System.out.print(
                    "\ntimestamp: "
                        + item.getString("timestamp")
                        + ",  errors: "
                        + item.getJsonArray("errors").getString(0));
            }
        }

        //Delete model
        RequestOptions requestOptions = new RequestOptions();
        Response<Void> deleteMultivariateModelWithResponse = client.deleteMultivariateModelWithResponse(modelId.toString(), requestOptions);
        Assertions.assertEquals(204, deleteMultivariateModelWithResponse.getStatusCode());

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
