package com.microsoft.windowsazure.management;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.management.models.SubscriptionGetResponse;
import com.microsoft.windowsazure.management.models.SubscriptionListOperationsParameters;
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
	    	 // Arrange  
	    	 SubscriptionListOperationsParameters parameters = new  SubscriptionListOperationsParameters();

	    	 Calendar now = Calendar.getInstance();
	    	 Calendar startTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	    	 startTime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH) , now.get(Calendar.DATE - 5));
	         Calendar endTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	         endTime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH) , now.get(Calendar.DATE - 1));
	         parameters.setStartTime(startTime);
	         parameters.setEndTime(endTime);
	    	
	    	 SubscriptionListOperationsResponse subscriptionListOperationsResponse = managementClient.getSubscriptionsOperations().listOperations(parameters);
	    	
	    	 Assert.assertEquals(200, subscriptionListOperationsResponse.getStatusCode());	    	 
		     Assert.assertNotNull(subscriptionListOperationsResponse.getRequestId());		
		     Assert.assertEquals(50, subscriptionListOperationsResponse.getSubscriptionOperations().size());
	    }
}
    