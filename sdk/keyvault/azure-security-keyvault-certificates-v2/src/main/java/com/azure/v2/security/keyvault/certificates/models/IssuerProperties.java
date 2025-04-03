// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.certificates.models;

import com.azure.v2.security.keyvault.certificates.implementation.IssuerPropertiesHelper;
import com.azure.v2.security.keyvault.certificates.implementation.models.CertificateIssuerItem;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;

import static com.azure.v2.security.keyvault.certificates.implementation.CertificatesUtils.getIdMetadata;

/**
 * Represents base properties of an {@link CertificateIssuer}.
 */
public class IssuerProperties implements JsonSerializable<IssuerProperties> {
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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return impl.toJson(jsonWriter);
    }

    /**
     * Reads a JSON stream into a {@link IssuerProperties}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link IssuerProperties} that the JSON stream represented, may return null.
     * @throws IOException If a {@link IssuerProperties} fails to be read from the {@code jsonReader}.
     */
    public static IssuerProperties fromJson(JsonReader jsonReader) throws IOException {
        return new IssuerProperties(CertificateIssuerItem.fromJson(jsonReader));
    }
}
