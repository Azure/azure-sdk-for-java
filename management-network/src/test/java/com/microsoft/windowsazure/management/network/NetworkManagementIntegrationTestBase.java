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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.microsoft.windowsazure.core.utils.BOMInputStream;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.configuration.*;
import com.microsoft.windowsazure.management.network.models.NetworkGetConfigurationResponse;
import com.microsoft.windowsazure.management.network.models.NetworkSetConfigurationParameters;
import com.microsoft.windowsazure.*;

public abstract class NetworkManagementIntegrationTestBase {

    protected static NetworkManagementClient networkManagementClient;    
    protected static NetworkOperations networkOperations;
    protected static ReservedIPOperations reservedIPOperations;
    protected static GatewayOperations gatewayOperations;
    protected static StaticIPOperations staticIPOperations;
    protected static ClientRootCertificateOperations  clientRootCertificateOperations; 
    protected static String testNetworkPrefix = "javatestvn";
    protected static String testReservedIPPrefix = "javareservedip";
    protected static String testGatewayPrefix = "javagateway";
    protected static String testNetworkName;
    protected static String testGatewayName; 

    
    protected static void createService() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();      
        networkManagementClient = NetworkManagementService.create(config);
    }

    protected static Configuration createConfiguration() throws Exception {
        String baseUri = System.getenv(ManagementConfiguration.URI);
        return ManagementConfiguration.configure(
            baseUri != null ? new URI(baseUri) : null,
            System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
            System.getenv(ManagementConfiguration.KEYSTORE_PATH),
            System.getenv(ManagementConfiguration.KEYSTORE_PASSWORD),
            KeyStoreType.fromString(System.getenv(ManagementConfiguration.KEYSTORE_TYPE)));
    }
    
    protected static void createNetwork(String networkName) throws Exception {
        NetworkGetConfigurationResponse operationResponse = networkManagementClient.getNetworksOperations().getConfiguration();

        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
        Assert.assertNotNull(operationResponse.getConfiguration());

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document responseDoc = documentBuilder.parse(new BOMInputStream(new ByteArrayInputStream(operationResponse.getConfiguration().getBytes())));
        
        NodeList list = responseDoc.getElementsByTagNameNS("http://schemas.microsoft.com/ServiceHosting/2011/07/NetworkConfiguration", "VirtualNetworkSite");
        boolean exist = false;
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getAttributes().getNamedItem("name").getTextContent().equals(networkName)) {
                exist = true;
                break;
            }
        }
        
        if (!exist) {
            Element vnets = (Element) responseDoc.getElementsByTagNameNS("http://schemas.microsoft.com/ServiceHosting/2011/07/NetworkConfiguration", "VirtualNetworkSites").item(0);
            Element vnet = responseDoc.createElementNS("http://schemas.microsoft.com/ServiceHosting/2011/07/NetworkConfiguration", "VirtualNetworkSite");
            vnet.setAttribute("name", networkName);
            vnet.setAttribute("AffinityGroup", "azuresdkci");
            
            Element addressSpace = responseDoc.createElementNS("http://schemas.microsoft.com/ServiceHosting/2011/07/NetworkConfiguration", "AddressSpace");
            vnet.appendChild(addressSpace);
           
            Element addressPrefix = responseDoc.createElementNS("http://schemas.microsoft.com/ServiceHosting/2011/07/NetworkConfiguration", "AddressPrefix");
            addressPrefix.setTextContent("10.10.0.0/8");
            addressSpace.appendChild(addressPrefix);
    
            vnets.appendChild(vnet);
            
            DOMSource domSource = new DOMSource(responseDoc);
            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(domSource, streamResult);
            
            NetworkSetConfigurationParameters parameters = new NetworkSetConfigurationParameters();
            parameters.setConfiguration(stringWriter.toString());
            networkOperations.setConfiguration(parameters);
         }
    }
    
    protected static void deleteNetwork(String networkName) {
        NetworkGetConfigurationResponse operationResponse = null ;
        boolean exist = false;
        
        try {
            operationResponse = networkManagementClient.getNetworksOperations().getConfiguration();
        } catch (IOException e) {
        } catch (ServiceException e) {
        }

        //Assert
        if (operationResponse != null)
        {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = null;
            try {
                documentBuilder = documentBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            Document responseDoc = null;
            try {
                responseDoc = documentBuilder.parse(new BOMInputStream(new ByteArrayInputStream(operationResponse.getConfiguration().getBytes())));
            } catch (NullPointerException e) {
            } catch (SAXException e) {
            } catch (IOException e) {
            }

            NodeList virtualNetworkSitelist = responseDoc.getElementsByTagNameNS("http://schemas.microsoft.com/ServiceHosting/2011/07/NetworkConfiguration", "VirtualNetworkSite");

            for (int i = 0; i < virtualNetworkSitelist.getLength(); i++) {
                if (virtualNetworkSitelist.item(i).getAttributes().getNamedItem("name").getTextContent().equals(networkName)) {
                    Node oldChild = virtualNetworkSitelist.item(i);
                    oldChild.getParentNode().removeChild(oldChild);
                    
                exist = true;
                break;
                }
            }

            if (exist) {
                DOMSource domSource = new DOMSource(responseDoc);
                StringWriter stringWriter = new StringWriter();
                StreamResult streamResult = new StreamResult(stringWriter);
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = null;
                try {
                    transformer = transformerFactory.newTransformer();
                } catch (TransformerConfigurationException e) {
                    e.printStackTrace();
                }

                try {
                    transformer.transform(domSource, streamResult);
                } catch (TransformerException e) {
                    e.printStackTrace();
                }

                NetworkSetConfigurationParameters parameters = new NetworkSetConfigurationParameters();
                parameters.setConfiguration(stringWriter.toString());
                try {
                    networkOperations.setConfiguration(parameters);
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                } catch (ServiceException e) {
                } catch (IOException e) {
                }
            }
        }
    }


    protected static String randomString(int length) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i<length; i++) {
            stringBuilder.append((char)('a' + random.nextInt(26)));
        }
        return stringBuilder.toString();
    }
}