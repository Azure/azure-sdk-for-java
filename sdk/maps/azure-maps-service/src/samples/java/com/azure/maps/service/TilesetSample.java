package com.azure.maps.service;

import java.io.IOException;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.maps.service.models.TilesetDetailInfo;
import com.azure.maps.service.models.TilesetsCreateResponse;
import com.azure.maps.service.models.TilesetsGetOperationResponse;

public class TilesetSample {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 1) {
            System.out.println("Usage TilesetSample.java <dataset_id>");
            return;
        }
        String datasetId = args[0];
        HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key",
                new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY")));
        MapsClient client = new MapsClientBuilder().addPolicy(policy).buildClient();

        TilesetsCreateResponse tilesetsCreateResponse = client.getTilesets()
                .createWithResponseAsync(datasetId, "Test Description").block();

        String operationLocation = tilesetsCreateResponse.getDeserializedHeaders().getOperationLocation();
        System.out.println(String.format("Created tileset with operation_id %s", operationLocation));
        MapsCommon.print(tilesetsCreateResponse.getValue());

        String operationId = MapsCommon.getUid(operationLocation);
        String tilesetId = MapsCommon.waitForStatusComplete(operationId, id -> getOperation(client.getTilesets(), id));
        if (tilesetId == null) {
            System.out.println("Tileset Creation Failed");
            return;
        }

        try {
            System.out.println("List Tilesets");
            PagedIterable<TilesetDetailInfo> list = client.getTilesets().list();
            for (TilesetDetailInfo item : list) {
                MapsCommon.print(item);
            }
            System.out.println("Get Tileset");
            MapsCommon.print(client.getTilesets().get(tilesetId));
        } catch (HttpResponseException err) {
            System.out.println(err);
        } finally {
            System.out.println("Delete Tileset");
            client.getTilesets().delete(tilesetId);
        }
    }

    public static MapsCommon.OperationWithHeaders getOperation(Tilesets tileset, String operationId) {
        TilesetsGetOperationResponse result = tileset.getOperationWithResponseAsync(operationId).block();
        System.out.println(String.format("Get tileset operation with operation_id %s status %s", operationId,
                result.getValue().getStatus()));
        return new MapsCommon.OperationWithHeaders(result.getValue(),
                result.getDeserializedHeaders().getResourceLocation());
    }
}
