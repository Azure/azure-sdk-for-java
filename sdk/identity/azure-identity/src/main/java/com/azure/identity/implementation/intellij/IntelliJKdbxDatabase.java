// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.intellij;

import com.azure.identity.CredentialUnavailableException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

public class IntelliJKdbxDatabase {

    private static final XPath XPATH = XPathFactory.newInstance().newXPath();

    private static final String STANDARD_PROPERTY_NAME_USER_NAME = "UserName";
    private static final String STANDARD_PROPERTY_NAME_PASSWORD = "Password";
    private static final String STANDARD_PROPERTY_NAME_URL = "URL";
    private static final String STANDARD_PROPERTY_NAME_TITLE = "Title";
    private static final String STANDARD_PROPERTY_NAME_NOTES = "Notes";

    private Element rootElement;

    IntelliJKdbxDatabase(Document document, Element rootElement) {
        this.rootElement = rootElement;
    }

    public static IntelliJKdbxDatabase parse(InputStream encryptedDatabaseStream, String databasePassword) throws IOException {

        byte[] key = getDatabaseKey(databasePassword);

        IntelliJKdbxMetadata kdbxMetadata = new IntelliJKdbxMetadata();
        InputStream decryptedInputStream = decryptInputStream(key, kdbxMetadata, encryptedDatabaseStream);

        Salsa20 salsa20 = IntelliJCryptoUtil.createSalsa20CryptoEngine(kdbxMetadata.getEncryptionKey());
        Document document = loadDatabase(decryptedInputStream, salsa20);

        Element rootElement;
        try {
            rootElement = (Element) XPATH.evaluate("/KeePassFile/Root/Group", document, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new CredentialUnavailableException("Error loading the database", e);
        }
        return new IntelliJKdbxDatabase(document, rootElement);
    }

    private static byte[] getDatabaseKey(String databasePassword) {
        MessageDigest md = IntelliJCryptoUtil.getMessageDigestSHA256();
        byte[] digest = md.digest(databasePassword.getBytes(StandardCharsets.UTF_8));
        return md.digest(digest);
    }


    private static InputStream decryptInputStream(byte[] key, IntelliJKdbxMetadata kdbxMetadata, InputStream inputStream)
        throws IOException {
        parseDatabaseMetadata(kdbxMetadata, inputStream);
        InputStream decryptedInputStream = IntelliJCryptoUtil.createDecryptedStream(key, inputStream, kdbxMetadata);
        validateInitialBytes(kdbxMetadata, decryptedInputStream);
        HashedBlockInputStream blockInputStream = new HashedBlockInputStream(decryptedInputStream, true);
        return kdbxMetadata.getDatabaseCompressionFlags().equals(IntelliJKdbxMetadata.DatabaseCompressionFlags.NONE)
            ? blockInputStream : new GZIPInputStream(blockInputStream);
    }

    private static IntelliJKdbxMetadata parseDatabaseMetadata(IntelliJKdbxMetadata kdbxMetadata, InputStream inputStream) throws IOException {
        MessageDigest digest = IntelliJCryptoUtil.getMessageDigestSHA256();
        DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest);
        LittleEndianDataInputStream littleEndianDataInputStream = new LittleEndianDataInputStream(digestInputStream);

        int sig1 = littleEndianDataInputStream.readInt();
        int sig2 = littleEndianDataInputStream.readInt();
        if (sig1 != -1700603645 || sig2 != -1253311641) {
            throw new IllegalStateException("Magic number did not match");
        } else if ((littleEndianDataInputStream.readInt() & -65536) > 196608) {
            throw new IllegalStateException("File version did not match");
        } else {
            byte headerType;
            while ((headerType = littleEndianDataInputStream.readByte()) != 0) {
                switch (headerType) {
                    case 1:
                        readByteArray(littleEndianDataInputStream);
                        break;
                    case 2:
                        kdbxMetadata.setCipherUuid(readByteArray(littleEndianDataInputStream));
                        break;
                    case 3:
                        kdbxMetadata.setDatabaseCompressionFlags(readInt(littleEndianDataInputStream));
                        break;
                    case 4:
                        kdbxMetadata.setBaseSeed(readByteArray(littleEndianDataInputStream));
                        break;
                    case 5:
                        kdbxMetadata.setTransformSeed(readByteArray(littleEndianDataInputStream));
                        break;
                    case 6:
                        kdbxMetadata.setTransformRounds(readLong(littleEndianDataInputStream));
                        break;
                    case 7:
                        kdbxMetadata.setEncryptionIv(readByteArray(littleEndianDataInputStream));
                        break;
                    case 8:
                        kdbxMetadata.setEncryptionKey(readByteArray(littleEndianDataInputStream));
                        break;
                    case 9:
                        kdbxMetadata.setInitBytes(readByteArray(littleEndianDataInputStream));
                        break;
                    case 10:
                        kdbxMetadata.setEncryptionAlgorithm(readInt(littleEndianDataInputStream));
                        break;
                    default:
                        throw new IllegalStateException("Unknown File Header");
                }
            }

            readByteArray(littleEndianDataInputStream);
            kdbxMetadata.setHeaderHash(digest.digest());
            return kdbxMetadata;
        }
    }

    private static byte[] readByteArray(LittleEndianDataInputStream ledis) throws IOException {
        short fieldLength = ledis.readShort();
        byte[] value = new byte[fieldLength];
        ledis.readFully(value);
        return value;
    }

