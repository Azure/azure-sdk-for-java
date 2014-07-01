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
package com.microsoft.azure.storage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.microsoft.azure.storage.analytics.CloudAnalyticsClient;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.table.CloudTableClient;

public class TestHelper {

    private static Tenant tenant;
    private static StorageCredentialsAccountAndKey credentials;
    private static CloudStorageAccount account;

    private final static AuthenticationScheme defaultAuthenticationScheme = AuthenticationScheme.SHAREDKEYFULL;
    private final static boolean enableFiddler = true;
    private final static boolean requireSecondaryEndpoint = false;

    public static CloudBlobClient createCloudBlobClient() throws StorageException {
        CloudBlobClient client = getAccount().createCloudBlobClient();
        client.setAuthenticationScheme(defaultAuthenticationScheme);
        return client;
    }

    public static CloudFileClient createCloudFileClient() throws StorageException {
        CloudFileClient client = getAccount().createCloudFileClient();
        client.setAuthenticationScheme(defaultAuthenticationScheme);
        return client;
    }

    public static CloudQueueClient createCloudQueueClient() throws StorageException {
        CloudQueueClient client = getAccount().createCloudQueueClient();
        client.setAuthenticationScheme(defaultAuthenticationScheme);
        return client;
    }

    public static CloudTableClient createCloudTableClient() throws StorageException {
        CloudTableClient client = getAccount().createCloudTableClient();
        client.setAuthenticationScheme(defaultAuthenticationScheme);
        return client;
    }

    public static CloudAnalyticsClient createCloudAnalyticsClient() throws StorageException {
        CloudAnalyticsClient client = getAccount().createCloudAnalyticsClient();
        return client;
    }

