// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.azure.security.keyvault.certificates.implementation.CertificateIssuerHelper;
import com.azure.security.keyvault.certificates.implementation.models.IssuerAttributes;
import com.azure.security.keyvault.certificates.implementation.models.IssuerBundle;
import com.azure.security.keyvault.certificates.implementation.models.IssuerCredentials;
import com.azure.security.keyvault.certificates.implementation.models.OrganizationDetails;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static com.azure.security.keyvault.certificates.implementation.CertificatesUtils.getIdMetadata;

/**
 * Represents certificate Issuer with all of its properties.
 */
public final class CertificateIssuer implements JsonSerializable<CertificateIssuer> {
    private static final ClientLogger LOGGER = new ClientLogger(CertificateIssuer.class);

    static {
        CertificateIssuerHelper.setAccessor(new CertificateIssuerHelper.CertificateIssuerAccessor() {
            @Override
            public CertificateIssuer createCertificateIssuer(IssuerBundle issuerBundle) {
                return new CertificateIssuer(issuerBundle);
            }

            @Override
            public IssuerBundle getIssuerBundle(CertificateIssuer certificateIssuer) {
                return certificateIssuer.issuerBundle;
            }
        });
    }

    private final IssuerBundle issuerBundle;

    /**
     * Name of the referenced issuer object or reserved names; for example,
     * 'Self' or 'Unknown'.
     */
    private final String name;

    /**
     * Creates an instance of the issuer.
     *
     * @param name The name of the issuer.
     * @param provider The provider of the issuer.
     */
    public CertificateIssuer(String name, String provider) {
        this.name = name;
        this.issuerBundle = new IssuerBundle().setProvider(provider);
    }

    /**
     * Creates an instance of the issuer.
     *
     * @param name The name of the issuer.
     */
    public CertificateIssuer(String name) {
        this(name, null);
    }

    private CertificateIssuer(IssuerBundle issuerBundle) {
        this.issuerBundle = issuerBundle;
        this.name = getIdMetadata(issuerBundle.getId(), -1, 3, -1, LOGGER).getName();
    }

    /**
     * Get the id of the issuer.
     * @return the identifier.
     */
    public String getId() {
        return issuerBundle.getId();
    }

    /**
     * Get the issuer provider
     * @return the issuer provider
     */
    public String getProvider() {
        return issuerBundle.getProvider();
    }

    /**
     * Get the issuer name
     * @return the issuer name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the account id of the issuer.
     * @return the account id
     */
    public String getAccountId() {
        return issuerBundle.getCredentials() == null ? null : issuerBundle.getCredentials().getAccountId();
    }

    /**
     * Set the account id of the issuer.
     * @param accountId the account id to set.
     * @return the Issuer object itself.
     */
    public CertificateIssuer setAccountId(String accountId) {
        if (issuerBundle.getCredentials() == null) {
            issuerBundle.setCredentials(new IssuerCredentials());
        }

        issuerBundle.getCredentials().setAccountId(accountId);
        return this;
    }

    /**
     * Get the password of the issuer.
     * @return the password
     */
    public String getPassword() {
        return issuerBundle.getCredentials() == null ? null : issuerBundle.getCredentials().getPassword();
    }

    /**
     * Set the password id of the issuer.
     * @param password the password set.
     * @return the Issuer object itself.
     */
    public CertificateIssuer setPassword(String password) {
        if (issuerBundle.getCredentials() == null) {
            issuerBundle.setCredentials(new IssuerCredentials());
        }

        issuerBundle.getCredentials().setPassword(password);
        return this;
    }

    /**
     * Get the organization id of the issuer.
     * @return the organization id
     */
    public String getOrganizationId() {
        return issuerBundle.getOrganizationDetails() == null ? null : issuerBundle.getOrganizationDetails().getId();
    }

    /**
     * Set the organization id of the issuer.
     * @param organizationId the org id to set.
     * @return the Issuer object itself.
     */
    public CertificateIssuer setOrganizationId(String organizationId) {
        if (issuerBundle.getOrganizationDetails() == null) {
            issuerBundle.setOrganizationDetails(new OrganizationDetails());
        }

        issuerBundle.getOrganizationDetails().setId(organizationId);
        return this;
    }

    /**
     * Get the administrators of the issuer.
     * @return the administrators
     */
    public List<AdministratorContact> getAdministratorContacts() {
        return issuerBundle.getOrganizationDetails() == null
            ? null
            : issuerBundle.getOrganizationDetails().getAdminDetails();
    }

    /**
     * Set the administrators of the issuer.
     * @param administratorContacts the administrators to set.
     * @return the Issuer object itself.
     */
    public CertificateIssuer setAdministratorContacts(List<AdministratorContact> administratorContacts) {
        if (issuerBundle.getOrganizationDetails() == null) {
            issuerBundle.setOrganizationDetails(new OrganizationDetails());
        }

        issuerBundle.getOrganizationDetails().setAdminDetails(administratorContacts);
        return this;
    }

    /**
     * Get the enabled status
     * @return the enabled status
     */
    public Boolean isEnabled() {
        return issuerBundle.getAttributes() == null ? null : issuerBundle.getAttributes().isEnabled();
    }

    /**
     * Set the enabled status
     * @param enabled the enabled status to set
     * @return the Issuer object itself.
     */
    public CertificateIssuer setEnabled(Boolean enabled) {
        if (issuerBundle.getAttributes() == null) {
            issuerBundle.setAttributes(new IssuerAttributes());
        }

        issuerBundle.getAttributes().setEnabled(enabled);
        return this;
    }

    /**
     * Get the created UTC time.
     * @return the created UTC time.
     */
    public OffsetDateTime getCreatedOn() {
        return issuerBundle.getAttributes() == null ? null : issuerBundle.getAttributes().getCreated();
    }

    /**
     * Get the updated UTC time.
     * @return the updated UTC time.
     */
    public OffsetDateTime getUpdatedOn() {
        return issuerBundle.getAttributes() == null ? null : issuerBundle.getAttributes().getUpdated();
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return issuerBundle.toJson(jsonWriter);
    }

    /**
     * Reads a JSON stream into a {@link CertificateIssuer}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link CertificateIssuer} that the JSON stream represented, may return null.
     * @throws IOException If a {@link CertificateIssuer} fails to be read from the {@code jsonReader}.
     */
    public static CertificateIssuer fromJson(JsonReader jsonReader) throws IOException {
        return new CertificateIssuer(IssuerBundle.fromJson(jsonReader));
    }
}
