// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.certificates.implementation.IssuerPropertiesHelper;
import com.azure.security.keyvault.certificates.implementation.models.CertificateIssuerItem;

import static com.azure.security.keyvault.certificates.implementation.CertificatesUtils.getIdMetadata;

/**
 * Represents base properties of an {@link CertificateIssuer}.
 */
public class IssuerProperties {
    private static final ClientLogger LOGGER = new ClientLogger(IssuerProperties.class);

    static {
        IssuerPropertiesHelper.setAccessor(IssuerProperties::new);
    }

    private final CertificateIssuerItem impl;

    /**
     * Name of the referenced issuer object or reserved names; for example,
     * 'Self' or 'Unknown'.
     */
    private final String name;

    /**
     * Creates an instance of {@link IssuerProperties}.
     */
    public IssuerProperties() {
        this(new CertificateIssuerItem());
    }

    private IssuerProperties(CertificateIssuerItem impl) {
        this.impl = impl;
        this.name = getIdMetadata(impl.getId(), -1, 3, -1, LOGGER).getName();
    }

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
}
