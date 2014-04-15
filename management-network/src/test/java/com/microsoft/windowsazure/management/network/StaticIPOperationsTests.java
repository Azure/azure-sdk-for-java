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

import com.microsoft.windowsazure.management.network.models.*;
import java.net.InetAddress;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class StaticIPOperationsTests extends NetworkManagementIntegrationTestBase {
    
	   @BeforeClass
	    public static void setup() throws Exception {
	        createService();
	        testNetworkName = testNetworkPrefix + randomString(10);
	    }

    @Test
    public void check() throws Exception {
    	InetAddress ipAddress = InetAddress.getLocalHost();
    	
        // Act
    	NetworkStaticIPAvailabilityResponse networkStaticIPAvailabilityResponse = networkManagementClient.getStaticIPsOperations().check(testNetworkName, ipAddress);

        // Assert
        Assert.assertEquals(200, networkStaticIPAvailabilityResponse.getStatusCode());
        Assert.assertNotNull(networkStaticIPAvailabilityResponse.getRequestId());
        Assert.assertEquals(false, networkStaticIPAvailabilityResponse.isAvailable()); 
    }  
}