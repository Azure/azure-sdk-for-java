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
package com.microsoft.windowsazure.storage;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.microsoft.windowsazure.storage.blob.CloudBlobClient;
import com.microsoft.windowsazure.storage.queue.CloudQueueClient;
import com.microsoft.windowsazure.storage.table.CloudTableClient;

public class TestBase {

    protected static Tenant tenant;
    protected static StorageCredentialsAccountAndKey credentials;
    protected static CloudStorageAccount account;

    private final static AuthenticationScheme defaultAuthenticationScheme = AuthenticationScheme.SHAREDKEYFULL;
    private final static boolean enableFiddler = true;
    private static final URL testConfig = TestBase.class.getResource("TestConfigurations.xml");

    @BeforeClass
    public static void testBaseClassSetup() throws URISyntaxException, SAXException, IOException,
            ParserConfigurationException, DOMException, InvalidKeyException {
        // Only do this the first time TestBase is called as storage account is static
        if (account == null) {
            String cloudAccount = System.getenv("storageConnection");
            // if the appropriate environment var is set, use that, otherwise use the configurations file
            if (cloudAccount != null) {
                account = CloudStorageAccount.parse(cloudAccount);
            }
            else {
                if (enableFiddler)
                    TestHelper.enableFiddler();

                tenant = readTestConfigsFromXml();
                tenant.assertSecondaryEndpoint();
                credentials = new StorageCredentialsAccountAndKey(tenant.getAccountName(), tenant.getAccountKey());
                account = new CloudStorageAccount(credentials, new StorageUri(tenant.getBlobServiceEndpoint(),
                        tenant.getBlobServiceSecondaryEndpoint()), new StorageUri(tenant.getQueueServiceEndpoint(),
                        tenant.getQueueServiceSecondaryEndpoint()), new StorageUri(tenant.getTableServiceEndpoint(),
                        tenant.getTableServiceSecondaryEndpoint()));
            }
        }
    }

    private static Tenant readTestConfigsFromXml() throws ParserConfigurationException, SAXException, IOException,
            DOMException, URISyntaxException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document testConfigs = documentBuilder.parse(new File(testConfig.getPath()));

        String targetTenant = testConfigs.getElementsByTagName("TargetTestTenant").item(0).getTextContent();
        Tenant tenant = new Tenant();

        NodeList tenantNodes = testConfigs.getElementsByTagName("TenantName");
        for (int i = 0; i < tenantNodes.getLength(); i++) {
            if (tenantNodes.item(i).getTextContent().equals(targetTenant)) {
                tenant.setTenantName(testConfigs.getElementsByTagName("TenantName").item(i).getTextContent());
                tenant.setAccountName(testConfigs.getElementsByTagName("AccountName").item(i).getTextContent());
                tenant.setAccountKey(testConfigs.getElementsByTagName("AccountKey").item(i).getTextContent());
                tenant.setBlobServiceEndpoint(new URI(testConfigs.getElementsByTagName("BlobServiceEndpoint").item(i)
                        .getTextContent()));
                tenant.setQueueServiceEndpoint(new URI(testConfigs.getElementsByTagName("QueueServiceEndpoint").item(i)
                        .getTextContent()));
                tenant.setTableServiceEndpoint(new URI(testConfigs.getElementsByTagName("TableServiceEndpoint").item(i)
                        .getTextContent()));
                tenant.setBlobServiceSecondaryEndpoint(new URI(testConfigs
                        .getElementsByTagName("BlobServiceSecondaryEndpoint").item(i).getTextContent()));
                tenant.setQueueServiceSecondaryEndpoint(new URI(testConfigs
                        .getElementsByTagName("QueueServiceSecondaryEndpoint").item(i).getTextContent()));
                tenant.setTableServiceSecondaryEndpoint(new URI(testConfigs
                        .getElementsByTagName("TableServiceSecondaryEndpoint").item(i).getTextContent()));
                break;
            }
        }

        return tenant;
    }

    public static CloudBlobClient createCloudBlobClient() {
        CloudBlobClient client = account.createCloudBlobClient();
        client.setAuthenticationScheme(defaultAuthenticationScheme);
        return client;
    }

    public static CloudQueueClient createCloudQueueClient() {
        CloudQueueClient client = account.createCloudQueueClient();
        client.setAuthenticationScheme(defaultAuthenticationScheme);
        return client;
    }

    public static CloudTableClient createCloudTableClient() {
        CloudTableClient client = account.createCloudTableClient();
        client.setAuthenticationScheme(defaultAuthenticationScheme);
        return client;
    }
}
