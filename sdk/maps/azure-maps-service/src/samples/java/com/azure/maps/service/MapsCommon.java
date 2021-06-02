package com.azure.maps.service;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.maps.service.models.ErrorDetail;
import com.azure.maps.service.models.LongRunningOperationResult;
import com.azure.maps.service.models.LroStatus;

public class MapsCommon {
	public static class OperationWithHeaders {
		public final LongRunningOperationResult longRunningOperationResult;
		public final String resourceLocation;
		public OperationWithHeaders(LongRunningOperationResult longRunningOperationResult, String resourceLocation) {
			this.longRunningOperationResult = longRunningOperationResult;
			this.resourceLocation = resourceLocation;
		}
	}
	public static MapsClient createMapsClient() {
    	//return new MapsClientBuilder()
    	//    .credential(new AzureKeyCredential());
    	HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key", new AzureKeyCredential("ZWLByDSFufxmSdnu5Sh1B8U84FkiltUdlRRMiJ6JIKU"));
    	return new MapsClientBuilder().addPolicy(policy).buildClient();
    			
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
	    return MapsCommon.getUid(status.resourceLocation);
	}
}
