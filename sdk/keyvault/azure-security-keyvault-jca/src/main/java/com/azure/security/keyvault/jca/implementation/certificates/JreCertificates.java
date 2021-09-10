// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.certificates;

import com.azure.security.keyvault.jca.implementation.JREKeyStoreProvider;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.logging.Logger;
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
     * Stores the instance of JreCertificates.
     */
    private static final JreCertificates INSTANCE = new JreCertificates();

    /**
     * Private constructor
     */
    private JreCertificates() {
        KeyStore jreKeyStore = JREKeyStoreProvider.getDefault();
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
     * @return the instance of JreCertificates.
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
}
