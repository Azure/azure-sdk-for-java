// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.certificates;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Store certificates loaded from file system.
 */
public final class SpecificPathCertificates implements AzureCertificates {

    private static final Map<String, SpecificPathCertificates> CACHE = new HashMap<>();

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SpecificPathCertificates.class.getName());

    /**
     * Stores the specific path aliases.
     */
    private final List<String> aliases = new ArrayList<>();

    /**
     * Stores the specific path certificates by alias.
     */
    private final Map<String, Certificate> certificates = new HashMap<>();

    /**
     * Stores the specific path certificate keys by alias.
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
        aliases.remove(alias);
        certificates.remove(alias);
        certificateKeys.remove(alias);
    }

    /**
     * Constructor.
     *
     * @param certificatePath Store the file path where certificates are placed
     */
    private SpecificPathCertificates(String certificatePath) {
        this.certificatePath = certificatePath;
        loadCertificatesFromSpecificPath();
    }

    /**
     * Add alias and certificate
     *
     * @param alias       certificate alias
     * @param certificate certificate value
     */
    public void setCertificateEntry(String alias, Certificate certificate) {
        //Add verification to avoid certificate files with the same file name but different suffixes
        if (aliases.contains(alias)) {
            LOGGER.log(WARNING, "Cannot load certificates with the same alias in specific path", alias);
            return;
        }
        aliases.add(alias);
        certificates.put(alias, certificate);
    }

    /**
     * If the file can be parsed into a certificate, add it to the list
     *
     * @param file file which try to parsed into a certificate
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    private void setCertificateByFile(File file) throws IOException {
        X509Certificate certificate;
        try (InputStream inputStream = new FileInputStream(file);
            BufferedInputStream bytes = new BufferedInputStream(inputStream)) {
            String alias = toCertificateAlias(file);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) cf.generateCertificate(bytes);
            if (certificate != null) {
                setCertificateEntry(alias, certificate);
                LOGGER.log(INFO, "Load certificate from specific path. alias = {0}, thumbprint = {1}, file = {2}",
                    new Object[]{alias, getThumbprint(certificate), file.getName()});
            }
        } catch (CertificateException e) {
            LOGGER.log(WARNING, "Unable to load certificate from: " + file.getName(), e);
        }
    }

    /**
     * Load certificates in the file directory
     */
    private void loadCertificatesFromSpecificPath() {
        try {
            List<File> files = getFiles();
            for (File file : files) {
                setCertificateByFile(file);
            }
        } catch (IOException ioe) {
            LOGGER.log(WARNING, "Unable to determine certificates to specific path", ioe);
        }
    }

    /**
     * Get thumbprint for a certificate
     *
     * @param certificate certificate value
     * @return certificate thumbprint
     */
    String getThumbprint(Certificate certificate) {
        try {
            return DigestUtils.sha1Hex(certificate.getEncoded());
        } catch (CertificateEncodingException e) {
            LOGGER.log(WARNING, "Unable to get thumbprint for certificate", e);
        }
        return "";
    }

    /**
     * Get alias from file
     *
     * @param file File containing certificate information
     * @return certificate alias
     */
    public static String toCertificateAlias(File file) {
        String fileName = file.getName();
        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot == -1) {
            return fileName;
        }
        return fileName.substring(0, lastIndexOfDot);
    }

    /**
     * Load all certificates in the folder, to avoid alias duplication, do not read files in subdirectories.
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
                .forEach(files::add);
        return files;
    }

    /**
     * Get File System certificates by path
     *
     * @param path certificate path, which works only in first time
     * @return file certificate
     */
    public static synchronized SpecificPathCertificates getSpecificPathCertificates(String path) {
        SpecificPathCertificates result = CACHE.getOrDefault(path, null);
        if (result == null) {
            result = new SpecificPathCertificates(path);
            CACHE.put(path, result);
        }
        return result;
    }
}
