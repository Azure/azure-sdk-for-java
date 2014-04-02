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

package com.microsoft.windowsazure.management.storage;

import java.util.ArrayList;

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.management.storage.models.*;
import com.microsoft.windowsazure.exception.ServiceException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class StorageAccountOperationsTests extends StorageManagementIntegrationTestBase {
	private static String testPrefix = "azuresdkteststorage";
    //lower case only for storage account name, this is existed storage account with vhd-store container, 
    //need to create your own storage account and create container there to store VM images 
    private static String storageAccountName = testPrefix + "08";

    @BeforeClass
    public static void setup() throws Exception {
        createService();
        createStorageAccount(); 
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try
        {
            StorageAccountListResponse storageServiceListResponse = storageManagementClient.getStorageAccountsOperations().list();
            ArrayList<StorageAccount> storageAccountlist = storageServiceListResponse.getStorageAccounts();
            for (StorageAccount storageAccount : storageAccountlist)
        	{ 
        		if (storageAccount.getName().contains(testPrefix))
        		{
        			storageManagementClient.getStorageAccountsOperations().delete(storageAccount.getName());
        		}
        	}
        }
        catch (ServiceException e) {
        	e.printStackTrace();
        }  
    }    
   
    private static void createStorageAccount() throws Exception {    	
        String storageAccountDescription = testPrefix + "Description1";       
        
        //Arrange
        StorageAccountCreateParameters createParameters = new StorageAccountCreateParameters();
        createParameters.setName(storageAccountName);        
        createParameters.setLabel(storageAccountDescription);
        createParameters.setGeoReplicationEnabled(false);
        createParameters.setLocation(GeoRegionNames.SOUTHCENTRALUS);       
     
        //act
        OperationResponse operationResponse = storageManagementClient.getStorageAccountsOperations().create(createParameters); 
        
        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    }    
    
    @Test
    public void createStorageAccountSuccess() throws Exception { 
    	String storageAccountName = testPrefix + "02";
        String storageAccountDescription = testPrefix + "Description2";       
        
        //Arrange
        StorageAccountCreateParameters createParameters = new StorageAccountCreateParameters();
        createParameters.setName(storageAccountName);        
        createParameters.setLabel(storageAccountDescription);
        createParameters.setGeoReplicationEnabled(false);
        createParameters.setLocation(GeoRegionNames.SOUTHCENTRALUS);        
     
        //act
        OperationResponse operationResponse = storageManagementClient.getStorageAccountsOperations().create(createParameters); 
        
        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    }    
   
    @Test
    public void getStorageAccountSuccess() throws Exception {    	
    	String storageAccountLocation = GeoRegionNames.SOUTHCENTRALUS;
    	
        //Act
        StorageAccountGetResponse storageAccountResponse = storageManagementClient.getStorageAccountsOperations().get(storageAccountName);
      
        //Assert
        Assert.assertEquals(200, storageAccountResponse.getStatusCode());
        Assert.assertNotNull(storageAccountResponse.getRequestId());
        Assert.assertEquals(storageAccountName, storageAccountResponse.getStorageAccount().getName()); 
        Assert.assertNotNull(storageAccountResponse.getStorageAccount().getUri());   
        Assert.assertEquals(storageAccountLocation, storageAccountResponse.getStorageAccount().getProperties().getLocation()); 
        Assert.assertNotNull(storageAccountResponse.getStorageAccount().getExtendedProperties());          
    }
    
    @Test
    public void checkAvailabilitySuccess() throws Exception {
    	String expectedStorageAccountName = testPrefix + "07";
        //Act       
        CheckNameAvailabilityResponse checkNameAvailabilityResponse = storageManagementClient.getStorageAccountsOperations().checkNameAvailability(expectedStorageAccountName);
        
        //Assert        	
        Assert.assertEquals(true, checkNameAvailabilityResponse.isAvailable()); 
    }
    
    @Test
    public void getKeySuccess() throws Exception {
    	//Act       
    	StorageAccountGetKeysResponse  storageAccountGetKeysResponse = storageManagementClient.getStorageAccountsOperations().getKeys(storageAccountName);
        
        //Act  
    	Assert.assertEquals(200, storageAccountGetKeysResponse.getStatusCode());
    	Assert.assertNotNull(storageAccountGetKeysResponse.getRequestId());
    	Assert.assertNotNull(storageAccountGetKeysResponse.getPrimaryKey()); 
    	Assert.assertNotNull(storageAccountGetKeysResponse.getSecondaryKey());
    }
    
    @Test
    public void generateKeysSuccess() throws Exception {
    	StorageAccountRegenerateKeysParameters storageAccountRegenerateKeysParameters = new StorageAccountRegenerateKeysParameters();    	
    	storageAccountRegenerateKeysParameters.setName(storageAccountName);
    	storageAccountRegenerateKeysParameters.setKeyType(StorageKeyType.Primary); 
    	
        //Act   
    	StorageAccountGetKeysResponse  storageAccountGetKeysResponse = storageManagementClient.getStorageAccountsOperations().getKeys(storageAccountName);
    	StorageAccountRegenerateKeysResponse  storageAccountRegenerateKeysResponse = storageManagementClient.getStorageAccountsOperations().regenerateKeys(storageAccountRegenerateKeysParameters);
        
        //assert  
    	Assert.assertEquals(200, storageAccountGetKeysResponse.getStatusCode());
        Assert.assertNotNull(storageAccountGetKeysResponse.getRequestId());
        Assert.assertNotEquals(storageAccountGetKeysResponse.getPrimaryKey(), storageAccountRegenerateKeysResponse.getPrimaryKey()); 
    } 
    
    @Test
    public void listStorageAccountSuccess() throws Exception {
    	//Arrange  
    	StorageAccountListResponse storageAccountListResponse = storageManagementClient.getStorageAccountsOperations().list();
    	ArrayList<StorageAccount> storageAccountlist =  storageAccountListResponse.getStorageAccounts();
    	for (StorageAccount storageAccount : storageAccountlist)
    	{    		 
    		if (storageAccount.getName().contains(testPrefix))
    		{
    			storageManagementClient.getStorageAccountsOperations().delete(storageAccount.getName());
    		}
    	}
    	Assert.assertNotNull(storageAccountlist);       
    }    
   
    @Test
    public void updateStorageAccountSuccess() throws Exception {
    	//Arrange 
    	String expectedStorageAccountName = testPrefix + "03";
    	String expectedStorageAccountLabel = testPrefix + "UpdateLabel3";
	         
    	String expectedUpdatedStorageAccountLabel = "testStorageAccountUpdatedLabel3";	        
    	String expectedUpdatedDescription = "updatedStorageAccountsuccess3";
	         
    	StorageAccountCreateParameters createParameters = new StorageAccountCreateParameters();
    	createParameters.setName(expectedStorageAccountName);
    	createParameters.setLocation(GeoRegionNames.SOUTHCENTRALUS);
    	createParameters.setLabel(expectedStorageAccountLabel);
    	createParameters.setGeoReplicationEnabled(true);
	        
    	//Act
    	OperationResponse operationResponse = storageManagementClient.getStorageAccountsOperations().create(createParameters); 
    	Assert.assertEquals(200, operationResponse.getStatusCode());	       
	        
    	StorageAccountUpdateParameters updateParameters = new StorageAccountUpdateParameters();      
    	updateParameters.setLabel(expectedUpdatedStorageAccountLabel);
    	updateParameters.setGeoReplicationEnabled(false);
    	updateParameters.setDescription(expectedUpdatedDescription);
    	OperationResponse updateoperationResponse = storageManagementClient.getStorageAccountsOperations().update(expectedStorageAccountName, updateParameters);
			        
    	//Assert
    	Assert.assertEquals(200, updateoperationResponse.getStatusCode());
    	Assert.assertNotNull(updateoperationResponse.getRequestId());	        
    }
}