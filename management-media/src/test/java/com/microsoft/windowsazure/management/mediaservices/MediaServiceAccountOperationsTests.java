/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.management.mediaservices;

import java.util.ArrayList;
import com.microsoft.windowsazure.management.mediaservices.models.*;
import com.microsoft.windowsazure.management.mediaservices.models.MediaServicesAccountListResponse.MediaServiceAccount;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class MediaServiceAccountOperationsTests extends MediaServiceManagementIntegrationTestBase {    
    private static String mediaServicesAccountName; 
    private static String storageAccountName;

    @BeforeClass
    public static void setup() throws Exception {
        mediaServicesAccountName = testMediaServicesAccountPrefix + randomString(10);
        storageAccountName = testStoragePrefix + randomString(5);        
       
        createManagementClient();
        getLocation(); 
        
        createStorageManagementClient();
        createStorageAccount(storageAccountName); 
        
        createMediaServiceManagementClient();
        createMediaServicesAccount();
    }

    @AfterClass
    public static void cleanup() {    	
        cleanMediaServicesAccount();
        cleanStorageAccount(storageAccountName);
    }    
   
    private static void createMediaServicesAccount() throws Exception { 
        //Arrange
        MediaServicesAccountCreateParameters createParameters = new MediaServicesAccountCreateParameters();
        createParameters.setAccountName(mediaServicesAccountName);
        createParameters.setRegion(storageLocation);
        createParameters.setStorageAccountName(storageAccountName); 
        createParameters.setStorageAccountKey(storageAccountKey);
        createParameters.setBlobStorageEndpointUri(storageEndpointUri);
     
        //act
        MediaServicesAccountCreateResponse operationResponse = mediaServicesManagementClient.getAccountsOperations().create(createParameters);
        
        //Assert
        Assert.assertEquals(201, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    }    
    
    @Test
    public void createMediaServicesAccountSuccess() throws Exception { 
        String mediaServicesAccountName = testMediaServicesAccountPrefix + randomString(7);

        //Arrange
        MediaServicesAccountCreateParameters createParameters = new MediaServicesAccountCreateParameters();
        createParameters.setAccountName(mediaServicesAccountName);
        createParameters.setRegion(storageLocation);
        createParameters.setStorageAccountName(storageAccountName); 
        createParameters.setStorageAccountKey(storageAccountKey);
        createParameters.setBlobStorageEndpointUri(storageEndpointUri);

        //act
        MediaServicesAccountCreateResponse operationResponse = mediaServicesManagementClient.getAccountsOperations().create(createParameters); 

        //Assert
        Assert.assertEquals(201, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    }    
   
    @Test
    public void getMediaServicesAccountSuccess() throws Exception { 
        //Act
        MediaServicesAccountGetResponse mediaServicesAccountResponse = mediaServicesManagementClient.getAccountsOperations().get(mediaServicesAccountName);
      
        //Assert
        Assert.assertEquals(200, mediaServicesAccountResponse.getStatusCode());
        Assert.assertNotNull(mediaServicesAccountResponse.getRequestId());
        Assert.assertEquals(mediaServicesAccountName, mediaServicesAccountResponse.getAccount().getAccountName());
    }    
    
    @Test
    public void listMediaServicesAccountSuccess() throws Exception {
        //Arrange  
        MediaServicesAccountListResponse mediaServicesAccountListResponse = mediaServicesManagementClient.getAccountsOperations().list();
        ArrayList<MediaServiceAccount> MediaServiceAccountlist =  mediaServicesAccountListResponse.getAccounts();
        Assert.assertNotNull(MediaServiceAccountlist);       
    } 
}