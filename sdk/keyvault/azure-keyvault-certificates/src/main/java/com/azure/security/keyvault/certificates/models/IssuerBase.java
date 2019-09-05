// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * Represents base properties of an {@link Issuer}.
 */
public class IssuerBase {
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
    private String name;

    /**
     * Determines whether the issuer is enabled.
     */
    @JsonProperty(value = "enabled")
    private Boolean enabled;

    /**
     * The created UTC time.
     */
    private OffsetDateTime created;

    /**
     * The updated UTC time.
     */
    private OffsetDateTime updated;

    public IssuerBase(String name, String provider) {
        this.name = name;
        this.provider = provider;
    }

    IssuerBase() {

    }

    /**
     * Get the id of the issuer.
     * @return the identifier.
     */
    public String id() {
        return id;
    }

    /**
     * Set the issuer identifier
     * @param id The issuer identifier
     */
    public void id(String id) {
        this.id = id;
    }

    /**
     * Get the issuer provider
     * @return the issuer provider
     */
    public String provider() {
        return provider;
    }

    /**
     * Get the issuer name
     * @return the issuer name
     */
    public String name() {
        return name;
    }

    /**
     * Get the enabled status
     * @return the enabled status
     */
    public Boolean enabled() {
        return enabled;
    }

    /**
     * Set the enabled status
     * @param enabled the enabled status to set
     */
    public void enabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get tje created UTC time.
     * @return the created UTC time.
     */
    public OffsetDateTime created() {
        return created;
    }

    /**
     * Get the updated UTC time.
     * @return the updated UTC time.
     */
    public OffsetDateTime updated() {
        return updated;
    }

    @JsonProperty("attributes")
    private void unpackBaseAttributes(Map<String, Object> attributes) {
        this.enabled = (Boolean) attributes.get("enabled");
        this.created = epochToOffsetDateTime(attributes.get("created"));
        this.updated = epochToOffsetDateTime(attributes.get("updated"));
    }

    private OffsetDateTime epochToOffsetDateTime(Object epochValue) {
        if (epochValue != null) {
            Instant instant = Instant.ofEpochMilli(((Number) epochValue).longValue() * 1000L);
            return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
        return null;
    }

    @JsonProperty(value = "id")
    private void unpackId(String id) {
        if (id != null && id.length() > 0) {
            this.id = id;
            try {
                URL url = new URL(id);
                String[] tokens = url.getPath().split("/");
                this.name = (tokens.length >= 3 ? tokens[2] : null);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