    private static void validateInitialBytes(IntelliJKdbxMetadata kdbxMetadata, InputStream decryptedInputStream) throws IOException {
        LittleEndianDataInputStream ledis = new LittleEndianDataInputStream(decryptedInputStream);
        byte[] initBytes = new byte[32];
        ledis.readFully(initBytes);
        if (!Arrays.equals(initBytes, kdbxMetadata.getInitBytes())) {
            throw new IllegalStateException("Inconsistent stream start bytes. This usually means the credentials were wrong.");
        }
    }

    private static int readInt(LittleEndianDataInputStream ledis) throws IOException {
        short length = ledis.readShort();
        if (length != 4) {
            throw new IllegalStateException("Int required but length was " + length);
        } else {
            return ledis.readInt();
        }
    }

    private static long readLong(LittleEndianDataInputStream ledis) throws IOException {
        short length = ledis.readShort();
        if (length != 8) {
            throw new IllegalStateException("Long required but length was " + length);
        } else {
            return ledis.readLong();
        }
    }

    public static Document loadDatabase(InputStream inputStream, Salsa20 salsa20Engine) throws IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);
            NodeList protectedContent = (NodeList) XPATH.evaluate("//*[@Protected='True']", doc, XPathConstants.NODESET);

            for (int i = 0; i < protectedContent.getLength(); ++i) {
                Element element = (Element) protectedContent.item(i);
                Element res = getElement(".", element, false);
                String base64 = res == null ? null : res.getTextContent();
                byte[] encrypted = Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8));
                String decrypted = new String(IntelliJCryptoUtil.decrypt(encrypted, salsa20Engine), "UTF-8");
                setElementContent(".", element, decrypted);
                element.removeAttribute("Protected");
            }

            return doc;
        } catch (ParserConfigurationException var10) {
            throw new IllegalStateException("Instantiating Document Builder", var10);
        } catch (SAXException var11) {
            throw new IllegalStateException("Parsing exception", var11);
        } catch (XPathExpressionException var12) {
            throw new IllegalStateException("XPath Exception", var12);
        }
    }

    private static Element getElement(String elementPath, Element parentElement, boolean create) {
        try {
            Element output = (Element) XPATH.evaluate(elementPath, parentElement, XPathConstants.NODE);
            if (output == null && create) {
                output = buildHierarchialPath(elementPath, parentElement);
            }

            return output;
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private static Element buildHierarchialPath(String elementPath, Element startElement) {
        Element currentElement = startElement;
        String[] pathTokens = elementPath.split("/");

        for (int i = 0; i < pathTokens.length; ++i) {
            String elementName = pathTokens[i];

            try {
                Element nextElement = (Element) XPATH.evaluate(elementName, currentElement, XPathConstants.NODE);
                if (nextElement == null) {
                    nextElement = (Element) currentElement.appendChild(currentElement.getOwnerDocument()
                        .createElement(elementName));
                }

                currentElement = nextElement;
            } catch (XPathExpressionException e) {
                throw new RuntimeException(e);
            }
        }

        return currentElement;
    }

    static Element setElementContent(String elementPath, Element parentElement, String value) {
        Element result = getElement(elementPath, parentElement, true);
        result.setTextContent(value);
        return result;
    }

    public static boolean match(Element baseEntry, String text) {
        String title = getProperty(baseEntry, STANDARD_PROPERTY_NAME_TITLE);
        String notes = getProperty(baseEntry, STANDARD_PROPERTY_NAME_NOTES);
        String url = getProperty(baseEntry, STANDARD_PROPERTY_NAME_URL);
        String username = getProperty(baseEntry, STANDARD_PROPERTY_NAME_USER_NAME);

        return matchString(title, text) || matchString(notes, text) || matchString(url, text) || matchString(username, text);
    }

    public static boolean matchString(String property, String toMatch) {
        return property != null && property.toLowerCase(Locale.getDefault())
            .contains(toMatch.toLowerCase(Locale.getDefault()));
    }

    private String getDatabaseEntryValue(Element dbRootGroup, String toMatch) {

        for (Element entry: getElements("Entry", dbRootGroup)) {
            if (match(entry, toMatch)) {
                return getProperty(entry, STANDARD_PROPERTY_NAME_PASSWORD);
            }
        }
        for (Element group : getGroups(dbRootGroup)) {
            getDatabaseEntryValue(group, toMatch);
        }

        return null;
    }

    public String getDatabaseEntryValue(String toMatch) {
        return getDatabaseEntryValue(rootElement, toMatch);
    }


    static String getElementContent(String elementPath, Element parentElement) {
        Element result = getElement(elementPath, parentElement, false);
        return result == null ? null : result.getTextContent();
    }

    public static String getProperty(Element element, String name) {
        Element property = getElement(String.format("String[Key/text()='%s']", name), element, false);
        return property == null ? null : getElementContent("Value", property);
    }

    static List<Element> getElements(String elementPath, Element parentElement) {
        try {
            NodeList nodes = (NodeList) XPATH.evaluate(elementPath, parentElement, XPathConstants.NODESET);
            ArrayList<Element> result = new ArrayList<>(nodes.getLength());

            for (int i = 0; i < nodes.getLength(); ++i) {
                result.add((Element) nodes.item(i));
            }

            return result;
        } catch (XPathExpressionException var5) {
            throw new IllegalStateException(var5);
        }
    }

    public static List<Element> getGroups(Element rootGroup) {
        List<Element> elements = getElements("Group", rootGroup);
        return elements;
    }
}
