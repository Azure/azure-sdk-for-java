// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.maps.service;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.maps.service.models.DatasetDetailInfo;
import com.azure.maps.service.models.DatasetsCreateResponse;
import com.azure.maps.service.models.DatasetsGetOperationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public class DatasetSample {
    public static void main(String[] args) throws InterruptedException, JsonProcessingException {
        if (args.length != 1) {
            System.out.println("Usage DatasetSample.java <conversion_id>");
        }
        HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key",
                new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY")));
        MapsClient client = new MapsClientBuilder().addPolicy(policy).buildClient();
        String conversionId = args[0];

        DatasetsCreateResponse datasetsCreateResponse = client.getDatasets()
                .createWithResponseAsync(conversionId, null, null).block();
        String operationLocation = datasetsCreateResponse.getDeserializedHeaders().getOperationLocation();
        System.out.println(String.format("Created dataset with operation location %s", operationLocation));
        MapsCommon.print(datasetsCreateResponse.getValue());
        String operationId = MapsCommon.getUid(operationLocation);

        String datasetId = MapsCommon.waitForStatusComplete(operationId, id -> getOperation(client.getDatasets(), id));
        if (datasetId == null) {
            System.out.println("Data upload Failed");
            return;
        }
        try {
            DatasetDetailInfo datasetDetailInfo = client.getDatasets().get(datasetId);
            System.out.println("Got details of dataset:");
            MapsCommon.print(datasetDetailInfo);

            PagedIterable<DatasetDetailInfo> list = client.getDatasets().list();
            System.out.println("View all previously created datasets:");
            for (DatasetDetailInfo item : list) {
                MapsCommon.print(item);
            }
        } catch (HttpResponseException err) {
            System.out.println(err);
        } finally {
            client.getDatasets().delete(datasetId);
            System.out.println(String.format("Deleted dataset with aliasId: %s", datasetId));
        }
    }

    public static MapsCommon.OperationWithHeaders getOperation(Datasets datasets, String operationId) {
        DatasetsGetOperationResponse result = datasets.getOperationWithResponseAsync(operationId).block();
        System.out.println(String.format("Get datasets operation with operation_id %s status %s", operationId,
                result.getValue().getStatus()));
        return new MapsCommon.OperationWithHeaders(result.getValue(),
                result.getDeserializedHeaders().getResourceLocation());
    }

}
