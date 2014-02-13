/*
 * Copyright Microsoft.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.management.website;

import java.util.ArrayList;

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.management.websites.*;
import com.microsoft.windowsazure.management.websites.models.*;
import com.microsoft.windowsazure.tracing.CloudTracing;
import com.microsoft.windowsazure.exception.ServiceException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ServerFarmOperationsTests extends WebSiteManagementIntegrationTestBase {
    
    @BeforeClass
    public static void setup() throws Exception {
        createService();
        cleanup();       
    }

    @AfterClass
    public static void cleanup() throws Exception {
    	 String webSpaceName = "northcentraluswebspace"; 
        try
        {
        	 ServerFarmListResponse ServerFarmListResponse = webSiteManagementClient.getServerFarmsOperations().list(webSpaceName);
        	 ArrayList<ServerFarmListResponse.ServerFarm> serverFarmAccountlist = ServerFarmListResponse.getServerFarms();        	
        	 webSiteManagementClient.getServerFarmsOperations().delete(webSpaceName);        	
        	
        }
        catch (ServiceException e) {
            e.printStackTrace();
        }  
    }
    
    @Test
    public void createServerFarmSuccess() throws Exception {    	
        String webSpaceName = "northcentraluswebspace";
        int currentNumberOfWorkersValue = 2; 
        int numberOfWorkersValue = 2;
        
        // Arrange
        ServerFarmCreateParameters createParameters = new ServerFarmCreateParameters();
        createParameters.setCurrentNumberOfWorkers(currentNumberOfWorkersValue); 
        createParameters.setCurrentWorkerSize(ServerFarmWorkerSize.Small);
        createParameters.setNumberOfWorkers(numberOfWorkersValue);
        createParameters.setStatus(ServerFarmStatus.Pending);        
        createParameters.setWorkerSize(ServerFarmWorkerSize.Small);       
        
        // Act
        ServerFarmCreateResponse serverFarmCreateResponse = webSiteManagementClient.getServerFarmsOperations().create(webSpaceName, createParameters);
        
        // Assert
        Assert.assertEquals(200,  serverFarmCreateResponse.getStatusCode());
        Assert.assertNotNull(serverFarmCreateResponse.getRequestId());
        Assert.assertEquals(currentNumberOfWorkersValue, serverFarmCreateResponse.getCurrentNumberOfWorkers()); 
        Assert.assertEquals(ServerFarmWorkerSize.Small, serverFarmCreateResponse.getCurrentWorkerSize()); 
    }    
   
    @Test
    public void getServerFarmSuccess() throws Exception {    	
        String webSpaceName = "eastuswebspace";
        String serverFarmName = "";
       
        // Act
        ServerFarmGetResponse serverFarmGetResponse = webSiteManagementClient.getServerFarmsOperations().get(webSpaceName, serverFarmName);

        // Assert
        Assert.assertEquals(200, serverFarmGetResponse.getStatusCode());
        Assert.assertNotNull(serverFarmGetResponse.getRequestId());        
    }  
    
    
    @Test
    public void listServerFarmSuccess() throws Exception {    	
        String webSpaceName = "eastuswebspace"; 
       
        // Act
        ServerFarmListResponse serverFarmListResponse = webSiteManagementClient.getServerFarmsOperations().list(webSpaceName);

        // Assert
        Assert.assertEquals(200, serverFarmListResponse.getStatusCode());
        Assert.assertNotNull(serverFarmListResponse.getRequestId());
        
         ArrayList<ServerFarmListResponse.ServerFarm> serverFarmslist = serverFarmListResponse.getServerFarms(); 
	   	for (ServerFarmListResponse.ServerFarm serverFarm : serverFarmslist)
	   	{ 
	   		 // Assert	   	    
	         Assert.assertEquals(3, serverFarm.getCurrentNumberOfWorkers());
	         Assert.assertEquals(3, serverFarm.getNumberOfWorkers());
	         Assert.assertEquals(ServerFarmWorkerSize.Medium, serverFarm.getCurrentWorkerSize());
	         Assert.assertEquals("DefaultServerFarm", serverFarm.getName());  
	         Assert.assertEquals(ServerFarmStatus.Ready, serverFarm.getStatus());
	         Assert.assertEquals(ServerFarmWorkerSize.Medium, serverFarm.getWorkerSize());	         
	   	}
    }
   
    @Test
    public void updateServerFarmSuccess() throws Exception {	   
    	String serverFarmName = "DefaultServerFarm";       
        String webSpaceName = "eastuswebspace"; 
        
        int currentNumberOfWorkersValue = 3;
        int numberOfWorkersValue = 3;
        
       // Arrange 
       ServerFarmGetResponse serverFarmGetResponse = webSiteManagementClient.getServerFarmsOperations().get(webSpaceName, serverFarmName);

	   // Act 	        
	   ServerFarmUpdateParameters updateParameters = new ServerFarmUpdateParameters();
	   updateParameters.setCurrentNumberOfWorkers(currentNumberOfWorkersValue);
	   updateParameters.setCurrentWorkerSize(ServerFarmWorkerSize.Medium);       
	   updateParameters.setNumberOfWorkers(numberOfWorkersValue); 
	   updateParameters.setStatus(ServerFarmStatus.Ready);
	   updateParameters.setWorkerSize(ServerFarmWorkerSize.Medium); 
	   ServerFarmUpdateResponse updateOperationResponse = webSiteManagementClient.getServerFarmsOperations().update(webSpaceName, updateParameters);	        
	   // Assert
	   Assert.assertEquals(200,  updateOperationResponse.getStatusCode());
	   Assert.assertNotNull(updateOperationResponse.getRequestId());
	   
	   Assert.assertEquals(3, updateOperationResponse.getCurrentNumberOfWorkers());
       Assert.assertEquals(3, updateOperationResponse.getNumberOfWorkers());
       Assert.assertEquals(ServerFarmWorkerSize.Medium, updateOperationResponse.getCurrentWorkerSize());
       Assert.assertEquals("DefaultServerFarm", updateOperationResponse.getName());  
       Assert.assertEquals(ServerFarmStatus.Ready, updateOperationResponse.getStatus());
       Assert.assertEquals(ServerFarmWorkerSize.Medium, updateOperationResponse.getWorkerSize()); 
    }
}