// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * Store certificates loaded from file system.
 */
public final class FileSystemCertificates implements AzureCertificates {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FileSystemCertificates.class.getName());

    /**
     * Stores the jre key store aliases.
     */
    private final List<String> aliases = new ArrayList<>();

    /**
     * Stores the file system certificates by alias.
     */
    private final Map<String, Certificate> certificates = new HashMap<>();

    /**
     * Stores the file system certificate keys by alias.
     */
    private final Map<String, Key> certificateKeys = new HashMap<>();

    private final String certificatePath;

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public Map<String, Certificate> getCertificates() {
        return certificates;
    }

    @Override
    public Map<String, Key> getCertificateKeys() {
        return certificateKeys;
    }

    @Override
    public void deleteEntry(String alias) {
        if (aliases != null) {
            aliases.remove(alias);
        }
        certificates.remove(alias);
        certificateKeys.remove(alias);
    }

    FileSystemCertificates(String certificatePath) {
        this.certificatePath = certificatePath;
    }

    /**
     * Add alias
     * @param alias certificate alias
     * @param certificate certificate value
     */
    public void setCertificateEntry(String alias, Certificate certificate) {
        if (aliases != null) {
            aliases.add(alias);
            certificates.put(alias, certificate);
        }
    }

    private void setCertificateByAliasAndFile(String alias, File file) throws IOException {
        X509Certificate certificate = null;
        try (InputStream inputStream = new FileInputStream(file)) {

            BufferedInputStream bytes = new BufferedInputStream(inputStream);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) cf.generateCertificate(bytes);

            if (certificate != null) {
                setCertificateEntry(alias, certificate);
                LOGGER.log(INFO, "Load file system certificate: {0} from: {1}",
                    new Object[]{alias, file.getName()});
            }

        } catch (CertificateException e) {
            LOGGER.log(WARNING, "Unable to load file system certificate from: " + file.getName(), e);
        }
    }

    void loadCertificatesFromFileSystem() {
        try {
            List<File> files = getFiles();
            for (File file : files) {
                String alias = file.getName();
                if (alias.lastIndexOf('.') != -1) {
                    alias = alias.substring(0, alias.lastIndexOf('.'));
                }
                setCertificateByAliasAndFile(alias, file);

            }
        } catch (IOException ioe) {
            LOGGER.log(WARNING, "Unable to determine certificates to file system", ioe);
        }
    }

    /**
     * Load all certificates in the folder
     */
    private List<File> getFiles() {
        List<File> files = new ArrayList<>();
        File filePackage = new File(certificatePath);
        File[] array = filePackage.listFiles();
        Optional.ofNullable(array)
            .map(Arrays::stream)
            .orElseGet(Stream::empty)
            .filter(Objects::nonNull)
            .filter(File::isFile)
            .filter(File::exists)
            .filter(File::canRead)
            .forEach(a -> files.add(a));
        return files;
    }
}
