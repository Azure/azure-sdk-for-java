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
package com.microsoft.windowsazure.management.configuration;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.utils.Base64;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;

/**
 * Loading a publish settings file to create a service management configuration.
 * Supports both schema version 1.0 (deprecated) and 2.0. To get different
 * schema versions, use the 'SchemaVersion' query parameter when downloading the
 * file:
 * <ul>
 * <li>https://manage.windowsazure.com/publishsettings/Index?client=vs&
 * SchemaVersion=1.0</li>
 * <li>https://manage.windowsazure.com/publishsettings/Index?client=vs&
 * SchemaVersion=2.0</li>
 * </ul>
 *
 */
public abstract class PublishSettingsLoader {

    /**
     * Create a service management configuration using specified publish settings
     * file and subscription ID.
     * <p>
     * <b>Please note:</b>
     * <ul>
     * <li>Will use the first PublishProfile present in the file.</li>
     * <li>An unprotected keystore file <code>keystore.out</code> will be left
     * in the working directory containing the management certificate.</li>
     * </ul>
     *
     * @param publishSettingsFileName
     *            The name of the publish settings file with a valid certificate obtained from
     *            Microsoft Azure portal.
     * @param subscriptionId
     *            subscription ID
     * @return a management configuration instance
     * @throws IOException
     *             if any error occurs when handling the specified file or the
     *             keystore
     * @throws IllegalArgumentException
     *             if the file is not of the expected format
     */
    public static Configuration createManagementConfiguration(
            String publishSettingsFileName, String subscriptionId)
            throws IOException {
        if (publishSettingsFileName == null) {
            throw new IllegalArgumentException("The publish settings file cannot be null.");
        }
        
        if (subscriptionId == null) {
            throw new IllegalArgumentException("The subscription ID cannot be null.");
        }
        
        if (publishSettingsFileName.isEmpty()) {
            throw new IllegalArgumentException("The publish settings file cannot be empty.");
        }
        
        if (subscriptionId.isEmpty()) {
            throw new IllegalArgumentException("The subscription ID cannot be empty.");
        }
        
        File publishSettingsFile = new File(publishSettingsFileName);
        // By default creating keystore output file in user home
        String outputKeyStore = System.getProperty("user.home") + File.separator
                + ".azure" + File.separator + subscriptionId + ".out";
        URI managementUrl = createCertficateFromPublishSettingsFile(publishSettingsFile, subscriptionId, outputKeyStore);
        
        // create new configuration object
        Configuration configuration = Configuration.load();
        return ManagementConfiguration.configure(null, configuration, managementUrl, subscriptionId, outputKeyStore, "",
                KeyStoreType.pkcs12);
    }

    private static KeyStore createKeyStoreFromCertifcate(String certificate, String keyStoreFileName) throws IOException {
        KeyStore keyStore = null;
        try {
            if (Float.valueOf(System.getProperty("java.specification.version")) < 1.7) {
                // Use Bouncy Castle Provider for java versions less than 1.7
                keyStore = getBCProviderKeyStore();
            } else {
                keyStore = KeyStore.getInstance("PKCS12");
            }
            
            keyStore.load(null, "".toCharArray());
            
            InputStream sslInputStream = new ByteArrayInputStream(
                    Base64.decode(certificate));

            keyStore.load(sslInputStream, "".toCharArray());

            // create directories if does not exists
            File outStoreFile = new File(keyStoreFileName);
            if (!outStoreFile.getParentFile().exists()) {
                outStoreFile.getParentFile().mkdirs();
            }

            OutputStream outputStream;
            outputStream = new FileOutputStream(keyStoreFileName);
            keyStore.store(outputStream, "".toCharArray());
            outputStream.close();
            
        } catch (KeyStoreException e) {
            throw new IllegalArgumentException(
                    "Cannot create keystore from the publish settings file", e);
        } catch (CertificateException e) {
            throw new IllegalArgumentException(
                    "Cannot create keystore from the publish settings file", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(
                    "Cannot create keystore from the publish settings file", e);
        }

        return keyStore;
    }

    private static URI createCertficateFromPublishSettingsFile(
            File publishSettingsFile, String subscriptionId, String outputKeyStore) throws IOException {
        String certificate = null;
        URI managementUri = null;
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            Document document = documentBuilder.parse(publishSettingsFile);
            document.getDocumentElement().normalize();
            NodeList publishProfileNodeList = document
                    .getElementsByTagName("PublishProfile");
            Element publishProfileElement = (Element) publishProfileNodeList.item(0);
            if (publishProfileElement.hasAttribute("SchemaVersion")
                    && publishProfileElement.getAttribute("SchemaVersion").equals("2.0")) {
                NodeList subscriptionNodeList = publishProfileElement.getElementsByTagName("Subscription");
                for (int i = 0; i < subscriptionNodeList.getLength(); i++) {
                    Element subscription = (Element) subscriptionNodeList.item(i);
                    String id = subscription.getAttribute("Id");
                    if (id.equals(subscriptionId)) {
                        certificate = subscription.getAttribute("ManagementCertificate");
                        String serviceManagementUrl = subscription.getAttribute("ServiceManagementUrl");
                        try {
                            managementUri = new URI(serviceManagementUrl);
                        } catch (URISyntaxException e) {
                            throw new IllegalArgumentException("The syntax of the Url in the publish settings file is incorrect.", e);
                        }
                        break;
                    }
                }
            } else {
                certificate = publishProfileElement.getAttribute("ManagementCertificate");
                String url = publishProfileElement.getAttribute("Url");
                try {
                    managementUri = new URI(url);
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("The syntax of the Url in the publish settings file is incorrect.", e);
                }
            }
        } catch (ParserConfigurationException e) {
            throw new IllegalArgumentException(
                    "could not parse publishsettings file", e);
        } catch (SAXException e) {
            throw new IllegalArgumentException(
                    "could not parse publishsettings file", e);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(
                    "could not parse publishsettings file", e);
        }
        
        createKeyStoreFromCertifcate(certificate, outputKeyStore);
        return managementUri;
    }

    /**
     * Sun JCE provider cannot open password less pfx files , refer to
     * discussion @ https://community.oracle.com/thread/2334304
     * 
     * To read password less pfx files in java versions less than 1.7 need to
     * use BouncyCastle's JCE provider
     */
    private static KeyStore getBCProviderKeyStore() {
        KeyStore keyStore = null;
        try {
            // Loading Bouncy castle classes dynamically so that BC dependency
            // is only for java 1.6 clients
            Class<?> providerClass = Class
                    .forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
            Security.addProvider((Provider) providerClass.newInstance());

            Field field = providerClass.getField("PROVIDER_NAME");
            keyStore = KeyStore.getInstance("PKCS12", field.get(null)
                    .toString());
        } catch (Exception e) {
            // Using catch all exception class to avoid repeated code in
            // different catch blocks
            throw new RuntimeException(
                "Could not create keystore from publishsettings file."
                + "Make sure java versions less than 1.7 has bouncycastle jar in classpath",
                e);
        }
        return keyStore;
    }

}