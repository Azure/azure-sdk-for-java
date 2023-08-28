// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.util.CoreUtils;
import com.azure.security.keyvault.certificates.implementation.models.CertificateIssuerItem;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Represents base properties of an {@link CertificateIssuer}.
 */
public class IssuerProperties {
    private final CertificateIssuerItem impl;

    /**
     * Creates an instance of {@link IssuerProperties}.
     */
    public IssuerProperties() {
        this.impl = new CertificateIssuerItem();
    }

    private IssuerProperties(CertificateIssuerItem impl) {
        this.impl = impl;
    }

    /**
     * Name of the referenced issuer object or reserved names; for example,
     * 'Self' or 'Unknown'.
     */
    String name;

    /**
     * Get the id of the issuer.
     * @return the identifier.
     */
    public String getId() {
        return impl.getId();
    }

    /**
     * Get the issuer provider
     * @return the issuer provider
     */
    public String getProvider() {
        return impl.getProvider();
    }

    /**
     * Get the issuer name
     * @return the issuer name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the issuer provider
     * @param provider The issuer provider.
     * @return the updated IssuerProperties object
     */
    public IssuerProperties setProvider(String provider) {
        impl.setProvider(provider);
        return this;
    }

    static void unpackId(String id, IssuerProperties properties) {
        if (CoreUtils.isNullOrEmpty(id)) {
            try {
                URL url = new URL(id);
                String[] tokens = url.getPath().split("/");
                properties.name = (tokens.length >= 4 ? tokens[3] : null);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
