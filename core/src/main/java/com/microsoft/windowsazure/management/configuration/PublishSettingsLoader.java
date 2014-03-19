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

import java.io.*;
import java.lang.IllegalArgumentException;
import java.lang.reflect.Field;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.utils.KeyStoreType;

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
public final class PublishSettingsLoader {

    /**
     * Create a service management configuration using the given publishsettings
     * file and subscription ID.
     * <p>
     * <b>Please note:</b>
     * <ul>
     * <li>Will use the first PublishProfile present in the file.</li>
     * <li>The unprotected keystore file <code>keystore.out</code> will be left
     * in the working directory (contains the management certificate).</li>
     * </ul>
     * </p>
     * 
     * @param publishSettingsFile
     *            publish settings file with a valid certificate obtained from
     *            the Windows Azure website
     * @param subscriptionId
     *            subscription ID
     * @return a valid service management configuration
     * @throws IOException
     *             if any error occurs when handling the specified file or the
     *             keystore
     * @throws IllegalArgumentException
     *             if the file is not of the expected format
     */
    public static Configuration createManagementConfiguration(
            String publishSettingsFile, String subscriptionId)
            throws IOException {
        File file = new File(publishSettingsFile);
        // By default creating keystore outfile in user home
        String outStore = System.getProperty("user.home") + File.separator
                + ".azure" + File.separator + subscriptionId + ".out";
        String certificate = null;
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList ndPublishProfile = doc
                    .getElementsByTagName("PublishProfile");
            Element ppElement = (Element) ndPublishProfile.item(0);
            if (ppElement.hasAttribute("SchemaVersion")
                    && ppElement.getAttribute("SchemaVersion").equals("2.0")) {
                NodeList subs = ppElement.getElementsByTagName("Subscription");
                for (int i = 0; i < subs.getLength(); i++) {
                    Element subscription = (Element) subs.item(i);
                    String id = subscription.getAttribute("Id");
                    if (id.equals(subscriptionId)) {
                        certificate = subscription
                                .getAttribute("ManagementCertificate");
                        break;
                    }
                }
            } else {
                certificate = ppElement.getAttribute("ManagementCertificate");
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

        KeyStore store = null;
        try {
            if (Float.valueOf(System.getProperty("java.specification.version")) < 1.7) {
                // Use Bouncy Castle Provider for java versions less than 1.7
                store = getBCProviderKeyStore();
            } else {
                store = KeyStore.getInstance("PKCS12");
            }
            store.load(null, "".toCharArray());
            InputStream sslInputStream = new ByteArrayInputStream(
                    Base64.decodeBase64(certificate));

            store.load(sslInputStream, "".toCharArray());

            // create directories if doesnot exists
            File outStoreFile = new File(outStore);
            if (!outStoreFile.getParentFile().exists()) {
                outStoreFile.getParentFile().mkdirs();
            }
            OutputStream out = new FileOutputStream(outStore);
            store.store(out, "".toCharArray());
            out.close();
        } catch (KeyStoreException e) {
            throw new IllegalArgumentException(
                    "could not create keystore from publishsettings file", e);
        } catch (CertificateException e) {
            throw new IllegalArgumentException(
                    "could not create keystore from publishsettings file", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(
                    "could not create keystore from publishsettings file", e);
        }

        return ManagementConfiguration.configure(subscriptionId, outStore, "",
                KeyStoreType.pkcs12);
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