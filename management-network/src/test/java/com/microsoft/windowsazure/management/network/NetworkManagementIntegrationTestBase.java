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

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.microsoft.windowsazure.core.pipeline.apache.ApacheConfigurationProperties;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.management.configuration.*;
import com.microsoft.windowsazure.management.network.models.NetworkGetConfigurationResponse;
import com.microsoft.windowsazure.management.network.models.NetworkSetConfigurationParameters;
import com.microsoft.windowsazure.*;

public abstract class NetworkManagementIntegrationTestBase {

    protected static NetworkManagementClient networkManagementClient;	

    protected static void createService() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();
        config.setProperty(ApacheConfigurationProperties.PROPERTY_RETRY_HANDLER, new DefaultHttpRequestRetryHandler());

        networkManagementClient = NetworkManagementService.create(config);
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
        Document responseDoc = documentBuilder.parse(new InputSource(new StringReader(operationResponse.getConfiguration())));

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
	        networkManagementClient.getNetworksOperations().setConfiguration(parameters);
        }
    }
    
    protected static Configuration createConfiguration() throws Exception {
        String baseUri = System.getenv(ManagementConfiguration.URI);
        return ManagementConfiguration.configure(
            baseUri != null ? new URI(baseUri) : null,
            System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
            System.getenv(ManagementConfiguration.KEYSTORE_PATH),
            System.getenv(ManagementConfiguration.KEYSTORE_PASSWORD),
            KeyStoreType.fromString(System.getenv(ManagementConfiguration.KEYSTORE_TYPE))
        );   
    }
}