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
package com.microsoft.windowsazure.management.sql;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.*;
import org.xml.sax.SAXException;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.sql.models.FirewallRule;
import com.microsoft.windowsazure.management.sql.models.FirewallRuleCreateParameters;
import com.microsoft.windowsazure.management.sql.models.FirewallRuleCreateResponse;
import com.microsoft.windowsazure.management.sql.models.FirewallRuleListResponse;
import com.microsoft.windowsazure.management.sql.models.FirewallRuleUpdateParameters;
import com.microsoft.windowsazure.management.sql.models.FirewallRuleUpdateResponse;

public class FirewallRuleOperationsIntegrationTest extends SqlManagementIntegrationTestBase {
    private static FirewallRuleOperations firewallRuleOperations;

    @BeforeClass
    public static void setup() throws Exception {
        createService();
        firewallRuleOperations = sqlManagementClient.getFirewallRulesOperations();
        serverOperations = sqlManagementClient.getServersOperations();
    }

    @AfterClass
    public static void cleanup() {
        for (String firewallRuleName : firewallRuleToBeRemoved.keySet()) {
            String serverName = firewallRuleToBeRemoved.get(firewallRuleName);
            try {
                firewallRuleOperations.delete(serverName, firewallRuleName);
            } catch (IOException e) {
            } catch (ServiceException e) {
            }
        }

        firewallRuleToBeRemoved.clear();
        
        for (String serverName : serverToBeRemoved) {
            try {
                serverOperations.delete(serverName);
            } catch (IOException e) {
            } catch (ServiceException e) {
            }
        }

        serverToBeRemoved.clear();
    }

    @Test
    public void createFirewallRuleWithRequiredParametersSuccess() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException {
        // arrange 
        String expectedServerName = createServer();
        String expectedRuleName = "AllowAll";
        InetAddress expectedStartIpAddress = InetAddress.getByName("0.0.0.0");
        InetAddress expectedEndIpAddress = InetAddress.getByName("255.255.255.255");
        String expectedType = "Microsoft.SqlAzure.FirewallRule";

        // act
        FirewallRuleCreateParameters firewallRuleCreateParameters = new FirewallRuleCreateParameters();
        firewallRuleCreateParameters.setName(expectedRuleName);
        firewallRuleCreateParameters.setStartIPAddress(expectedStartIpAddress);
        firewallRuleCreateParameters.setEndIPAddress(expectedEndIpAddress);
        FirewallRuleCreateResponse firewallRuleCreateResponse = firewallRuleOperations.create(expectedServerName, firewallRuleCreateParameters);
        FirewallRule firewallRule = firewallRuleCreateResponse.getFirewallRule();
        String firewallRuleName = firewallRule.getName();
        firewallRuleToBeRemoved.put(firewallRuleName, expectedServerName);

        // assert
        assertEquals(expectedRuleName, firewallRule.getName());
        assertEquals(expectedStartIpAddress, firewallRule.getStartIPAddress());
        assertEquals(expectedEndIpAddress, firewallRule.getEndIPAddress());
        assertEquals(expectedType, firewallRule.getType());
    }

    @Test
    public void deleteFirewallRuleSuccess() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException {
        // arrange 
        String expectedServerName = createServer();
        String expectedRuleName = "AllowAll";
        InetAddress expectedStartIpAddress = InetAddress.getByName("0.0.0.0");
        InetAddress expectedEndIpAddress = InetAddress.getByName("255.255.255.255");

        // act
        FirewallRuleCreateParameters firewallRuleCreateParameters = new FirewallRuleCreateParameters();
        firewallRuleCreateParameters.setName(expectedRuleName);
        firewallRuleCreateParameters.setStartIPAddress(expectedStartIpAddress);
        firewallRuleCreateParameters.setEndIPAddress(expectedEndIpAddress);
        FirewallRuleCreateResponse firewallRuleCreateResponse = firewallRuleOperations.create(expectedServerName, firewallRuleCreateParameters);
        FirewallRule expectedFirewallRule = firewallRuleCreateResponse.getFirewallRule();
        firewallRuleOperations.delete(expectedServerName, expectedRuleName);
        
        // assert
        FirewallRuleListResponse firewallRuleListResponse = firewallRuleOperations.list(expectedServerName);
        ArrayList<FirewallRule> firewallRuleList = firewallRuleListResponse.getFirewallRules();
        for (FirewallRule firewallRule : firewallRuleList)
        {
            assertNotEquals(expectedFirewallRule.getName(), firewallRule.getName());
        }
    }

    @Test
    public void updateRuleSuccess() throws ParserConfigurationException, SAXException, TransformerException, IOException, ServiceException {
        // arrange 
        String expectedServerName = createServer();
        String expectedRuleName = "AllowAll";
        InetAddress expectedStartIpAddress = InetAddress.getByName("0.0.0.0");
        InetAddress expectedEndIpAddress = InetAddress.getByName("255.255.255.255");
        InetAddress updatedEndIpAddress = InetAddress.getByName("255.255.255.128");

        // act
        FirewallRuleCreateParameters firewallRuleCreateParameters = new FirewallRuleCreateParameters();
        firewallRuleCreateParameters.setName(expectedRuleName);
        firewallRuleCreateParameters.setStartIPAddress(expectedStartIpAddress);
        firewallRuleCreateParameters.setEndIPAddress(expectedEndIpAddress);
        FirewallRuleCreateResponse firewallRuleCreateResponse = firewallRuleOperations.create(expectedServerName, firewallRuleCreateParameters);
        FirewallRule firewallRule = firewallRuleCreateResponse.getFirewallRule();
        String firewallRuleName = firewallRule.getName();
        firewallRuleToBeRemoved.put(firewallRuleName, expectedServerName);
        FirewallRuleUpdateParameters firewallRuleUpdateParameters = new FirewallRuleUpdateParameters();
        firewallRuleUpdateParameters.setEndIPAddress(updatedEndIpAddress);
        firewallRuleUpdateParameters.setName(expectedRuleName);
        firewallRuleUpdateParameters.setStartIPAddress(expectedStartIpAddress);
        FirewallRuleUpdateResponse firewallRuleUpdateResponse = firewallRuleOperations.update(expectedServerName, expectedRuleName, firewallRuleUpdateParameters);
        
        // assert
        FirewallRule updatedFirewallRule = firewallRuleUpdateResponse.getFirewallRule();
        assertEquals(updatedEndIpAddress, updatedFirewallRule.getEndIPAddress());
        assertEquals(expectedStartIpAddress, updatedFirewallRule.getStartIPAddress());
        assertEquals(expectedRuleName, updatedFirewallRule.getName());
    }
}