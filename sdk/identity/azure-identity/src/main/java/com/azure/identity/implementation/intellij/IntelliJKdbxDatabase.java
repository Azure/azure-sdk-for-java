package com.azure.identity.implementation.intellij;

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
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

public class IntelliJKdbxDatabase {

    private static XPath xpath = XPathFactory.newInstance().newXPath();
    private Document document;

    IntelliJKdbxDatabase(Document document) {
        this.document = document;
    }

    public static IntelliJKdbxDatabase parse(InputStream encryptedDatabaseStream, String databasePassword) throws IOException {

        byte[] key = getDatabaseKey(databasePassword);

        IntelliJKdbxMetadata kdbxMetadata = new IntelliJKdbxMetadata();
        InputStream decryptedInputStream = decryptInputStream(key, kdbxMetadata, encryptedDatabaseStream);
        SecureRandom secureRandom = new SecureRandom();
        byte[] protectedStreamKey = secureRandom.generateSeed(32);

        Salsa20 salsa20 = IntelliJCryptoUtil.createSalsa20(protectedStreamKey);
        Document document = loadDatabase(decryptedInputStream, salsa20);

        return new IntelliJKdbxDatabase(document);
    }

    private static byte[] getDatabaseKey(String databasePassword) {
        MessageDigest md = IntelliJCryptoUtil.getMessageDigestInstance();
        byte[] digest = md.digest(databasePassword.getBytes());
        return md.digest(digest);
    }


    static InputStream decryptInputStream(byte[] key, IntelliJKdbxMetadata kdbxMetadata, InputStream inputStream) throws IOException {
        parseDatabaseMetadata(kdbxMetadata, inputStream);
        InputStream decryptedInputStream = kdbxMetadata.createDecryptedStream(key, inputStream);
        validateInitialBytes(kdbxMetadata, decryptedInputStream);
        HashedBlockInputStream blockInputStream = new HashedBlockInputStream(decryptedInputStream, true);
        return (InputStream)(kdbxMetadata.getDatabaseCompressionFlags().equals(IntelliJKdbxMetadata.DatabaseCompressionFlags.NONE) ? blockInputStream : new GZIPInputStream(blockInputStream));
    }

    static IntelliJKdbxMetadata parseDatabaseMetadata(IntelliJKdbxMetadata kdbxMetadata, InputStream inputStream) throws IOException {
        MessageDigest digest = IntelliJCryptoUtil.getMessageDigestInstance();
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
            while((headerType = littleEndianDataInputStream.readByte()) != 0) {
                switch(headerType) {
                    case 1:
                        getByteArray(littleEndianDataInputStream);
                        break;
                    case 2:
                        kdbxMetadata.setCipherUuid(getByteArray(littleEndianDataInputStream));
                        break;
                    case 3:
                        kdbxMetadata.setDatabaseCompressionFlags(getInt(littleEndianDataInputStream));
                        break;
                    case 4:
                        kdbxMetadata.setBaseSeed(getByteArray(littleEndianDataInputStream));
                        break;
                    case 5:
                        kdbxMetadata.setTransformSeed(getByteArray(littleEndianDataInputStream));
                        break;
                    case 6:
                        kdbxMetadata.setTransformRounds(getLong(littleEndianDataInputStream));
                        break;
                    case 7:
                        kdbxMetadata.setEncryptionIv(getByteArray(littleEndianDataInputStream));
                        break;
                    case 8:
                        kdbxMetadata.setEncryptionKey(getByteArray(littleEndianDataInputStream));
                        break;
                    case 9:
                        kdbxMetadata.setInitBytes(getByteArray(littleEndianDataInputStream));
                        break;
                    case 10:
                        kdbxMetadata.setInnerRandomStreamId(getInt(littleEndianDataInputStream));
                        break;
                    default:
                        throw new IllegalStateException("Unknown File Header");
                }
            }

            getByteArray(littleEndianDataInputStream);
            kdbxMetadata.setHeaderHash(digest.digest());
            return kdbxMetadata;
        }
    }

    private static byte[] getByteArray(LittleEndianDataInputStream ledis) throws IOException {
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

    private static int getInt(LittleEndianDataInputStream ledis) throws IOException {
        short length = ledis.readShort();
        if (length != 4) {
            throw new IllegalStateException("Int required but length was " + length);
        } else {
            return ledis.readInt();
        }
    }

    private static long getLong(LittleEndianDataInputStream ledis) throws IOException {
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
            NodeList protectedContent = (NodeList) xpath.evaluate("//*[@Protected='True']", doc, XPathConstants.NODESET);

            for(int i = 0; i < protectedContent.getLength(); ++i) {
                Element element = (Element)protectedContent.item(i);
                Element res = getElement(".", element, false);
                String base64 = res == null ? null : res.getTextContent();
                byte[] encrypted = Base64.getDecoder().decode(base64.getBytes());
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

    static Element getElement(String elementPath, Element parentElement, boolean create) {
        try {
            Element result = (Element)xpath.evaluate(elementPath, parentElement, XPathConstants.NODE);
            if (result == null && create) {
                result = createHierarchically(elementPath, parentElement);
            }

            return result;
        } catch (XPathExpressionException var4) {
            throw new IllegalStateException(var4);
        }
    }

    private static Element createHierarchically(String elementPath, Element startElement) {
        Element currentElement = startElement;
        String[] var3 = elementPath.split("/");
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String elementName = var3[var5];

            try {
                Element nextElement = (Element)xpath.evaluate(elementName, currentElement, XPathConstants.NODE);
                if (nextElement == null) {
                    nextElement = (Element)currentElement.appendChild(currentElement.getOwnerDocument().createElement(elementName));
                }

                currentElement = nextElement;
            } catch (XPathExpressionException var8) {
                throw new IllegalStateException(var8);
            }
        }

        return currentElement;
    }

    static Element setElementContent(String elementPath, Element parentElement, String value) {
        Element result = getElement(elementPath, parentElement, true);
        result.setTextContent(value);
        return result;
    }
}
