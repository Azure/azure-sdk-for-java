package com.microsoft.windowsazure.management;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;
import java.lang.*;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.models.AffinityGroupCreateParameters;
import com.microsoft.windowsazure.management.models.AffinityGroupListResponse;
import com.microsoft.windowsazure.management.models.SubscriptionListOperationsParameters;
import com.microsoft.windowsazure.management.models.SubscriptionGetResponse;
import com.microsoft.windowsazure.management.models.SubscriptionListOperationsResponse;

public class SubscriptionOperationsTest  extends ManagementIntegrationTestBase { 
	
	   @BeforeClass
	    public static void setup() throws Exception {
	        createService();	      
	    }	   
	
	    @Test
	    public void getSubscriptionSuccess() throws Exception {
	        // Act
		    SubscriptionGetResponse subscriptionGetResponse = managementClient.getSubscriptionsOperations().get();
	        // Assert
	        Assert.assertEquals(200, subscriptionGetResponse.getStatusCode());
	        Assert.assertNotNull(subscriptionGetResponse.getRequestId());	        
	        Assert.assertNotNull(subscriptionGetResponse.getAccountAdminLiveEmailId()); 
	        Assert.assertNotNull(subscriptionGetResponse.getSubscriptionID()); 
	        
	        Assert.assertEquals("Azure SDK sandbox", subscriptionGetResponse.getSubscriptionName()); 	       
	        Assert.assertEquals(10, subscriptionGetResponse.getMaximumVirtualNetworkSites()); 
	        Assert.assertEquals(10, subscriptionGetResponse.getMaximumLocalNetworkSites()); 
	        Assert.assertEquals(9, subscriptionGetResponse.getMaximumDnsServers()); 
	        Assert.assertEquals(20, subscriptionGetResponse.getMaximumStorageAccounts()); 
	    }
	    
	    @Test
	    public void listSubscriptionsSuccess() throws Exception {
	    	 String  continuationToken = "testubscriptiontoken";
	        // Arrange  
	    	 SubscriptionListOperationsParameters parameters = new  SubscriptionListOperationsParameters();
	    	 Calendar startTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	    	 startTime.set(2013, 11, 30);	    	
	    	 Calendar endTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	    	 endTime.set(2014, 2, 1); 
	         parameters.setStartTime(startTime);
	    	 parameters.setEndTime(endTime);
	    	 //parameters.setContinuationToken(continuationToken);	    	 
	    	
	    	 SubscriptionListOperationsResponse subscriptionListOperationsResponse = managementClient.getSubscriptionsOperations().listOperations(parameters);
	    	
	    	 Assert.assertEquals(200, subscriptionListOperationsResponse.getStatusCode());	    	 
		     Assert.assertNotNull(subscriptionListOperationsResponse.getRequestId());		
		     Assert.assertEquals(50, subscriptionListOperationsResponse.getSubscriptionOperations().size());
		     //Assert.assertEquals(continuationToken, subscriptionListOperationsResponse.getContinuationToken());   
	    }
}
    