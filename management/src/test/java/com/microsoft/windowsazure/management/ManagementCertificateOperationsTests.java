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

package com.microsoft.windowsazure.management;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.management.models.ManagementCertificateGetResponse;
import com.microsoft.windowsazure.management.models.ManagementCertificateListResponse;

public class ManagementCertificateOperationsTests extends ManagementIntegrationTestBase {
    @BeforeClass
    public static void setup() throws Exception {
        createService();       
    }   
    
    @Test
    public void createManagementCertificate() throws Exception {
        // Arrange
    	
//    	ManagementCertificateCreateParameters createParameters = new ManagementCertificateCreateParameters();
////        createParameters.setData(dataValue);
////        createParameters.setPublicKey(publicKeyValue);
////        createParameters.setThumbprint(thumbprintValue);
//        
//        // Act
//        OperationResponse operationResponse = managementClient.getManagementCertificatesOperations().create(createParameters);
//        
//        // Assert
//        Assert.assertEquals(201, operationResponse.getStatusCode());
//        Assert.assertNotNull(operationResponse.getRequestId());
    }
    
    @Test
    public void getManagementCertificateSuccess() throws Exception {
    	
        // arrange
   	    ManagementCertificateListResponse managementCertificateListResponse = managementClient.getManagementCertificatesOperations().list();
   	    ArrayList<ManagementCertificateListResponse.SubscriptionCertificate> managementCertificatelist = managementCertificateListResponse.getSubscriptionCertificates();
   	 
   	    if (managementCertificatelist.size() > 0) {
   	    	String thumbprint = managementCertificatelist.get(0).getThumbprint();

   	    	ManagementCertificateGetResponse managementCertificateResponse = managementClient.getManagementCertificatesOperations().get(thumbprint);

   	        // Assert
   	        Assert.assertEquals(200, managementCertificateResponse.getStatusCode());
   	        Assert.assertNotNull(managementCertificateResponse.getRequestId()); 
   	        Assert.assertEquals(thumbprint, managementCertificateResponse.getThumbprint());    
   	    }
    }
    
    @Test
    public void listManagementCertificateSuccess() throws Exception {
        // Arrange  
    	 ManagementCertificateListResponse managementCertificateListResponse = managementClient.getManagementCertificatesOperations().list();
    	 ArrayList<ManagementCertificateListResponse.SubscriptionCertificate> managementCertificatelist = managementCertificateListResponse.getSubscriptionCertificates();
         
    	 Assert.assertNotNull(managementCertificatelist);;        
    }
}
