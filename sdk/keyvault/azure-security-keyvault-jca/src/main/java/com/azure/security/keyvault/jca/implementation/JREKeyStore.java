package com.azure.security.keyvault.jca.implementation;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.logging.Level.WARNING;

/**
 * This class provides a JRE key store.
 */
public final class JREKeyStore {
    private static final String JAVA_HOME =  privilegedGetProperty("java.home", "");
    private static final Path STORE_PATH = Paths.get(JAVA_HOME).resolve("lib").resolve("security");
    private static final Path DEFAULT_STORE = STORE_PATH.resolve("cacerts");
    private static final Path JSSE_DEFAULT_STORE = STORE_PATH.resolve("jssecacerts");
    private static final String KEY_STORE_PASSWORD = privilegedGetProperty("javax.net.ssl.keyStorePassword", "changeit");
    private static final Logger LOGGER = Logger.getLogger(JREKeyStore.class.getName());
    private static final KeyStore JRE_KEY_STORE = getJreKeyStore();


    private JREKeyStore() {

    }

    /**
     * This method returns the instance of JRE key store
     * @return the JRE key store.
     */
    public static KeyStore getDefault() {
        return  JRE_KEY_STORE;
    }


    private static KeyStore getJreKeyStore() {
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
        try (InputStream inputStream = Files.newInputStream(getKeyStoreFile())) {
            ks.load(inputStream, KEY_STORE_PASSWORD.toCharArray());
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            LOGGER.log(WARNING, "unable to load the jre key store", e);
        }
    }

    private static Path getKeyStoreFile() {
        return Stream.of(getConfiguredKeyStorePath(), JSSE_DEFAULT_STORE, DEFAULT_STORE)
            .filter(Objects::nonNull)
            .filter(Files::exists)
            .filter(Files::isReadable)
            .findFirst()
            .orElse(null);
    }

    private static Path getConfiguredKeyStorePath() {
        String configuredKeyStorePath = privilegedGetProperty("javax.net.ssl.keyStore", "");
        return Optional.of(configuredKeyStorePath)
            .filter(path -> !path.isEmpty())
            .map(Paths::get)
            .orElse(null);
    }

    private static String  privilegedGetProperty(String theProp, String defaultVal) {
        return AccessController.doPrivileged(
            (PrivilegedAction<String>) () -> {
                String value = System.getProperty(theProp, "");
                return (value.isEmpty()) ? defaultVal : value;
            });
    }
}
