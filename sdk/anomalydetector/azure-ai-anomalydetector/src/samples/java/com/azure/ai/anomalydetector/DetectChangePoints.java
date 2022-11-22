// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.ai.anomalydetector.models.ChangePointDetectRequest;
import com.azure.ai.anomalydetector.models.ChangePointDetectResponse;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.ContentType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonObjectBuilder;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;


/**
 * Sample for detecting change points in a piece of time series.
 */
public class DetectChangePoints {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     * @throws IOException Exception thrown when there is an error in reading all the lines from the csv file.
     */
    public static void main(final String[] args) throws IOException {
        String endpoint = "<anomaly-detector-resource-endpoint>";
        String key = "<anomaly-detector-resource-key>";

        HttpHeaders headers = new HttpHeaders()
            .put("Accept", ContentType.APPLICATION_JSON);

        HttpPipelinePolicy authPolicy = new AzureKeyCredentialPolicy("Ocp-Apim-Subscription-Key",
            new AzureKeyCredential(key));
        AddHeadersPolicy addHeadersPolicy = new AddHeadersPolicy(headers);

        HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(HttpClient.createDefault())
            .policies(authPolicy, addHeadersPolicy).build();
        // Instantiate a client that will be used to call the service.
        AnomalyDetectorClient anomalyDetectorClient = new AnomalyDetectorClientBuilder()
            .pipeline(httpPipeline)
            .endpoint(endpoint)
            .buildClient();

        InputStream fileInputStream = new FileInputStream("azure-ai-anomalydetector\\src\\samples\\java\\sample_data\\request-data.json");
        JsonReader reader = Json.createReader(fileInputStream);
        JsonObject jsonObject = reader.readObject();

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder(jsonObject);
        jsonObjectBuilder.add("granularity", "daily");
        jsonObject = jsonObjectBuilder.build();

        String detectBodyStr = jsonObject.toString();
        System.out.println(detectBodyStr);
        BinaryData detectBody = BinaryData.fromString(detectBodyStr);

        ChangePointDetectRequest changePointDetectRequest = detectBody.toObject(ChangePointDetectRequest.class);
        ChangePointDetectResponse changePointDetectResponse = anomalyDetectorClient.detectUnivariateChangePoint(changePointDetectRequest);
        System.out.println(changePointDetectResponse.getPeriod());
        System.out.println(Arrays.toString(changePointDetectResponse.getIsChangePoint().toArray(new Boolean[0])));
        System.out.println(Arrays.toString(changePointDetectResponse.getConfidenceScores().toArray(new Double[0])));
        System.out.println(Arrays.toString(changePointDetectResponse.getIsChangePoint().toArray(new Boolean[0])));

    }
}
