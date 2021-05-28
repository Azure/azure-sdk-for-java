// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.io.*;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * Store certificates loaded from file system.
 */
public class FileSystemCertificates implements AzureCertificates {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FileSystemCertificates.class.getName());

    private final Map<String, List<String>> fileSystemAlias = new HashMap<>();

    /**
     * Stores the file system certificates by alias.
     */
    private final Map<String, Certificate> certificates = new HashMap<>();

    /**
     * Stores the file system certificate keys by alias.
     */
    private final Map<String, Key> certificateKeys = new HashMap<>();

    private final List<String> certPaths;

    @Override
    public List<String> getAliases() {
        return fileSystemAlias.values()
                              .stream()
                              .flatMap(l -> l.stream())
                              .collect(Collectors.toList());
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
        fileSystemAlias.values().forEach(a -> a.remove(alias));
        certificates.remove(alias);
        certificateKeys.remove(alias);
    }

    FileSystemCertificates(List<String> certPaths) {
        this.certPaths = certPaths;
    }

    /**
     * Add certificate.
     * @param alias certificate alias
     * @param certificate certificate
     */
    public void setCertificateEntry(String filePath, String alias, Certificate certificate) {
        List<String> allAlias = fileSystemAlias.values()
                                               .stream()
                                               .flatMap(l -> l.stream())
                                               .collect(Collectors.toList());
        if (!allAlias.contains(alias)) {
            if (fileSystemAlias.get(filePath) == null) {
                fileSystemAlias.put(filePath, new ArrayList<>());
            }
            fileSystemAlias.get(filePath).add(alias);
            certificates.put(alias, certificate);
        }
    }


    private X509Certificate getCertificateByAliasAndStream(InputStream inputStream, String filename, String alias) throws IOException {
        X509Certificate certificate = null;
        if (alias != null) {
            byte[] bytes = KeyVaultKeyStore.readAllBytes(inputStream);
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                certificate = (X509Certificate) cf.generateCertificate(
                    new ByteArrayInputStream(bytes));
            } catch (CertificateException e) {
                LOGGER.log(WARNING, "Unable to load file system certificate from: " + filename, e);
            }
        }
        return certificate;
    }

    void loadCertificatesFromFileSystem() {
        try {
            for (String filePath: certPaths) {
                String[] filenames = getAbsoluteFilename(filePath);
                if (filenames.length > 0) {
                    for (String filename : filenames) {
                        try (InputStream inputStream = new FileInputStream(new File(filePath + filename))) {
                            String alias = filename;
                            if (alias != null) {
                                if (alias.lastIndexOf('.') != -1) {
                                    alias = alias.substring(0, alias.lastIndexOf('.'));
                                }
                                X509Certificate certificate = getCertificateByAliasAndStream(inputStream, alias, filename);
                                if (certificate != null) {
                                    setCertificateEntry(filePath, alias, certificate);
                                    LOGGER.log(INFO, "Load file system certificate: {0} from: {1}",
                                        new Object[]{alias, filename});
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            LOGGER.log(WARNING, "Unable to determine certificates to file system", ioe);
        }
    }

    /**
     * Get the filenames.
     *
     * @param path the path.
     * @return the filenames.
     */
    private String[] getAbsoluteFilename(String path) {
        List<String> filenames = new ArrayList<>();
        File file = new File(path);
        File[] array = file.listFiles();
        Optional.ofNullable(array)
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .filter(File::isFile)
                .map(File::getName)
                .forEach(filename -> filenames.add(filename));
        return filenames.toArray(new String[0]);
    }
}
