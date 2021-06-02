package com.azure.maps.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.maps.service.models.AliasCreateResponseV2;
import com.azure.maps.service.models.AliasListItem;
import com.azure.maps.service.models.ConversionListDetailInfo;
import com.azure.maps.service.models.ConversionsConvertResponse;
import com.azure.maps.service.models.ConversionsGetOperationResponse;
import com.azure.maps.service.models.DatasGetOperationPreviewResponse;
import com.azure.maps.service.models.DatasUploadPreviewResponse;
import com.azure.maps.service.models.LongRunningOperationResult;
import com.azure.maps.service.models.UploadDataFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataSample {
	public static void main(String[] args) throws InterruptedException, IOException {
		if (args.length != 1) {
			System.out.println("Usage DataSample.java <dont_delete(default false)>");
		}
		boolean doDelete = true;
		if (args.length >= 1) {
			doDelete = false;
		}
		Datas data = MapsCommon.createMapsClient().getDatas();
		String operationId = DataSample.upload(data);
		String udid = MapsCommon.waitForStatusComplete(operationId, id -> DataSample.getOperation(data, id));
		if (udid == null) {
			System.out.println("Data upload Failed");
			return;
		}
		try {
			//get(conversion, conversionId);
			//list(conversion);
		} catch(HttpResponseException err) {
			System.out.println(err);
		} finally {
			if(doDelete) {
				delete(conversion, conversionId);
			}
		}
	}
	
	public static MapsCommon.OperationWithHeaders getOperation(Datas data, String operationId) {
		DatasGetOperationPreviewResponse dataGetOperationResponse = data.getOperationPreviewWithResponseAsync(operationId).block();
		System.out.println(String.format("Get data operation with operation_id %s and result %s", operationId, dataGetOperationResponse.getValue().getStatus()));
	    return new MapsCommon.OperationWithHeaders(dataGetOperationResponse.getValue(), dataGetOperationResponse.getDeserializedHeaders().getResourceLocation());
	}

	public static String upload(Datas data) throws IOException {
		InputStream stream = new DataSample().getClass().getResourceAsStream("/data_sample_upload.json");
		java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
		String content = s.hasNext() ? s.next() : "";
        ObjectMapper mapper = new ObjectMapper();
        Object contentJson = mapper.readValue(content, Object.class);

		DatasUploadPreviewResponse response = data.uploadPreviewWithResponseAsync(UploadDataFormat.GEOJSON, contentJson, null).block();
		String operationLocation = response.getDeserializedHeaders().getOperationLocation();
		System.out.println(String.format("Created upload with operation_id %s", operationLocation));
		return MapsCommon.getUid(operationLocation);
	}
	
	public static String uploadZip(Datas data) throws IOException {
		InputStream stream = new DataSample().getClass().getResourceAsStream("/data_sample_upload.zip");

		DatasUploadPreviewResponse response = data.uploadPreviewWithResponseAsync(UploadDataFormat.DWGZIPPACKAGE, stream.readAllBytes(), null).block();
		String operationLocation = response.getDeserializedHeaders().getOperationLocation();
		System.out.println(String.format("Created upload with operation_id %s", operationLocation));
		return MapsCommon.getUid(operationLocation);
	}
}
