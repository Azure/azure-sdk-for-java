// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Represents base properties of an {@link CertificateIssuer}.
 */
public class IssuerProperties {
    /**
     * The issuer id.
     */
    @JsonProperty(value = "id")
    private String id;

    /**
     * The issuer provider.
     */
    @JsonProperty(value = "provider")
    private String provider;

    /**
     * Name of the referenced issuer object or reserved names; for example,
     * 'Self' or 'Unknown'.
     */
    @JsonProperty(value = "name")
    String name;

    /**
     * Creates a new IssuerProperties instance.
     *
     * @param name Name of the referenced issuer object or reserved names; for example, 'Self' or 'Unknown'.
     * @param provider The issuer provider.
     */
    IssuerProperties(String name, String provider) {
        this.name = name;
        this.provider = provider;
    }

    IssuerProperties() { }

    /**
     * Get the id of the issuer.
     * @return the identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Get the issuer provider
     * @return the issuer provider
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Get the issuer name
     * @return the issuer name
     */
    public String getName() {
        return name;
    }

    @JsonProperty(value = "id")
    void unpackId(String id) {
        if (id != null && id.length() > 0) {
            this.id = id;
            try {
                URL url = new URL(id);
                String[] tokens = url.getPath().split("/");
                this.name = (tokens.length >= 4 ? tokens[3] : null);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
