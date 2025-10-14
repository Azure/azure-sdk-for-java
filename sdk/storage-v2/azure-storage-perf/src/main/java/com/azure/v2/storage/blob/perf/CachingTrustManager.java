// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.storage.blob.perf;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ConcurrentHashMap;

public class CachingTrustManager implements X509TrustManager {
    private final X509TrustManager defaultTrustManager;
    private final ConcurrentHashMap<String, Boolean> cache = new ConcurrentHashMap<>();

    public CachingTrustManager(X509TrustManager defaultTrustManager) {
        this.defaultTrustManager = defaultTrustManager;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        defaultTrustManager.checkClientTrusted(chain, authType); // Delegate to default
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        String fingerprint = getFingerprint(chain[0]);

        if (cache.containsKey(fingerprint)) {
            if (cache.get(fingerprint)) {
                return;
            } else {
                throw new CertificateException("Cached validation failure for certificate.");
            }
        }

        try {
            defaultTrustManager.checkServerTrusted(chain, authType);
            cache.put(fingerprint, true);
        } catch (CertificateException e) {
            cache.put(fingerprint, false);
            throw e;
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return defaultTrustManager.getAcceptedIssuers();
    }

    private String getFingerprint(X509Certificate cert) throws CertificateException {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] fingerprintBytes = md.digest(cert.getEncoded());
            return java.util.Base64.getEncoder().encodeToString(fingerprintBytes);
        } catch (Exception e) {
            throw new CertificateException("Failed to compute certificate fingerprint.", e);
        }
    }
}
