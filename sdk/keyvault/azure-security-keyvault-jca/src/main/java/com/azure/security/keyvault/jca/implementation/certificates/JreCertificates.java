// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.certificates;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.stream.Stream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.Objects;

import static java.util.logging.Level.WARNING;

/**
 * This class provides the certificates from jre key store. It only provides certificates. It does not provide key entries from jre key store.
 */
public final class JreCertificates implements AzureCertificates {
    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JreCertificates.class.getName());

    /**
     * Stores the jre key store aliases.
     */
    private final List<String> aliases;

    /**
     * Stores the jre key store certificates.
     */
    private final  Map<String, Certificate> certs;

    /**
     * Stores the jre key store keys
     */
    private final  Map<String, Key> keys;

    /**
     * Stores the singleton
     */
    private static final JreCertificates INSTANCE = new JreCertificates();

    /**
     * Private constructor
     */
    private JreCertificates() {
        KeyStore jreKeyStore = JREKeyStore.getDefault();
        aliases = Optional.ofNullable(jreKeyStore)
            .map(a -> {
                try {
                    return Collections.unmodifiableList(Collections.list(a.aliases()));
                } catch (KeyStoreException e) {
                    LOGGER.log(WARNING, "Unable to load the jre key store aliases.", e);
                }
                return null;
            })
            .orElseGet(Collections::emptyList);
        certs = aliases.stream()
            .collect(
                HashMap::new,
                (m, v) -> {
                    try {
                        m.put(v, jreKeyStore.getCertificate(v));
                    } catch (KeyStoreException e) {
                        LOGGER.log(WARNING, "Unable to get the jre key store certificate.", e);
                    }
                },
                HashMap::putAll);
        keys = Collections.emptyMap();
    }

    /**
     *
     * @return the singleton.
     */
    public static JreCertificates getInstance() {
        return INSTANCE;
    }


    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public Map<String, Certificate> getCertificates() {
        return certs;
    }

    @Override
    public Map<String, Key> getCertificateKeys() {
        return keys;
    }

    @Override
    public void deleteEntry(String alias) {

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
}
