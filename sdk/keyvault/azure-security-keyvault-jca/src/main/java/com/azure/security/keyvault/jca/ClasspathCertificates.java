package com.azure.security.keyvault.jca;

import java.security.Key;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClasspathCertificates {

    /**
     * Stores the side load aliases.
     */
    private final List<String> aliases = new ArrayList<>();

    /**
     * Stores the sideLoad certificates by alias.
     */
    private final Map<String, Certificate> certificates = new HashMap<>();

    /**
     * Stores the certificate keys by alias.
     */
    private final HashMap<String, Key> certificateKeys = new HashMap<>();

    public List<String> getAliases() {
        return aliases;
    }

    public Map<String, Certificate> getCertificates() {
        return certificates;
    }

    public HashMap<String, Key> getCertificateKeys() {
        return certificateKeys;
    }

    public void removeAlias(String alias) {
        aliases.remove(alias);
    }
    public void removeCertificate(String alias) {
        certificates.remove(alias);
    }
    public void removeCertificateKey(String alias) {
        certificateKeys.remove(alias);
    }

    public void setCertificateEntry(String alias, Certificate certificate) {
        if (!alias.contains(alias)) {
            aliases.add(alias);
            certificates.put(alias, certificate);
        }
    }
}
