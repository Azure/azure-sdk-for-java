package com.azure.maps.service;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.maps.service.models.ConversionListDetailInfo;
import com.azure.maps.service.models.ConversionsConvertResponse;
import com.azure.maps.service.models.ConversionsGetOperationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ConversionSample {
    public static void main(String[] args) throws InterruptedException, JsonProcessingException {
        if (args.length != 1) {
            System.out.println("Usage ConversionSample.java <udid>");
            return;
        }
        String udid = args[0];
        HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key",
                new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY")));
        MapsClient client = new MapsClientBuilder().addPolicy(policy).buildClient();

        ConversionsConvertResponse result = client.getConversions().convertWithResponseAsync(udid, "facility-2.0", null)
                .block();
        String operationLocation = result.getDeserializedHeaders().getOperationLocation();
        System.out.println(String.format("Created conversion with operation_id %s", operationLocation));
        MapsCommon.print(result.getValue());

        String operationId = MapsCommon.getUid(operationLocation);
        String conversionId = MapsCommon.waitForStatusComplete(operationId,
                id -> getOperation(client.getConversions(), id));
        if (conversionId == null) {
            System.out.println("Conversion Failed");
            return;
        }
        try {
            ConversionListDetailInfo conversionListDetailInfo = client.getConversions().get(conversionId);
            System.out.println("Got conversion:");
            MapsCommon.print(conversionListDetailInfo);

            PagedIterable<ConversionListDetailInfo> list = client.getConversions().list();
            System.out.println("View all conversions:");
            for (ConversionListDetailInfo item : list) {
                MapsCommon.print(item);
            }
        } catch (HttpResponseException err) {
            System.out.println(err);
        } finally {
            client.getConversions().delete(conversionId);
            System.out.println(String.format("Deleted conversion with id %s", conversionId));
        }
    }

    public static MapsCommon.OperationWithHeaders getOperation(Conversions conversion, String operationId) {
        ConversionsGetOperationResponse result = conversion.getOperationWithResponseAsync(operationId).block();
        System.out.println(String.format("Get conversion operation with operation_id %s status %s", operationId,
                result.getValue().getStatus()));
        return new MapsCommon.OperationWithHeaders(result.getValue(),
                result.getDeserializedHeaders().getResourceLocation());
    }
}
