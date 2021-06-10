package com.azure.maps.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.maps.service.models.AliasListItem;
import com.azure.maps.service.models.ConversionListDetailInfo;
import com.azure.maps.service.models.ConversionsConvertResponse;
import com.azure.maps.service.models.ConversionsGetOperationResponse;
import com.azure.maps.service.models.DatasGetOperationPreviewResponse;
import com.azure.maps.service.models.DatasUpdatePreviewResponse;
import com.azure.maps.service.models.DatasUploadPreviewResponse;
import com.azure.maps.service.models.LongRunningOperationResult;
import com.azure.maps.service.models.MapDataListResponse;
import com.azure.maps.service.models.UploadDataFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataSample {
	public static void main(String[] args) throws InterruptedException, IOException {
    	HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key", new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY")));
    	MapsClient client = new MapsClientBuilder().addPolicy(policy).buildClient();

		InputStream stream = MapsCommon.getResource("/data_sample_upload.zip");

		DatasUploadPreviewResponse result = client.getDatas().uploadPreviewWithResponseAsync(UploadDataFormat.DWGZIPPACKAGE, stream.readAllBytes(), null).block();
		String operationLocation = result.getDeserializedHeaders().getOperationLocation();
		System.out.println(String.format("Created upload with operation_id %s", operationLocation));
		MapsCommon.print(result.getValue());
		String operationId = MapsCommon.getUid(operationLocation);
		
		String udid = MapsCommon.waitForStatusComplete(operationId, id -> getOperation(client.getDatas(), id));
		if (udid == null) {
			System.out.println("Data upload for zip Failed");
			return;
		}
		client.getDatas().deletePreview(udid);
		System.out.println(String.format("Deleted file with udid %s", udid));

        Object contentJson = MapsCommon.readJson(MapsCommon.readContent(MapsCommon.getResource("/data_sample_upload.json")), Object.class);

		DatasUploadPreviewResponse datasUploadPreviewResponse = client.getDatas().uploadPreviewWithResponseAsync(UploadDataFormat.GEOJSON, contentJson, null).block();
		operationLocation = datasUploadPreviewResponse.getDeserializedHeaders().getOperationLocation();
		System.out.println(String.format("Created upload with operation_id %s", operationLocation));
		MapsCommon.print(datasUploadPreviewResponse.getValue());
		operationId =  MapsCommon.getUid(operationLocation);
		udid = MapsCommon.waitForStatusComplete(operationId, id -> getOperation(client.getDatas(), id));
		if (udid == null) {
			System.out.println("Data upload Failed");
			return;
		}
		
		try {
			MapDataListResponse list = client.getDatas().listPreview();
			System.out.println("View all previously created files:");
			MapsCommon.print(list);

			InputStream downloadData = client.getDatas().downloadPreview(udid);
			System.out.println(String.format("Downloaded file with udid %s", udid));
			Scanner downloadScanner = new Scanner(downloadData).useDelimiter("\\A");
			System.out.println(downloadScanner.hasNext() ? downloadScanner.next() : "");

	        contentJson = MapsCommon.readJson(MapsCommon.readContent(MapsCommon.getResource("/data_sample_update.json")), Object.class);

			DatasUpdatePreviewResponse updateOperation = client.getDatas().updatePreviewWithResponseAsync(udid, contentJson, null).block();
			operationLocation = updateOperation.getDeserializedHeaders().getOperationLocation();
			System.out.println(String.format("Updated file with operation_id %s", operationLocation));
			MapsCommon.print(updateOperation.getValue());
			operationId = MapsCommon.getUid(operationLocation);
			udid = MapsCommon.waitForStatusComplete(operationId, id -> getOperation(client.getDatas(), id));
			if (udid == null) {
				System.out.println("Data update Failed");
				return;
			}
		} catch(HttpResponseException err) {
			System.out.println(err);
		} finally {
			client.getDatas().deletePreview(udid);
			System.out.println(String.format("Deleted file with udid %s", udid));
		}
	}
	
	public static MapsCommon.OperationWithHeaders getOperation(Datas data, String operationId) {
		DatasGetOperationPreviewResponse result = data.getOperationPreviewWithResponseAsync(operationId).block();
		System.out.println(String.format("Get data operation with operation_id %s status %s", operationId, result.getValue().getStatus()));
	    return new MapsCommon.OperationWithHeaders(result.getValue(), result.getDeserializedHeaders().getResourceLocation());
	}
}
