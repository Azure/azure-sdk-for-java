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

package com.microsoft.windowsazure.management.network;

import java.util.ArrayList;

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.management.network.models.*;
import com.microsoft.windowsazure.tracing.CloudTracing;
import com.microsoft.windowsazure.exception.ServiceException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientRootCertificateOperationsTests extends NetworkManagementIntegrationTestBase {
    
    @BeforeClass
    public static void setup() throws Exception {
        createService();
        //cleanup();       
    }

    @AfterClass
    public static void cleanup() throws Exception {
    	String virtualNetworkName ="";
        try
        {
        	 ClientRootCertificateListResponse ClientRootCertificateListResponse = networkManagementClient.getClientRootCertificatesOperations().list(virtualNetworkName);
        	 ArrayList<ClientRootCertificateListResponse.ClientRootCertificate> clientRootCertificatelist = ClientRootCertificateListResponse.getClientRootCertificates();
        	 for (ClientRootCertificateListResponse.ClientRootCertificate clientRootCertificate : clientRootCertificatelist)
        	 { 
            	 
        	 }
        }
        catch (ServiceException e) {
            e.printStackTrace();
        }  
    }
    
    @Test
    public void createClientRootCertificatesSuccess() throws Exception {
    	 String virtualNetworkName = "testsdkVirtualNetwork01";      
         String certificateValue ="";
        // Arrange
        ClientRootCertificateCreateParameters createParameters = new ClientRootCertificateCreateParameters();
        createParameters.setCertificate(certificateValue); 
        
        // Act
        OperationResponse operationResponse = networkManagementClient.getClientRootCertificatesOperations().create(virtualNetworkName, createParameters);
        
        // Assert
        Assert.assertEquals(201, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    }
    
    @Test
    public void getClientRootCertificates() throws Exception {
    	String certificateThumbprint = "testsdkClientRootCertificate";
    	String virtualNetworkName = "testsdkVirtualNetwork01";
        // Act
        ClientRootCertificateGetResponse clientRootCertificateGetResponse = networkManagementClient.getClientRootCertificatesOperations().get(virtualNetworkName, certificateThumbprint);

        // Assert
        Assert.assertEquals(200, clientRootCertificateGetResponse.getStatusCode());
        Assert.assertNotNull(clientRootCertificateGetResponse.getRequestId());
        Assert.assertNotNull(clientRootCertificateGetResponse.getCertificate());  
    }
    
    @Test
    public void listClientRootCertificatesSuccess() throws Exception {
    	String virtualNetworkName ="testsdkVirtualNetwork01";
        try
        {
        	 ClientRootCertificateListResponse ClientRootCertificateListResponse = networkManagementClient.getClientRootCertificatesOperations().list(virtualNetworkName);
        	 ArrayList<ClientRootCertificateListResponse.ClientRootCertificate> clientRootCertificatelist = ClientRootCertificateListResponse.getClientRootCertificates();
        	 for (ClientRootCertificateListResponse.ClientRootCertificate clientRootCertificate : clientRootCertificatelist)
        	 { 
        		 System.out.println("clientRootCertificate.getThumbprint() = " + clientRootCertificate.getThumbprint());
        		 System.out.println("clientRootCertificate.getExpirationTime() = " + clientRootCertificate.getExpirationTime());
        		 System.out.println("clientRootCertificate.getSubject() = " + clientRootCertificate.getSubject());
        	 }
        }
        catch (ServiceException e) {
            e.printStackTrace();
        }  
    }
}