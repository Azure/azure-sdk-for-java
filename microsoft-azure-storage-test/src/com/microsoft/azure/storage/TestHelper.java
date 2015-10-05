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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.microsoft.azure.keyvault.extensions.RsaKey;
import com.microsoft.azure.keyvault.extensions.SymmetricKey;
import com.microsoft.azure.storage.analytics.CloudAnalyticsClient;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.table.CloudTableClient;

public class TestHelper {

    private static Tenant tenant;
    private static StorageCredentialsAccountAndKey credentials;
    private static CloudStorageAccount account;

    private final static boolean enableFiddler = true;
    private final static boolean requireSecondaryEndpoint = false;

    public static CloudBlobClient createCloudBlobClient() throws StorageException {
        CloudBlobClient client = getAccount().createCloudBlobClient();
        return client;
    }

    public static CloudBlobClient createCloudBlobClient(SharedAccessAccountPolicy policy, boolean useHttps)
            throws StorageException, InvalidKeyException, URISyntaxException {
        
        CloudStorageAccount sasAccount = getAccount();
        final String token = sasAccount.generateSharedAccessSignature(policy);
        final StorageCredentials creds =
                new StorageCredentialsSharedAccessSignature(token);
        
        sasAccount = new CloudStorageAccount(
                creds, TestHelper.securePortUri(sasAccount.getBlobEndpoint(), useHttps, 'b'),
                sasAccount.getQueueEndpoint(), sasAccount.getTableEndpoint(), sasAccount.getFileEndpoint());
        return sasAccount.createCloudBlobClient();
    }

    public static CloudFileClient createCloudFileClient() throws StorageException {
        CloudFileClient client = getAccount().createCloudFileClient();
        return client;
    }

    public static CloudFileClient createCloudFileClient(SharedAccessAccountPolicy policy, boolean useHttps)
            throws StorageException, InvalidKeyException, URISyntaxException {

        CloudStorageAccount sasAccount = getAccount();
        final String token = sasAccount.generateSharedAccessSignature(policy);
        final StorageCredentials creds =
                new StorageCredentialsSharedAccessSignature(token);
        
        sasAccount = new CloudStorageAccount(
                creds, sasAccount.getBlobEndpoint(), sasAccount.getQueueEndpoint(), sasAccount.getTableEndpoint(),
                TestHelper.securePortUri(sasAccount.getFileEndpoint(), useHttps, 'f'));
        return sasAccount.createCloudFileClient();
    }


    public static CloudQueueClient createCloudQueueClient() throws StorageException {
        CloudQueueClient client = getAccount().createCloudQueueClient();
        return client;
    }

    public static CloudQueueClient createCloudQueueClient(SharedAccessAccountPolicy policy, boolean useHttps)
            throws StorageException, InvalidKeyException, URISyntaxException {

        CloudStorageAccount sasAccount = getAccount();
        final String token = sasAccount.generateSharedAccessSignature(policy);
        final StorageCredentials creds =
                new StorageCredentialsSharedAccessSignature(token);
        
        sasAccount = new CloudStorageAccount(
                creds, sasAccount.getBlobEndpoint(), TestHelper.securePortUri(sasAccount.getQueueEndpoint(), useHttps, 'q'),
                sasAccount.getTableEndpoint(), sasAccount.getFileEndpoint());
        return sasAccount.createCloudQueueClient();
    }


    public static CloudTableClient createCloudTableClient() throws StorageException {
        CloudTableClient client = getAccount().createCloudTableClient();
        return client;
    }

