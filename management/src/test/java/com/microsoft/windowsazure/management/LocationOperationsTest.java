package com.microsoft.windowsazure.management;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.models.LocationsListResponse;

public class LocationOperationsTest  extends ManagementIntegrationTestBase { 
	
	   @BeforeClass
	    public static void setup() throws Exception {
	        createService();	      
	    }   
	
	   
	    @Test
	    public void listLocationSuccess() throws Exception {	    	   	  	 
	    	
	    	LocationsListResponse locationsListResponse = managementClient.getLocationsOperations().list();
	    	
	    	 Assert.assertEquals(200, locationsListResponse.getStatusCode());	    	 
		     Assert.assertNotNull(locationsListResponse.getRequestId());		
		     Assert.assertEquals(8, locationsListResponse.getLocations().size());		   
	    }	    
}
    