    protected static void enableFiddler() {
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "8888");
    }

    public static byte[] getRandomBuffer(int length) {
        final Random randGenerator = new Random();
        final byte[] buff = new byte[length];
        randGenerator.nextBytes(buff);
        return buff;
    }

    public static ByteArrayInputStream getRandomDataStream(int length) {
        return new ByteArrayInputStream(getRandomBuffer(length));
    }

    public static void assertStreamsAreEqual(ByteArrayInputStream src, ByteArrayInputStream dst) {
        dst.reset();
        src.reset();
        Assert.assertEquals(src.available(), dst.available());

        while (src.available() > 0) {
            Assert.assertEquals(src.read(), dst.read());
        }

        Assert.assertFalse(dst.available() > 0);
    }

    public static void assertStreamsAreEqualAtIndex(ByteArrayInputStream src, ByteArrayInputStream dst, int srcIndex,
            int dstIndex, int length, int bufferSize) throws IOException {
        dst.reset();
        src.reset();

        dst.skip(dstIndex);
        src.skip(srcIndex);
        byte[] srcBuffer = new byte[bufferSize];
        byte[] destBuffer = new byte[bufferSize];
        src.read(srcBuffer);
        dst.read(destBuffer);

        for (int i = 0; i < length; i++) {
            Assert.assertEquals(src.read(), dst.read());
        }
    }

    public static URI defiddler(URI uri) throws URISyntaxException {
        String fiddlerString = "ipv4.fiddler";
        String replacementString = "127.0.0.1";

        String uriString = uri.toString();
        if (uriString.contains(fiddlerString)) {
            return new URI(uriString.replace(fiddlerString, replacementString));
        }
        else {
            return uri;
        }
    }

    public static void verifyServiceStats(ServiceStats stats) {
        Assert.assertNotNull(stats);
        if (stats.getGeoReplication().getLastSyncTime() != null) {
            Assert.assertEquals(GeoReplicationStatus.LIVE, stats.getGeoReplication().getStatus());
        }
        else {
            Assert.assertTrue(stats.getGeoReplication().getStatus() == GeoReplicationStatus.BOOTSTRAP
                    || stats.getGeoReplication().getStatus() == GeoReplicationStatus.UNAVAILABLE);
        }
    }

    private static CloudStorageAccount getAccount() throws StorageException {
        // Only do this the first time TestBase is called as storage account is static
        if (account == null) {
            //enable fiddler
            if (enableFiddler)
                enableFiddler();

            // try to get the environment variable with the connection string
            String cloudAccount;
            try {
                cloudAccount = System.getenv("storageConnection");
            }
            catch (SecurityException e) {
                cloudAccount = null;
            }

            // try to get the environment variable with the test configuration file path
            String accountConfig;
            try {
                accountConfig = System.getenv("storageTestConfiguration");
            }
            catch (SecurityException e) {
                accountConfig = null;
            }

            // if storageConnection is set, use that as an account string
            // if storageTestConfiguration is set, use that as a path to the configurations file
            // if neither are set, use the local configurations file at TestConfigurations.xml
            try {
                if (cloudAccount != null) {
                    account = CloudStorageAccount.parse(cloudAccount);
                }
                else if (accountConfig != null) {
                    tenant = readTestConfigsFromXml(new File(accountConfig));
                    setAccountAndCredentials();
                }
                else {
                    URL localTestConfig = TestHelper.class.getClassLoader().getResource("TestConfigurations.xml");
                    tenant = readTestConfigsFromXml(new File(localTestConfig.getPath()));
                    setAccountAndCredentials();
                }
            }
            catch (Exception e) {
                throw StorageException.translateException(null, e, null);
            }
        }
        return account;
    }

    private static void setAccountAndCredentials() {
        if (requireSecondaryEndpoint)
            tenant.assertSecondaryEndpoint();
        credentials = new StorageCredentialsAccountAndKey(tenant.getAccountName(), tenant.getAccountKey());
        account = new CloudStorageAccount(credentials, new StorageUri(tenant.getBlobServiceEndpoint(),
                tenant.getBlobServiceSecondaryEndpoint()), new StorageUri(tenant.getQueueServiceEndpoint(),
                tenant.getQueueServiceSecondaryEndpoint()), new StorageUri(tenant.getTableServiceEndpoint(),
                tenant.getTableServiceSecondaryEndpoint()), new StorageUri(tenant.getFileServiceEndpoint(),
                tenant.getFileServiceSecondaryEndpoint()));
    }

    private static Tenant readTestConfigsFromXml(File testConfigurations) throws ParserConfigurationException,
            SAXException, IOException, DOMException, URISyntaxException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document testConfigs = documentBuilder.parse(testConfigurations);
        testConfigs.getDocumentElement().normalize();

        Node targetTenantNode = testConfigs.getElementsByTagName("TargetTestTenant").item(0);
        String targetTenant = null;
        if (targetTenantNode != null) {
            targetTenant = testConfigs.getElementsByTagName("TargetTestTenant").item(0).getTextContent();
        }
        else {
            throw new IllegalArgumentException("No TargetTestTenant specified.");
        }

        Tenant tenant = null;
        final NodeList tenantNodes = testConfigs.getElementsByTagName("TenantName");
        for (int i = 0; i < tenantNodes.getLength(); i++) {
            if (tenantNodes.item(i).getTextContent().equals(targetTenant)) {
                tenant = new Tenant();
                Node parent = tenantNodes.item(i).getParentNode();
                final NodeList childNodes = parent.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node node = childNodes.item(j);
                    if (node.getNodeType() != Node.ELEMENT_NODE) {
                        // do nothing
                    }
                    else {
                        final String name = node.getNodeName();
                        if (name.equals("TenantName")) {
                            tenant.setTenantName(node.getTextContent());
                        }
                        else if (name.equals("TenantType")) {
                            // do nothing, we don't track this field
                        }
                        else if (name.equals("AccountName")) {
                            tenant.setAccountName(node.getTextContent());
                        }
                        else if (name.equals("AccountKey")) {
                            tenant.setAccountKey(node.getTextContent());
                        }
                        else if (name.equals("BlobServiceEndpoint")) {
                            tenant.setBlobServiceEndpoint(new URI(node.getTextContent()));
                        }
                        else if (name.equals("QueueServiceEndpoint")) {
                            tenant.setQueueServiceEndpoint(new URI(node.getTextContent()));;
                        }
                        else if (name.equals("TableServiceEndpoint")) {
                            tenant.setTableServiceEndpoint(new URI(node.getTextContent()));;
                        }
                        else if (name.equals("FileServiceEndpoint")) {
                            tenant.setFileServiceEndpoint(new URI(node.getTextContent()));;
                        }
                        else if (name.equals("BlobServiceSecondaryEndpoint")) {
                            tenant.setBlobServiceSecondaryEndpoint(new URI(node.getTextContent()));;
                        }
                        else if (name.equals("QueueServiceSecondaryEndpoint")) {
                            tenant.setQueueServiceSecondaryEndpoint(new URI(node.getTextContent()));;
                        }
                        else if (name.equals("TableServiceSecondaryEndpoint")) {
                            tenant.setTableServiceSecondaryEndpoint(new URI(node.getTextContent()));;
                        }
                        else if (name.equals("FileServiceSecondaryEndpoint")) {
                            tenant.setFileServiceSecondaryEndpoint(new URI(node.getTextContent()));;
                        }
                        else {
                            throw new IllegalArgumentException(String.format(
                                    "Invalid child of TenantConfiguration with name: %s", name));
                        }
                    }
                }
            }
        }

        if (tenant == null) {
            throw new IllegalArgumentException("TargetTestTenant specified did not exist in TenantConfigurations.");
        }
        return tenant;
    }
}
