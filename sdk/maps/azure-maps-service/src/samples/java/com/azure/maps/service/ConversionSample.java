package com.azure.maps.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.maps.service.models.AliasCreateResponseV2;
import com.azure.maps.service.models.AliasListItem;
import com.azure.maps.service.models.ConversionListDetailInfo;
import com.azure.maps.service.models.ConversionsConvertResponse;
import com.azure.maps.service.models.ConversionsGetOperationResponse;
import com.azure.maps.service.models.LongRunningOperationResult;

public class ConversionSample {
	public static void main(String[] args) throws InterruptedException {
		if (args.length != 2) {
			System.out.println("Usage ConversionSample.java <udid> <dont_delete(default false)>");
		}
		if (args.length < 1) {
			return;
		}
		String udid = args[0];
		boolean doDelete = true;
		if (args.length >= 2) {
			doDelete = false;
		}
		Conversions conversion = MapsCommon.createMapsClient().getConversions();
		String operationId = ConversionSample.convert(conversion, udid);
		String conversionId = MapsCommon.waitForStatusComplete(operationId, id -> ConversionSample.getOperation(conversion, id));
		if (conversionId == null) {
			System.out.println("Conversion Failed");
			return;
		}
		try {
			get(conversion, conversionId);
			list(conversion);
		} catch(HttpResponseException err) {
			System.out.println(err);
		} finally {
			if(doDelete) {
				delete(conversion, conversionId);
			}
		}
	}
	
	public static MapsCommon.OperationWithHeaders getOperation(Conversions conversion, String operationId) {
		ConversionsGetOperationResponse conversionsGetOperationResponse = conversion.getOperationWithResponseAsync(operationId).block();
		System.out.println(String.format("Get conversion operation with operation_id %s and result %s", operationId, conversionsGetOperationResponse.getValue().getStatus()));
	    return new MapsCommon.OperationWithHeaders(conversionsGetOperationResponse.getValue(), conversionsGetOperationResponse.getDeserializedHeaders().getResourceLocation());
	}

	public static String convert(Conversions conversion, String udid) {
		ConversionsConvertResponse response = conversion.convertWithResponseAsync(udid, "facility-2.0", null).block();
		String operationLocation = response.getDeserializedHeaders().getOperationLocation();
		System.out.println(String.format("Created conversion with operation_id %s", operationLocation));
		return MapsCommon.getUid(operationLocation);
	}
	
	public static void get(Conversions conversion, String conversionId) {
		ConversionListDetailInfo conversionListDetailInfo = conversion.get(conversionId);
		System.out.println(String.format("Got conversion with id %s udid %s", conversionListDetailInfo.getConversionId(), conversionListDetailInfo.getUdid()));
	}
	
	public static void delete(Conversions conversion, String conversionId) {
		conversion.delete(conversionId);
		System.out.println(String.format("Deleted conversion with id %s", conversionId));
	}
	public static void list(Conversions conversion) {
		PagedIterable<ConversionListDetailInfo> conversionList = conversion.list();
		System.out.println("View all conversions:");
		conversionList.forEach(conversionListItem -> System.out.println(conversionListItem.getConversionId()));
	}
}
