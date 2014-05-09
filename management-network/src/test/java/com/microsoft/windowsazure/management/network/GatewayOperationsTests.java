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

package com.microsoft.windowsazure.management.network;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.management.network.models.*;
import com.microsoft.windowsazure.exception.ServiceException;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class GatewayOperationsTests extends NetworkManagementIntegrationTestBase {
    
    @BeforeClass
    public static void setup() throws Exception {
        createService();
        networkOperations = networkManagementClient.getNetworksOperations();
        gatewayOperations = networkManagementClient.getGatewaysOperations();
        testNetworkName = testNetworkPrefix + randomString(10);
        testGatewayName = testGatewayPrefix + randomString(10);
        createNetwork(testNetworkName);
    }

    @AfterClass
    public static void cleanup() {
        deleteNetwork(testNetworkName);
        
        try {
            gatewayOperations.delete(testNetworkName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }
    
    @Test(expected = ExecutionException.class)
    public void createGatewayOnEmptyNetworkFailed() throws Exception {     
        // Arrange
        GatewayCreateParameters gatewayCreateParameters = new GatewayCreateParameters();
        gatewayCreateParameters.setGatewayType(GatewayType.StaticRouting);
        
        // Act
        OperationResponse operationResponse = gatewayOperations.create(testNetworkName, gatewayCreateParameters);
        
        // Assert
        Assert.assertEquals(201, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    }
    
    @Test
    public void getGatewaySuccess() throws Exception {
        // Act
        GatewayGetResponse gatewayGetResponse = gatewayOperations.get(testNetworkName);
        // Assert
        Assert.assertEquals(200, gatewayGetResponse.getStatusCode());
        Assert.assertNotNull(gatewayGetResponse.getRequestId());
    }
    
    @Test(expected = ServiceException.class)
    public void listGatewayFailedWithInsufficientPermission() throws Exception {
        // Arrange  
        GatewayListConnectionsResponse gatewayListConnectionsResponse = gatewayOperations.listConnections(testNetworkName);
        ArrayList<GatewayListConnectionsResponse.GatewayConnection> gatewayConnectionlist = gatewayListConnectionsResponse.getConnections();
        for (GatewayListConnectionsResponse.GatewayConnection gatewayConnection : gatewayConnectionlist )    { 
            assertNotNull(gatewayConnection.getAllocatedIPAddresses());
            assertNotNull(gatewayConnection.getConnectivityState());
            assertNotNull(gatewayConnection.getEgressBytesTransferred());
            assertNotNull(gatewayConnection.getIngressBytesTransferred());
            assertNotNull(gatewayConnection.getLastConnectionEstablished());
            assertNotNull(gatewayConnection.getLastEvent());
            assertNotNull(gatewayConnection.getLocalNetworkSiteName());
        }
    }
}