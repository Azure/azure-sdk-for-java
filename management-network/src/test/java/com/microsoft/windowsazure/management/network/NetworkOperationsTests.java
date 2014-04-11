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

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.management.network.models.*;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class NetworkOperationsTests extends NetworkManagementIntegrationTestBase {
    @BeforeClass
    public static void setup() throws Exception {
        createService();
    }

    @Test
    public void getConfiguration() throws Exception {
        //act
        NetworkGetConfigurationResponse operationResponse = networkManagementClient.getNetworksOperations().getConfiguration();

        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
        Assert.assertNotNull(operationResponse.getConfiguration());
    }

    @Test
    public void setConfiguration() throws Exception {
        //act
        NetworkGetConfigurationResponse operationResponse = networkManagementClient.getNetworksOperations().getConfiguration();

        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
        Assert.assertNotNull(operationResponse.getConfiguration());

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document responseDoc = documentBuilder.parse(new InputSource(new ByteArrayInputStream(operationResponse.getConfiguration().getBytes("UTF-8"))));

        DOMSource domSource = new DOMSource(responseDoc);
        StringWriter stringWriter = new StringWriter();
        StreamResult streamResult = new StreamResult(stringWriter);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(domSource, streamResult);

        NetworkSetConfigurationParameters parameters = new NetworkSetConfigurationParameters();
        parameters.setConfiguration(stringWriter.toString());
        OperationResponse response = networkManagementClient.getNetworksOperations().setConfiguration(parameters);

        //Assert
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertNotNull(response.getRequestId());
    }

    @Test
    public void list() throws Exception {
        //act
        NetworkListResponse operationResponse = networkManagementClient.getNetworksOperations().list();

        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
        Assert.assertNotNull(operationResponse.getVirtualNetworkSites());
    }
}