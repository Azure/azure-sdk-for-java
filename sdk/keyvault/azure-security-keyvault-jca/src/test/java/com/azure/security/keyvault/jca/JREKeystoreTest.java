package com.azure.security.keyvault.jca;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.PrivateKeyDetails;
import org.apache.http.ssl.PrivateKeyStrategy;
import org.apache.http.ssl.SSLContexts;
import org.junit.jupiter.api.Test;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JREKeystoreTest {
    private static final String fileSep = File.separator;
    private static final String defaultStorePath =
        PrivilegedActionImpl.privilegedGetProperty("java.home", "") +
            fileSep + "lib" + fileSep + "security";
    private static final String defaultStore =
        defaultStorePath + fileSep + "cacerts";
    private static final String jsseDefaultStore =
        defaultStorePath + fileSep + "jssecacerts";
    private PrivateKeyStrategyImpl aliasStrategy = new PrivateKeyStrategyImpl();
    private static final String keyStorePassword = PrivilegedActionImpl.privilegedGetProperty("javax.net.ssl.keyStorePassword", "changeit");
    // private static final String keyStoreType = PrivilegedActionImpl.privilegedGetProperty("javax.net.ssl.keyStoreType", KeyStore.getDefaultType());
    //private static final String keyStoreProvider = PrivilegedActionImpl.privilegedGetProperty("javax.net.ssl.keyStoreProvider", "");
    private static final String trustStorePassword = PrivilegedActionImpl.privilegedGetProperty("javax.net.ssl.trustStorePassword", "changeit");
    //private static final String trustStoreType = PrivilegedActionImpl.privilegedGetProperty("javax.net.ssl.trustStoreType", KeyStore.getDefaultType());
    //private static final String trustStoreProvider = PrivilegedActionImpl.privilegedGetProperty("javax.net.ssl.trustStoreProvider", "");
    private static final String keyPassword = keyStorePassword;
    private static final Logger logger = Logger.getLogger(JREKeystoreTest.class.getName());


    private static String privateKey =
        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC/QWKhTKDVdZW0" +
            "rdC8OLD/3oCrkS4UMh3jh81g8vjOetuO8pki94Aj+vfvhIdSqB8ddhh8LdilAWp9" +
            "e03TLpgXf9DlVTVrkt7Yr7t1+V3YvSQonIf1RODSpLumHZY7VvIB2Hu9rDAxPGd4" +
            "AXTxJSokOb4JBEXCTSves8a3vY6NxNwprlBzRpqC7q/MyvpKki92I7CvWB8SQLla" +
            "9y7ezewUpf9Wkc2SixuDZ+xQNPZZv7DBynF1DB9wGkhfbcsUmQ5GD5ZD3fDiXwL6" +
            "CoTFXRr+KAEZBMwmtBBj93q9se2dWE1KWFD9fRNjOHrl6+rAqaQKMcjwNt0K5jFp" +
            "oSBllJ35AgMBAAECggEAEfZHGBUFSeboL3bfkr3UScOQ3AwfgsAONI9Nh5xqJ6I8" +
            "OpmoLjOGclqgrHsK5oVpnq+3rvnzTdPrN2u8LGhE2ngjm1Y8VSUPDtS6S2MnIDLr" +
            "UXEaB7v3TXExOnGu6VXKFshJHtAsGoOsyAWDAR7XbV2K7nfbQVtUEm9Z62m/GKtJ" +
            "lwrerjsfFpgEKl2gMOKBLHCLxNHuDg/K0jpyXJnp+0Pp6o2pveituP4mQXh1jOXW" +
            "0cQf5WeevPyXdR9SsbVnJYpd9nKbWdN73QKfXRYsDOT9i/oQpqeEPW+9ncARsEFc" +
            "HSj+UEa17Gq7RWOfUwdz9WwdsA1TFjaekEI7eDM1fQKBgQD4WEYcfFpKO9GBAYh8" +
            "gpxNQl7TnIArsE5HA0ohi+7AQykmd9jfutTqGqytuTAywykBOuwpYXRhZR4eC2Uy" +
            "ZRkjojIjROrsXdU/OyLU9SlCMxBxnIIjT6oNB662/hSRKTyDR/vhEdfr5pAMbrXD" +
            "bu6DYG2DtM/P4l4PEphT25mBPwKBgQDFJpw/EMKVVSr1tq4mf9ouRtRztJEKjMB1" +
            "K35xua0ci+iFlT8X3llVO9Ex4OXxfb3rOmHvVp4u5h7gWP3/gAn3k99Mx8Y7MYbo" +
            "Xi7V4dtf4cy3v71R7IkKRElNmzFpQB3FKbPtbz3qAiRDtDCs4uWipNsO8n2r+gCY" +
            "LP4+8O9axwKBgDWX3zGAiH+inxim+wxbp11o6EvkZyiGeK4MgK/yocht9fBuRqgp" +
            "io8myIMJBuS9hxjT3Wkktdfa2YAEV4djl+Q/uXAxDD2MFlv56A9rBMdbe73414DF" +
            "TV5YfRTSih+1dXCjVTECB8XJ0OZN8f3F78T8R0X3/CzRnhjEm4jlSCBDAoGBALsL" +
            "C5flrcaB+/UMsSLDj9rxxiRUPns02G8Rqa/5ydxYfG0oEKjzgeuUib95sI6xjlq6" +
            "lCm/EupJ1ydgJvKdplcmCufMbSzBq02P9X1j/35zOodmORadg9KiwK0JPRKvCs+A" +
            "5jywmpwGFiPJs5iC1/y7zTzcIRDlamT14u0SJvQHAoGAY2bt4Xltpu7d+KO7ULAl" +
            "l6shyIyoDMlq8XIjqaQvyqjTvbA7aLAvecy0BUm+uE3FI+ZtvVwgyOaw9zgsNqLN" +
            "PkL2jUZWVnqOLar8BOb1fDSKQK9QdFAy+hV/9556lhk6znjWAHGOe7aoaZfZYlEA" +
            "aPskotuFa1bPRDIj/PP6DvQ=";

    private static String publicCert = "MIIDfTCCAmWgAwIBAgIUauZKENDcSXD7V3lTVt06BHn2M2MwDQYJKoZIhvcNAQEL" +
        "BQAwTjELMAkGA1UEBhMCQ04xETAPBgNVBAgMCFNIQU5HSEFJMQswCQYDVQQKDAJv" +
        "dTEMMAoGA1UECwwDb3VuMREwDwYDVQQDDAhteWRvbWFpbjAeFw0yMTA0MjUwODU3" +
        "MDZaFw0yMTA1MDUwODU3MDZaME4xCzAJBgNVBAYTAkNOMREwDwYDVQQIDAhTSEFO" +
        "R0hBSTELMAkGA1UECgwCb3UxDDAKBgNVBAsMA291bjERMA8GA1UEAwwIbXlkb21h" +
        "aW4wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC/QWKhTKDVdZW0rdC8" +
        "OLD/3oCrkS4UMh3jh81g8vjOetuO8pki94Aj+vfvhIdSqB8ddhh8LdilAWp9e03T" +
        "LpgXf9DlVTVrkt7Yr7t1+V3YvSQonIf1RODSpLumHZY7VvIB2Hu9rDAxPGd4AXTx" +
        "JSokOb4JBEXCTSves8a3vY6NxNwprlBzRpqC7q/MyvpKki92I7CvWB8SQLla9y7e" +
        "zewUpf9Wkc2SixuDZ+xQNPZZv7DBynF1DB9wGkhfbcsUmQ5GD5ZD3fDiXwL6CoTF" +
        "XRr+KAEZBMwmtBBj93q9se2dWE1KWFD9fRNjOHrl6+rAqaQKMcjwNt0K5jFpoSBl" +
        "lJ35AgMBAAGjUzBRMB0GA1UdDgQWBBRJUbpVuE6UECeZZJswRaXb6rPHVDAfBgNV" +
        "HSMEGDAWgBRJUbpVuE6UECeZZJswRaXb6rPHVDAPBgNVHRMBAf8EBTADAQH/MA0G" +
        "CSqGSIb3DQEBCwUAA4IBAQBh4V0n0+Dvvsy05Gp6MWizqEVzIQNdVto/3OHpg1Bt" +
        "pYsmj+GqZpO/ElcXJmGVvWnx8uN1bT+sugxlBzJME/j9inhv1LJ89vd0vXXDn62t" +
        "jQNjLiKMj7VusK3xOfHcvQ78N+horyuVIU6NhZ5uT/xMz5AcovzwUA8vaS4jCRfK" +
        "EHoGeQvjuAvPaUIWdVlDeD5t+rkupkysEeMIrMoVB969gt34Ki0NuxDxJE7YxEJq" +
        "6rCfWN6NukKFTKgVhwOg5r0iNBf1ewmujaHWA9eX8Ug62yS1liQyQXQmJKlJWNqh" +
        "aoEAAH9ED5yDWyHROIa1DEaEfI6MjXS+ZusfLctC1tf/";

    @Test
    public void testLocalKeystore() throws Exception {
        //   Security.insertProviderAt(new KeyVaultJcaProvider(), 1);

        /*
         * Trust manager is used by the client, as the client need trust the server.
         */
        Security.insertProviderAt(new KeyVaultTrustManagerFactoryProvider(), 1);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (final FileInputStream inStream = new FileInputStream(getKeyStoreFile())) {
            keyStore.load(inStream, keyStorePassword.toCharArray());
        }
        keyStore.setEntry(aliasStrategy.chooseAlias(null, null), new KeyStore.PrivateKeyEntry(getKey(), getCertChain()), new KeyStore.PasswordProtection(keyPassword.toCharArray()));
        /*
         * Setup server side.
         */
        int randomPort = ThreadLocalRandom.current().nextInt(49452, 52570);
        logger.log(Level.INFO, "The port used is " + randomPort);
        SSLContext sslContext = SSLContexts.custom()
            .loadKeyMaterial(keyStore, keyPassword.toCharArray(), aliasStrategy)
            .build();
        SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(randomPort);

        Thread serverThread = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    try (OutputStream outputStream = socket.getOutputStream()) {
                        outputStream.write("HTTP/1.1 204\r\n".getBytes());
                        outputStream.flush();
                    }
                } catch (IOException ioe) {
                    logger.log(Level.SEVERE, "Server can't write to the client socket.", ioe);
                }
            }
        });
        serverThread.start();

        /*
         * Setup client side
         */

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

        try (final FileInputStream inStream = new FileInputStream(getTrustStoreFile())) {
            trustStore.load(inStream, trustStorePassword.toCharArray());
        }
        trustStore.setEntry(aliasStrategy.chooseAlias(null, null), new KeyStore.PrivateKeyEntry(getKey(), getCertChain()), new KeyStore.PasswordProtection(keyPassword.toCharArray()));


        SSLContext clientSSLContext = SSLContexts.custom()
            .loadTrustMaterial(trustStore, null)
            .build();
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
            clientSSLContext, (hostname, session) -> true);

        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(
            RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionSocketFactory)
                .build());

        /*
         * And now execute the test.
         */
        String result = null;

        try (CloseableHttpClient client = HttpClients.custom().setConnectionManager(manager).build()) {
            HttpGet httpGet = new HttpGet("https://localhost:" + randomPort);
            ResponseHandler<String> responseHandler = (HttpResponse response) -> {
                int status = response.getStatusLine().getStatusCode();
                String result1 = null;
                if (status == 204) {
                    result1 = "Success";
                }
                return result1;
            };
            result = client.execute(httpGet, responseHandler);
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "client can't read from the server socket.", ioe);
        }


        /*
         * And verify all went well.
         */
        assertEquals("Success", result);


    }

    private static PrivateKey getKey() throws Exception {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
        PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);
        return privKey;
    }

    private static Certificate[] getCertChain() {
        try {
            byte[] certificateBytes = Base64.getDecoder().decode(publicCert);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate certificate = cf.generateCertificate(new ByteArrayInputStream(certificateBytes));
            return new Certificate[]{certificate};
        } catch (CertificateException e) {
            throw new ProviderException(e);
        }
    }

    private File getKeyStoreFile() {
        String storePropName = PrivilegedActionImpl.privilegedGetProperty(
            "javax.net.ssl.keyStore", jsseDefaultStore);

        String[] fileNames =
            new String[]{storePropName, defaultStore};
        for (String fileName : fileNames) {
            File f = new File(fileName);
            if (f.isFile() && f.canRead()) {
                return f;
            }
        }
        return null;
    }

    private File getTrustStoreFile() {
        String storePropName = PrivilegedActionImpl.privilegedGetProperty(
            "javax.net.ssl.trustStore", jsseDefaultStore);
        String[] fileNames =
            new String[]{storePropName, defaultStore};
        for (String fileName : fileNames) {
            File f = new File(fileName);
            if (f.isFile() && f.canRead()) {
                return f;
            }


        }
        return null;
    }

    private static class PrivateKeyStrategyImpl implements PrivateKeyStrategy {
        @Override
        public String chooseAlias(Map<String, PrivateKeyDetails> map, Socket socket) {
            return "a-humble-key-entry-alias";
        }

    }


    private static class PrivilegedActionImpl implements PrivilegedAction<String> {

        private static String privilegedGetProperty(String theProp, String defaultVal) {
            if (System.getSecurityManager() == null) {
                String value = System.getProperty(theProp);
                return (value == null || value.isEmpty()) ? defaultVal : value;
            } else {
                return AccessController.doPrivileged(
                    new PrivilegedActionImpl(theProp, defaultVal));
            }
        }

        private String theProp;
        private String defaultVal;

        private PrivilegedActionImpl(String theProp) {
            this.theProp = theProp;

        }

        private PrivilegedActionImpl(String theProp, String defaultVal) {
            this.theProp = theProp;
            this.defaultVal = defaultVal;

        }

        public String run() {
            String value = System.getProperty(theProp);
            return (value == null || value.isEmpty()) ? defaultVal : value;
        }
    }


}