    public static CloudTableClient createCloudTableClient(SharedAccessAccountPolicy policy, boolean useHttps)
            throws StorageException, InvalidKeyException, URISyntaxException {

        CloudStorageAccount sasAccount = getAccount();
        final String token = sasAccount.generateSharedAccessSignature(policy);
        final StorageCredentials creds =
                new StorageCredentialsSharedAccessSignature(token);
        
        sasAccount = new CloudStorageAccount(
                creds, sasAccount.getBlobEndpoint(), sasAccount.getQueueEndpoint(),
                TestHelper.securePortUri(sasAccount.getTableEndpoint(), useHttps, 't'), sasAccount.getFileEndpoint());
        return sasAccount.createCloudTableClient();
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
    
    public static URI securePortUri(URI uri, boolean useHttps, char service) throws URISyntaxException {
        Integer port = null;
        String scheme;
        
        if (useHttps) {
            if (TestHelper.tenant != null) {
                switch(service) {
                    case 'b' :
                        port = TestHelper.tenant.getBlobHttpsPortOverride();
                        break;
    
                    case 'f' :
                        port = TestHelper.tenant.getFileHttpsPortOverride();
                        break;
                        
                    case 't' :
                        port = TestHelper.tenant.getTableHttpsPortOverride();
                        break;
                        
                    case 'q' :
                        port = TestHelper.tenant.getQueueHttpsPortOverride();
                        break;
                        
                    default :
                        fail();
                }
            }

            scheme = Constants.HTTPS;
            if (port == null) {
                port = 443;
            }
        }
        else {
            scheme = Constants.HTTP;
            port = uri.getPort();
        }
        
        return new URI(scheme, uri.getUserInfo(), uri.getHost(), port, uri.getPath(), uri.getQuery(), uri.getFragment());
    }

    public static void assertStreamsAreEqual(InputStream src, InputStream dst) throws IOException {
        dst.reset();
        src.reset();

        int next = src.read();
        while (next != -1) {
            assertEquals(next, dst.read());
            next = src.read();
        }

        next = dst.read();
        while (next != -1) {
            assertEquals(0, next);
            next = dst.read();
        }
    }

    public static void assertStreamsAreEqualAtIndex(ByteArrayInputStream src, ByteArrayInputStream dst, int srcIndex,
            int dstIndex, int length, int bufferSize) throws IOException {
        dst.reset();
        src.reset();

        dst.skip(dstIndex);
        src.skip(srcIndex);

        for (int i = 0; i < length; i++) {
            assertEquals(src.read(), dst.read());
        }
    }

    public static void assertURIsEqual(URI expected, URI actual, boolean ignoreQueryOrder) {
        if (expected == null) {
            assertEquals(null, actual);
        }

        assertEquals(expected.getScheme(), actual.getScheme());
        assertEquals(expected.getAuthority(), actual.getAuthority());
        assertEquals(expected.getPath(), actual.getPath());
        assertEquals(expected.getFragment(), actual.getFragment());

        ArrayList<String> expectedQueries = new ArrayList<String>(Arrays.asList(expected.getQuery().split("&")));
        ArrayList<String> actualQueries = new ArrayList<String>(Arrays.asList(actual.getQuery().split("&")));

        assertEquals(expectedQueries.size(), actualQueries.size());
        for (String expectedQuery : expectedQueries) {
            assertTrue(expectedQuery, actualQueries.remove(expectedQuery));
        }

        assertTrue(actualQueries.isEmpty());
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
    
    public static SymmetricKey getSymmetricKey() throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey wrapKey = keyGen.generateKey();

        return new SymmetricKey("symmKey1", wrapKey.getEncoded());
    }
    
    public static RsaKey getRSAKey() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        final KeyPair wrapKey = keyGen.generateKeyPair();

        return new RsaKey("rsaKey1", wrapKey);
    }

    public static void verifyServiceStats(ServiceStats stats) {
        assertNotNull(stats);
        if (stats.getGeoReplication().getLastSyncTime() != null) {
            assertEquals(GeoReplicationStatus.LIVE, stats.getGeoReplication().getStatus());
        }
        else {
            assertTrue(stats.getGeoReplication().getStatus() == GeoReplicationStatus.BOOTSTRAP
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
                throw StorageException.translateClientException(e);
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
                    final Node node = childNodes.item(j);
                    
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
                            tenant.setQueueServiceEndpoint(new URI(node.getTextContent()));
                        }
                        else if (name.equals("TableServiceEndpoint")) {
                            tenant.setTableServiceEndpoint(new URI(node.getTextContent()));
                        }
                        else if (name.equals("FileServiceEndpoint")) {
                            tenant.setFileServiceEndpoint(new URI(node.getTextContent()));
                        }
                        else if (name.equals("BlobServiceSecondaryEndpoint")) {
                            tenant.setBlobServiceSecondaryEndpoint(new URI(node.getTextContent()));
                        }
                        else if (name.equals("QueueServiceSecondaryEndpoint")) {
                            tenant.setQueueServiceSecondaryEndpoint(new URI(node.getTextContent()));
                        }
                        else if (name.equals("TableServiceSecondaryEndpoint")) {
                            tenant.setTableServiceSecondaryEndpoint(new URI(node.getTextContent()));
                        }
                        else if (name.equals("FileServiceSecondaryEndpoint")) {
                            tenant.setFileServiceSecondaryEndpoint(new URI(node.getTextContent()));
                        }
                        else if (name.equals("BlobHttpsPortOverride")) {
                            tenant.setBlobHttpsPortOverride(Integer.parseInt(node.getTextContent()));
                        }
                        else if (name.equals("QueueHttpsPortOverride")) {
                            tenant.setQueueHttpsPortOverride(Integer.parseInt(node.getTextContent()));
                        }
                        else if (name.equals("TableHttpsPortOverride")) {
                            tenant.setTableHttpsPortOverride(Integer.parseInt(node.getTextContent()));
                        }
                        else if (name.equals("FileHttpsPortOverride")) {
                            tenant.setFileHttpsPortOverride(Integer.parseInt(node.getTextContent()));
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
