package com.azure.security.keyvault.jca;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import static java.util.logging.Level.WARNING;

public class JreCertificates implements AzureCertificates {
    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JreCertificates.class.getName());

    /**
     * Stores the jre key store.
     */
    private static final KeyStore JRE_KEY_STORE;

    /**
     * Stores the jre key store aliases.
     */
    private static final List<String> JRE_KS_ALIASES;

    /**
     * Stores the jre key store certificates.
     */
    private static final  Map<String, Certificate> JRE_KS_CERTS;

    /**
     * Stores the singleton
     */
    private static final JreCertificates INSTANCE = new JreCertificates();

    static {
        JRE_KEY_STORE = JREKeyStore.getDefault();
        List<String> JRE_KS_ALIASES1 = Collections.emptyList();
        if (null != JRE_KEY_STORE) {
            try {
                JRE_KS_ALIASES1 = Collections.unmodifiableList(Collections.list(JRE_KEY_STORE.aliases()));
            } catch (KeyStoreException e) {
                LOGGER.log(WARNING, "Unable to load the jre key store aliases.", e);
            }
        }
        JRE_KS_ALIASES = JRE_KS_ALIASES1;
        JRE_KS_CERTS = JRE_KS_ALIASES.stream()
            .collect(Collectors.toMap(a -> a, a -> {
                try {
                    return JRE_KEY_STORE.getCertificate(a);
                } catch (KeyStoreException e) {
                    LOGGER.log(WARNING, "Unable to get the jre key store certificate.", e);
                }
                return null;
            }));
    }

    private JreCertificates() {}

    public static JreCertificates getInstance() {
        return INSTANCE;
    }


    @Override
    public List<String> getAliases() {
        return JRE_KS_ALIASES;
    }

    @Override
    public Map<String, Certificate> getCertificates() {
        return JRE_KS_CERTS;
    }

    @Override
    public Map<String, Key> getCertificateKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteEntry(String alias) {
        throw new UnsupportedOperationException();

    }

    private static class JREKeyStore {
        private static final String JAVA_HOME =  privilegedGetProperty("java.home", "");
        private static final Path STORE_PATH = Paths.get(JAVA_HOME).resolve("lib").resolve("security");
        private static final Path DEFAULT_STORE = STORE_PATH.resolve("cacerts");
        private static final Path JSSE_DEFAULT_STORE = STORE_PATH.resolve("jssecacerts");
        private static final String KEY_STORE_PASSWORD = privilegedGetProperty("javax.net.ssl.keyStorePassword", "changeit");

        private static KeyStore getDefault() {
            KeyStore defaultKeyStore = null;
            try {
                defaultKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                loadKeyStore(defaultKeyStore);
            } catch (KeyStoreException e) {
                LOGGER.log(WARNING, "Unable to get the jre key store.", e);
            }
            return defaultKeyStore;
        }

        private static void loadKeyStore(KeyStore ks) {
            InputStream inStream = null;
            try {
                inStream = Files.newInputStream(getKeyStoreFile());
                ks.load(inStream, KEY_STORE_PASSWORD.toCharArray());
            } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
                LOGGER.log(WARNING, "unable to load the jre key store", e);
            } finally {
                try {
                    inStream.close();
                } catch (NullPointerException | IOException e ) {
                    LOGGER.log(WARNING, "", e);
                }
            }
        }

        private static Path getKeyStoreFile() {
            String storePropName = privilegedGetProperty(
                "javax.net.ssl.keyStore", "");
            return getStoreFile(storePropName);
        }

        private static Path getStoreFile(String storePropName) {
            Path storeProp;
            if (storePropName.isEmpty()) {
                storeProp = JSSE_DEFAULT_STORE;
            } else {
                storeProp = Paths.get(storePropName);
            }

            Path[] fileNames = new Path[]{storeProp, DEFAULT_STORE};
            return Arrays.stream(fileNames)
                .filter(a -> Files.exists(a) && Files.isReadable(a))
                .findFirst().orElse(null);
        }

        private static String  privilegedGetProperty(String theProp, String defaultVal) {
            if (System.getSecurityManager() == null) {
                String value = System.getProperty(theProp, "");
                return (value.isEmpty()) ? defaultVal : value;
            } else {
                return AccessController.doPrivileged(
                    (PrivilegedAction<String>) () -> {
                        String value = System.getProperty(theProp, "");
                        return (value.isEmpty()) ? defaultVal : value;
                    });
            }
        }
    }
}
