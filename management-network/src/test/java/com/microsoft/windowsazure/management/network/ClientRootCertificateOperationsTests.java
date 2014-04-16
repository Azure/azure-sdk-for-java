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

import static org.junit.Assert.*;

import java.util.ArrayList;

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.management.network.models.*;
import com.microsoft.windowsazure.exception.ServiceException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientRootCertificateOperationsTests extends NetworkManagementIntegrationTestBase {
    @BeforeClass
    public static void setup() throws Exception {
        testNetworkName =  testNetworkPrefix + randomString(10);;
        createService();
        networkOperations = networkManagementClient.getNetworksOperations();
        createNetwork(testNetworkName);
        clientRootCertificateOperations = networkManagementClient.getClientRootCertificatesOperations();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
            ClientRootCertificateListResponse ClientRootCertificateListResponse = clientRootCertificateOperations.list(testNetworkName);
            ArrayList<ClientRootCertificateListResponse.ClientRootCertificate> clientRootCertificatelist = ClientRootCertificateListResponse.getClientRootCertificates();
            for (ClientRootCertificateListResponse.ClientRootCertificate clientRootCertificate : clientRootCertificatelist) {
                clientRootCertificateOperations.delete(testNetworkName, clientRootCertificate.getThumbprint());
            }
        } catch (ServiceException e) {
        }
    }
    
    @Test(expected = ServiceException.class)
    public void createClientInvalidRootCertificatesFailed() throws Exception {
         String certificateValue = "InvalidRootCertificate";
        // Arrange
        ClientRootCertificateCreateParameters createParameters = new ClientRootCertificateCreateParameters();
        createParameters.setCertificate(certificateValue); 
        
        // Act
        OperationResponse operationResponse = clientRootCertificateOperations.create(testNetworkName, createParameters);
        
        // Assert
        Assert.assertEquals(201, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    }
    
    @Test
    public void getClientRootCertificates() throws Exception {
        ClientRootCertificateListResponse ClientRootCertificateListResponse = networkManagementClient.getClientRootCertificatesOperations().list(testNetworkName);
        ArrayList<ClientRootCertificateListResponse.ClientRootCertificate> clientRootCertificatelist = ClientRootCertificateListResponse.getClientRootCertificates();
        for (ClientRootCertificateListResponse.ClientRootCertificate clientRootCertificate : clientRootCertificatelist) { 
            ClientRootCertificateGetResponse clientRootCertificateGetResponse = networkManagementClient.getClientRootCertificatesOperations().get(testNetworkName, clientRootCertificate.getThumbprint());
            Assert.assertEquals(200, clientRootCertificateGetResponse.getStatusCode());
            Assert.assertNotNull(clientRootCertificateGetResponse.getRequestId());
            Assert.assertNotNull(clientRootCertificateGetResponse.getCertificate());
        }
    }
    
    @Test
    public void listClientRootCertificatesSuccess() throws Exception {
        try {
             ClientRootCertificateListResponse ClientRootCertificateListResponse = networkManagementClient.getClientRootCertificatesOperations().list(testNetworkName);
             ArrayList<ClientRootCertificateListResponse.ClientRootCertificate> clientRootCertificatelist = ClientRootCertificateListResponse.getClientRootCertificates();
             for (ClientRootCertificateListResponse.ClientRootCertificate clientRootCertificate : clientRootCertificatelist) {
                assertNotNull(clientRootCertificate.getThumbprint());
                assertNotNull(clientRootCertificate.getExpirationTime());
                assertNotNull(clientRootCertificate.getSubject());
             }
        } catch (ServiceException e) {
        }
    }
}