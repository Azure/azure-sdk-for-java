package com.microsoft.azure.keyvault.requests;

import java.util.ArrayList;

import com.microsoft.azure.keyvault.models.AdministratorDetails;
import com.microsoft.azure.keyvault.models.IssuerAttributes;
import com.microsoft.azure.keyvault.models.IssuerCredentials;
import com.microsoft.azure.keyvault.models.OrganizationDetails;

/**
 * The set certificate issuer request class.
 */
public final class SetCertificateIssuerRequest {

    private final String vaultBaseUrl;
    private final String issuerName;
    private final String provider;
    private final IssuerCredentials credentials;
    private final OrganizationDetails organizationDetails;
    private final IssuerAttributes attributes;

    /**
     * The {@link SetCertificateIssuerRequest} builder.
     */
    public static class Builder {

        // Required parameters
        private final String vaultBaseUrl;
        private final String issuerName;
        private final String provider;

        // Optional parameters
        private IssuerCredentials credentials;
        private OrganizationDetails organizationDetails;
        private IssuerAttributes attributes;

        /**
         * The builder for constructing {@link SetCertificateIssuerRequest}
         * object.
         * 
         * @param vaultBaseUrl
         *            The vault name, e.g. https://myvault.vault.azure.net.
         * @param issuerName
         *            The name of the issuer.
         * @param provider The name of the issuer.
         */
        public Builder(String vaultBaseUrl, String issuerName, String provider) {
            this.vaultBaseUrl = vaultBaseUrl;
            this.issuerName = issuerName;
            this.provider = provider;
        }

        /**
         * Set issuer credentials.
         * 
         * @param credentials
         *            The issuer credentials.
         * @return the Builder object itself.
         */
        public Builder withCredentials(IssuerCredentials credentials) {
            this.credentials = credentials;
            return this;
        }
        
        /**
         * Set issuer organization details.
         * 
         * @param organizationDetails
         *            The issuer organization details.
         * @return the Builder object itself.
         */
        public Builder withOrganizationDetails(OrganizationDetails organizationDetails) {
            this.organizationDetails = organizationDetails;
            return this;
        }
        
        /**
         * Set issuer attributes.
         * 
         * @param attributes
         *            The issuer attributes.
         * @return the Builder object itself.
         */
        public Builder withAttributes(IssuerAttributes attributes) {
            this.attributes = attributes;
            return this;
        }
        
        /**
         * builds the {@link SetCertificateIssuerRequest} object.
         * 
         * @return the {@link SetCertificateIssuerRequest} object.
         */
        public SetCertificateIssuerRequest build() {
            return new SetCertificateIssuerRequest(this);
        }
    }

    private SetCertificateIssuerRequest(Builder builder) {
        vaultBaseUrl = builder.vaultBaseUrl;
        issuerName = builder.issuerName;
        provider = builder.provider;
        if (builder.organizationDetails != null) {
            organizationDetails = new OrganizationDetails()
                .withId(builder.organizationDetails.id())
                .withAdminDetails(new ArrayList<AdministratorDetails>(builder.organizationDetails.adminDetails()));
        } else {
            organizationDetails = null;
        }
        if (builder.credentials != null) {
            credentials = new IssuerCredentials()
                .withAccountId(builder.credentials.accountId())
                .withPassword(builder.credentials.password());
        } else {
            credentials = null;
        }
        if (builder.attributes != null) {
            attributes = new IssuerAttributes().withEnabled(builder.attributes.enabled());
        } else {
            attributes = null;
        }
    }

    /**
     * @return the vault base url
     */
    public String vaultBaseUrl() {
        return vaultBaseUrl;
    }

    /**
     * @return the issuer name
     */
    public String issuerName() {
        return issuerName;
    }

    /**
     * @return the issuer provider name
     */
    public String provider() {
        return provider;
    }
    
    /**
     * @return the issuer credentials
     */
    public IssuerCredentials credentials() {
        return credentials;
    }
    
    /**
     * @return the organization details
     */
    public OrganizationDetails organizationDetails() {
        return organizationDetails;
    }
    
    /**
     * @return the issuer attributes
     */
    public IssuerAttributes attributes() {
        return attributes;
    }
}
