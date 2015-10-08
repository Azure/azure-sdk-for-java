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

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.network.models.GatewayCreateParameters;
import com.microsoft.windowsazure.management.network.models.GatewayGetResponse;
import com.microsoft.windowsazure.management.network.models.GatewayListConnectionsResponse;
import com.microsoft.windowsazure.management.network.models.GatewayType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;

public class GatewayOperationsTests extends NetworkManagementIntegrationTestBase {
    
    @BeforeClass
    public static void setup() throws Exception {
        createService();
        testNetworkName = testNetworkPrefix + "got" + randomString(10);
        testGatewayName = testGatewayPrefix + "got" + randomString(10);
        
        addRegexRule(testNetworkPrefix + "got[a-z]{10}");
        addRegexRule(testGatewayPrefix + "got[a-z]{10}");
        
        setupTest(GatewayOperationsTests.class.getSimpleName());
        networkOperations = networkManagementClient.getNetworksOperations();
        gatewayOperations = networkManagementClient.getGatewaysOperations();
        createNetwork(testNetworkName);
        resetTest(GatewayOperationsTests.class.getSimpleName());
    }

    @AfterClass
    public static void cleanup() throws Exception {
        setupTest(GatewayOperationsTests.class.getSimpleName() + CLEANUP_SUFFIX);
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
        } finally {
            resetTest(GatewayOperationsTests.class.getSimpleName() + CLEANUP_SUFFIX);
        }
    }
    
    @Before
    public void beforeTest() throws Exception {
        setupTest();
    }
    
    @After
    public void afterTest() throws Exception {
        resetTest();
    }
    
    @Test(expected = ExecutionException.class)
    public void createGatewayOnEmptyNetworkFailed() throws Exception {     
        // Arrange
        GatewayCreateParameters gatewayCreateParameters = new GatewayCreateParameters();
        gatewayCreateParameters.setGatewayType(GatewayType.STATICROUTING);
        
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