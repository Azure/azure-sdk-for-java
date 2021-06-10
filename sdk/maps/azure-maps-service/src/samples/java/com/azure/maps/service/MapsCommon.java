package com.azure.maps.service;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.maps.service.models.ErrorDetail;
import com.azure.maps.service.models.LongRunningOperationResult;
import com.azure.maps.service.models.LroStatus;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

public class MapsCommon {
	public static class OperationWithHeaders {
		public final LongRunningOperationResult longRunningOperationResult;
		public final String resourceLocation;
		public OperationWithHeaders(LongRunningOperationResult longRunningOperationResult, String resourceLocation) {
			this.longRunningOperationResult = longRunningOperationResult;
			this.resourceLocation = resourceLocation;
		}
	}
	
	public static String getUid(String url) {
		Pattern pattern = Pattern.compile("[0-9A-Fa-f\\-]{36}");
		Matcher matcher = pattern.matcher(url);
		matcher.find();
	    return matcher.group();
	}
	
	public static String waitForStatusComplete(String operationId, Function<String, MapsCommon.OperationWithHeaders> getStatus) throws InterruptedException {
		MapsCommon.OperationWithHeaders status = getStatus.apply(operationId);
	    while (status.longRunningOperationResult.getStatus() != LroStatus.SUCCEEDED) {
	    	LongRunningOperationResult lroResult = status.longRunningOperationResult;
	        if (status.longRunningOperationResult.getStatus() == LroStatus.FAILED) {
	            System.out.println(lroResult.getError().getMessage());
	            for (ErrorDetail detail : lroResult.getError().getDetails()) {
		            for (ErrorDetail innerDetail : detail.getDetails()) {
		            	System.out.println(innerDetail.getMessage());
		            }
	            }
	            return null;
	        }
	        TimeUnit.SECONDS.sleep(15);
	        status = getStatus.apply(operationId);
	    }
	    return getUid(status.resourceLocation);
	}
	
	public static void print(Object object) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.ANY);
		mapper.disable(MapperFeature.USE_ANNOTATIONS);
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String json;
		json = ow.writeValueAsString(object);
        System.out.println(json);
	}
	
	public static InputStream getResource(String path) {
		return new MapsCommon().getClass().getResourceAsStream(path);
	}
	
	public static String readContent(InputStream stream) {
		Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
	
	public static <T> T readJson(String content, Class<T> valueType) throws JsonMappingException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(content, valueType);
	}
}
