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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class NetworkOperationsTests extends NetworkManagementIntegrationTestBase {
    
    @BeforeClass
    public static void setup() throws Exception {
        createService();         
    }
   
    
    @Test
    public void setConfiguration() throws Exception {    	
         String configurationValue = "﻿<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
"<NetworkConfiguration xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.microsoft.com/ServiceHosting/2011/07/NetworkConfiguration\">"+
  "<VirtualNetworkConfiguration> " +
  "<Dns /> " +
    "<VirtualNetworkSites>" +
      "<VirtualNetworkSite name=\"testsdkVirtualNetwork03\" AffinityGroup=\"testsdkVirtualNetwork01AG\"> " +
        "<AddressSpace> " +
          "<AddressPrefix>172.16.0.0/28</AddressPrefix> " +
        "</AddressSpace> " +
        "<Subnets> " +
          "<Subnet name=\"Subnet-2\"> " +
            "<AddressPrefix>172.16.0.0/28</AddressPrefix> " +
          "</Subnet> " +
        "</Subnets> " +
      "</VirtualNetworkSite> " +
    "</VirtualNetworkSites>" +
  "</VirtualNetworkConfiguration>" +
"</NetworkConfiguration>";
        // Arrange
        NetworkSetConfigurationParameters createParameters = new NetworkSetConfigurationParameters();
        createParameters.setConfiguration(configurationValue);      
        
        // Act
        OperationResponse operationResponse = networkManagementClient.getNetworksOperations().setConfiguration(createParameters);
        
        // Assert
        Assert.assertEquals(201, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    }
    
    @Test
    public void getNetworks() throws Exception {
    	String NetworkName = "testsdkVirtualNetwork01";
        // Act
        NetworkGetConfigurationResponse networkGetConfigurationResponse = networkManagementClient.getNetworksOperations().getConfiguration();

        // Assert
        Assert.assertEquals(200, networkGetConfigurationResponse.getStatusCode());
        Assert.assertNotNull(networkGetConfigurationResponse.getRequestId());
        System.out.println("networksite.getConfiguration() = " + networkGetConfigurationResponse.getConfiguration());
        //Assert.assertNotNull(NetworkResponse.getCapabilities());    
        //Assert.assertEquals(NetworkName1, NetworkResponse.getName());  
        //Assert.assertEquals(NetworkLocation1, NetworkResponse.getLocation());
        //Assert.assertEquals(Networklabel1, NetworkResponse.getLabel());
    }
    
    @Test
    public void listNetworksSuccess() throws Exception {
        // Arrange  
    	 NetworkListResponse NetworkListResponse = networkManagementClient.getNetworksOperations().list();
    	 ArrayList<NetworkListResponse.VirtualNetworkSite> virtualnetwoksitelist = NetworkListResponse.getVirtualNetworkSites();
    	 for (NetworkListResponse.VirtualNetworkSite networksite : virtualnetwoksitelist)
    	 {
    		 System.out.println("networksite.getName() = " + networksite.getName());
    		 System.out.println("networksite.getAG() = " + networksite.getAffinityGroup());
    		 System.out.println("networksite.getid() = " + networksite.getId());
    		 System.out.println("networksite.getstate() = " + networksite.getState());
    		 System.out.println("networksite.getlabel() = " + networksite.getLabel());
    		 
    		 System.out.println("networksite.getaddressspace = " + networksite.getAddressSpace());
    		 System.out.println("networksite.getdnsserver() = " + networksite.getDnsServers());
    		 System.out.println("networksite.getgetways() = " + networksite.getGateway());
    		 System.out.println("networksite.getsubnet() = " + networksite.getSubnets());
    		
//        	 if (networksite.getName().contains("testsdkNetwork"))
//        	 {
//                //virtualnetworkManagementClient.getNetworksOperations().delete(networksite.getName(), true);
//        	 }
    	 }     
    }
}