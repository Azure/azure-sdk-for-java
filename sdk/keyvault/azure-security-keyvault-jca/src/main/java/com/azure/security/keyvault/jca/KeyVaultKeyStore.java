// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * The Azure Key Vault implementation of the KeyStoreSpi.
 */
public final class KeyVaultKeyStore extends KeyStoreSpi {

    /**
     * Stores the key-store name.
     */
    public static final String KEY_STORE_TYPE = "AzureKeyVault";

    /**
     * Stores the algorithm name.
     */
    public static final String ALGORITHM_NAME = KEY_STORE_TYPE;

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyVaultKeyStore.class.getName());

    /**
     * Stores the list of aliases.
     */
    private List<String> aliases;

    /**
     * Stores the certificates by alias.
     */
    private final HashMap<String, Certificate> certificates = new HashMap<>();

    /**
     * Stores the certificate keys by alias.
     */
    private final HashMap<String, Key> certificateKeys = new HashMap<>();

    /**
     * Stores the creation date.
     */
    private final Date creationDate;

    /**
     * Stores the key vault client.
     */
    private KeyVaultClient keyVaultClient;

    /**
     * Stores the jre key store.
     */
    private static final KeyStore DEFAULT_KEY_STORE;

    /**
     * Stores the jre key store aliases.
     */
    private static final Set<String> JRE_ALIASES;

    /**
     * Constructor.
     *
     * <p>
     * The constructor uses System.getProperty for
     * <code>azure.keyvault.uri</code>,
     * <code>azure.keyvault.aadAuthenticationUrl</code>,
     * <code>azure.keyvault.tenantId</code>,
     * <code>azure.keyvault.clientId</code>,
     * <code>azure.keyvault.clientSecret</code> and
     * <code>azure.keyvault.managedIdentity</code> to initialize the
     * Key Vault client.
     * </p>
     */
    public KeyVaultKeyStore() {
        creationDate = new Date();
        String keyVaultUri = System.getProperty("azure.keyvault.uri");
        String tenantId = System.getProperty("azure.keyvault.tenant-id");
        String clientId = System.getProperty("azure.keyvault.client-id");
        String clientSecret = System.getProperty("azure.keyvault.client-secret");
        String managedIdentity = System.getProperty("azure.keyvault.managed-identity");
        if (clientId != null) {
            keyVaultClient = new KeyVaultClient(keyVaultUri, tenantId, clientId, clientSecret);
        } else {
            keyVaultClient = new KeyVaultClient(keyVaultUri, managedIdentity);
        }

    }

    static {
        DEFAULT_KEY_STORE = JREKeyStore.getDefault();
        JRE_ALIASES = new HashSet<>();
        if (null != DEFAULT_KEY_STORE) {
            try {
                JRE_ALIASES.addAll(Collections.list(DEFAULT_KEY_STORE.aliases()));
            } catch (KeyStoreException e) {
                LOGGER.log(WARNING, "Unable to load the jre key store aliases.", e);
            }
        }
    }

    @Override
    public Enumeration<String> engineAliases() {
        if (aliases == null) {
            aliases = keyVaultClient.getAliases();
        }
        Set<String> als = new HashSet<>();
        als.addAll(aliases);
        als.addAll(JRE_ALIASES);
        return Collections.enumeration(als);
    }

    @Override
    public boolean engineContainsAlias(String alias) {
        return engineIsCertificateEntry(alias);
    }

    @Override
    public void engineDeleteEntry(String alias) {
    }

    @Override
    public boolean engineEntryInstanceOf(String alias, Class<? extends KeyStore.Entry> entryClass) {
        return super.engineEntryInstanceOf(alias, entryClass);
    }

    @Override
    public Certificate engineGetCertificate(String alias) {
        Certificate certificate = null;
        if (certificates.containsKey(alias)) {
            certificate = certificates.get(alias);
        } else {
            if (aliases == null) {
                aliases = keyVaultClient.getAliases();
            }
            if (aliases.contains(alias)) {
                certificate = keyVaultClient.getCertificate(alias);
                if (certificate != null) {
                    certificates.put(alias, certificate);
                }
            }
            if (certificate == null) {
                try {
                    certificate = DEFAULT_KEY_STORE.getCertificate(alias);
                } catch (KeyStoreException e) {
                    LOGGER.log(WARNING, "Unable to load certificate from jre Key store.", e);
                }
            }
        }
        return certificate;
    }

    @Override
    public String engineGetCertificateAlias(Certificate cert) {
        String alias = null;
        if (cert != null) {
            if (aliases == null) {
                aliases = keyVaultClient.getAliases();
            }
            for (String candidateAlias : aliases) {
                Certificate certificate = engineGetCertificate(candidateAlias);
                if (certificate.equals(cert)) {
                    alias = candidateAlias;
                    break;
                }
            }
            if (null == alias) {
                try {
                    alias = DEFAULT_KEY_STORE.getCertificateAlias(cert);
                } catch (KeyStoreException e) {
                    LOGGER.log(WARNING, "Unable to load the alias from jre Key store.", e);
                }
            }
        }
        return alias;
    }

    @Override
    public Certificate[] engineGetCertificateChain(String alias) {
        Certificate[] chain = null;
        Certificate certificate = engineGetCertificate(alias);
        if (certificate != null) {
            chain = new Certificate[1];
            chain[0] = certificate;
        } else {
            try {
                chain = DEFAULT_KEY_STORE.getCertificateChain(alias);
            } catch (KeyStoreException e) {
                LOGGER.log(WARNING, "Unable to load the certificate chain from jre Key store.", e);
            }
        }
        return chain;
    }

    @Override
    public Date engineGetCreationDate(String alias) {
        return new Date(creationDate.getTime());
    }

    @Override
    public KeyStore.Entry engineGetEntry(String alias, KeyStore.ProtectionParameter protParam) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        return super.engineGetEntry(alias, protParam);
    }

    @Override
    public Key engineGetKey(String alias, char[] password) {
        Key key;
        if (certificateKeys.containsKey(alias)) {
            key = certificateKeys.get(alias);
        } else {
            key = keyVaultClient.getKey(alias, password);
            if (key != null) {
                certificateKeys.put(alias, key);
                if (aliases == null) {
                    aliases = keyVaultClient.getAliases();
                }
                if (!aliases.contains(alias)) {
                    aliases.add(alias);
                }
            } else {
                key = JREKeyStore.getKey(DEFAULT_KEY_STORE, alias);
            }
        }
        return key;
    }

    @Override
    public boolean engineIsCertificateEntry(String alias) {
        if (aliases == null) {
            aliases = keyVaultClient.getAliases();
        }
        Set<String> als = new HashSet<>();
        als.addAll(aliases);
        als.addAll(JRE_ALIASES);
        return als.contains(alias);
    }

    @Override
    public boolean engineIsKeyEntry(String alias) {
        if (aliases == null) {
            aliases = keyVaultClient.getAliases();
        }
        return aliases.contains(alias);
    }

    @Override
    public void engineLoad(KeyStore.LoadStoreParameter param) {
        if (param instanceof KeyVaultLoadStoreParameter) {
            KeyVaultLoadStoreParameter parameter = (KeyVaultLoadStoreParameter) param;
            if (parameter.getClientId() != null) {
                keyVaultClient = new KeyVaultClient(
                        parameter.getUri(),
                        parameter.getTenantId(),
                        parameter.getClientId(),
                        parameter.getClientSecret());
            } else if (parameter.getManagedIdentity() != null) {
                keyVaultClient = new KeyVaultClient(
                        parameter.getUri(),
                        parameter.getManagedIdentity()
                );
            } else {
                keyVaultClient = new KeyVaultClient(parameter.getUri());
            }
        }
        sideLoad();
    }

    @Override
    public void engineLoad(InputStream stream, char[] password) {
        sideLoad();
    }

    @Override
    public void engineSetCertificateEntry(String alias, Certificate certificate) {
        if (aliases == null) {
            aliases = keyVaultClient.getAliases();
        }
        if (!aliases.contains(alias)) {
            aliases.add(alias);
            certificates.put(alias, certificate);
        }
    }

    @Override
    public void engineSetEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam) throws KeyStoreException {
        super.engineSetEntry(alias, entry, protParam);
    }

    @Override
    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) {
    }

    @Override
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) {
    }

    @Override
    public int engineSize() {
        int size = 0;
        try {
            size = DEFAULT_KEY_STORE.size();
        } catch (KeyStoreException e) {
            LOGGER.log(WARNING, "Unable to get the size of the jre key store.", e);
        }
        return  size + (aliases != null ? aliases.size() : 0);
    }

    @Override
    public void engineStore(OutputStream stream, char[] password) {
    }

    @Override
    public void engineStore(KeyStore.LoadStoreParameter param) {
    }

    /**
     * Get the filenames.
     *
     * @param path the path.
     * @return the filenames.
     * @throws IOException when an I/O error occurs.
     */
    private String[] getFilenames(String path) throws IOException {
        List<String> filenames = new ArrayList<>();
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    String resource;
                    while ((resource = br.readLine()) != null) {
                        filenames.add(resource);
                    }
                }
            }
        }
        return filenames.toArray(new String[0]);
    }

    /**
     * Read all the bytes for a given input stream.
     *
     * @param inputStream the input stream.
     * @return the byte-array.
     * @throws IOException when an I/O error occurs.
     */
    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        byte[] bytes;
        try (ByteArrayOutputStream byteOutput = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            while (true) {
                int r = inputStream.read(buffer);
                if (r == -1) {
                    break;
                }
                byteOutput.write(buffer, 0, r);
            }
            bytes = byteOutput.toByteArray();
        }
        return bytes;
    }

    /**
     * Side-load certificate from classpath.
     */
    private void sideLoad() {
        try {
            String[] filenames = getFilenames("/keyvault");
            if (filenames.length > 0) {
                for (String filename : filenames) {
                    try (InputStream inputStream = getClass().getResourceAsStream("/keyvault/" + filename)) {
                        String alias = filename;
                        if (alias != null) {
                            if (alias.lastIndexOf('.') != -1) {
                                alias = alias.substring(0, alias.lastIndexOf('.'));
                            }
                            byte[] bytes = readAllBytes(inputStream);
                            try {
                                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                                X509Certificate certificate = (X509Certificate) cf.generateCertificate(
                                        new ByteArrayInputStream(bytes));
                                engineSetCertificateEntry(alias, certificate);
                                LOGGER.log(INFO, "Side loaded certificate: {0} from: {1}",
                                        new Object[]{alias, filename});
                            } catch (CertificateException e) {
                                LOGGER.log(WARNING, "Unable to side-load certificate from: " + filename, e);
                            }
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            LOGGER.log(WARNING, "Unable to determine certificates to side-load", ioe);
        }
    }

    private static class JREKeyStore {
        private static final String JAVA_HOME =  privilegedGetProperty("java.home", "");
        private static final Path STORE_PATH = Paths.get(JAVA_HOME).resolve("lib").resolve("security");
        private static final Path DEFAULT_STORE = STORE_PATH.resolve("cacerts");
        private static final Path JSSE_DEFAULT_STORE = STORE_PATH.resolve("jssecacerts");
        private static final String KEY_STORE_PASSWORD = privilegedGetProperty("javax.net.ssl.keyStorePassword", "changeit");
        private static final String KEY_PASSWORD = KEY_STORE_PASSWORD;

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
            try (InputStream inStream = Files.newInputStream(getKeyStoreFile())) {
                ks.load(inStream, KEY_STORE_PASSWORD.toCharArray());
            } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
                LOGGER.log(WARNING, "unable to load the jre key store", e);
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

            Path[] fileNames =
                new Path[]{storeProp, DEFAULT_STORE};
            for (Path fileName : fileNames) {
                if (Files.exists(fileName) && Files.isReadable(fileName)) {
                    return fileName;
                }
            }
            return null;
        }

        private static Key getKey(KeyStore ks, String alias) {
            try {
                return  ks.getKey(alias, KEY_PASSWORD.toCharArray());
            } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
                LOGGER.log(WARNING, "Unable to get the key from jre key store.", e);
            }
            return null;
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
