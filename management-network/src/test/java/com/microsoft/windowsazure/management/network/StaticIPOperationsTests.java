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

import com.microsoft.windowsazure.exception.ServiceException;

import java.net.InetAddress;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StaticIPOperationsTests extends NetworkManagementIntegrationTestBase {
    
    @BeforeClass
    public static void setup() throws Exception {
        createService();
        testNetworkName = testNetworkPrefix + "sio" + randomString(10);
        addRegexRule(testNetworkPrefix + "sio[a-z]{10}");
        
        setupTest("StaticIPOperationsTests");
        networkOperations = networkManagementClient.getNetworksOperations();
        createNetwork(testNetworkName);
        staticIPOperations = networkManagementClient.getStaticIPsOperations();
        resetTest("StaticIPOperationsTests");
    }
    
    @AfterClass
    public static void cleanup() throws Exception {
        setupTest("StaticIPOperationsTestsCleanup");
        deleteNetwork(testNetworkName);
        resetTest("StaticIPOperationsTestsCleanup");
    }

    @Before
    public void beforeTest() throws Exception {
        setupTest();
    }
    
    @After
    public void afterTest() throws Exception {
        resetTest();
    }
    
    @Test(expected = ServiceException.class)
    public void checkIllegalIPAddressFailed() throws Exception {
        InetAddress ipAddress = InetAddress.getLocalHost();

        // Act
        staticIPOperations.check(testNetworkName, ipAddress);
    }
}