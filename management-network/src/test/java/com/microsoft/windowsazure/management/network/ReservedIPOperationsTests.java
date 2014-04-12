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

import com.microsoft.windowsazure.management.network.models.*;
import com.microsoft.windowsazure.exception.ServiceException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReservedIPOperationsTests extends NetworkManagementIntegrationTestBase {
    @BeforeClass
    public static void setup() throws Exception {
        createService();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
            // Arrange
            NetworkReservedIPListResponse networkReservedIPListResponse = networkManagementClient.getReservedIPsOperations().list();
            ArrayList<NetworkReservedIPListResponse.ReservedIP> networkReservedIPlist = networkReservedIPListResponse.getReservedIPs();
            for (NetworkReservedIPListResponse.ReservedIP reservedip : networkReservedIPlist) { 
                if (reservedip.getName().contains("testsdkReservedIP")) {
                   networkManagementClient.getReservedIPsOperations().delete(reservedip.getName());
                }
            }
        }
        catch (ServiceException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void listAndGetReservedIPSuccess() throws Exception {
        // Arrange  
        NetworkReservedIPListResponse networkReservedIPListResponse = networkManagementClient.getReservedIPsOperations().list();
        ArrayList<NetworkReservedIPListResponse.ReservedIP> networkReservedIPlist = networkReservedIPListResponse.getReservedIPs();
        for ( NetworkReservedIPListResponse.ReservedIP reservedIP : networkReservedIPlist) { 
            Assert.assertNotNull(reservedIP.getName());

            NetworkReservedIPGetResponse networkReservedIPGetResponse = networkManagementClient.getReservedIPsOperations().get(reservedIP.getName());
            // Assert
            Assert.assertEquals(200, networkReservedIPGetResponse.getStatusCode());
            Assert.assertNotNull(networkReservedIPGetResponse.getRequestId());
        }
    }
}