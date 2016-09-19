package com.microsoft.azure.keyvault.requests;

import java.util.ArrayList;

import com.microsoft.azure.keyvault.models.AdministratorDetails;
import com.microsoft.azure.keyvault.models.IssuerAttributes;
import com.microsoft.azure.keyvault.models.IssuerCredentials;
import com.microsoft.azure.keyvault.models.OrganizationDetails;

/**
 * The update certificate issuer request class.
 */
public final class UpdateCertificateIssuerRequest {
    private final String vaultBaseUrl;
    private final String issuerName;
    private final String provider;
    private final IssuerCredentials credentials;
    private final OrganizationDetails organizationDetails;
    private final IssuerAttributes attributes;

    /**
     * The {@link UpdateCertificateIssuerRequest} builder.
     */
    public static class Builder {

        // Required parameters
        private final String vaultBaseUrl;
        private final String issuerName;

        // Optional parameters
        private String provider;
        private IssuerCredentials credentials;
        private OrganizationDetails organizationDetails;
        private IssuerAttributes attributes;

        /**
         * The builder for constructing {@link UpdateCertificateIssuerRequest}
         * object.
         * 
         * @param vaultBaseUrl
         *            The vault name, e.g. https://myvault.vault.azure.net.
         * @param issuerName
         *            The name of the issuer in the given vault.
         */
        public Builder(String vaultBaseUrl, String issuerName) {
            this.vaultBaseUrl = vaultBaseUrl;
            this.issuerName = issuerName;
        }

        /**
         * Set issuer credentials.
         * 
         * @param provider
         *            The issuer provider.
         * @return the Builder object itself.
         */
        public Builder withProvider(String provider) {
            this.provider = provider;
            return this;
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
         * builds the {@link UpdateCertificateIssuerRequest} object.
         * 
         * @return the {@link UpdateCertificateIssuerRequest} object.
         */
        public UpdateCertificateIssuerRequest build() {
            return new UpdateCertificateIssuerRequest(this);
        }
    }

    private UpdateCertificateIssuerRequest(Builder builder) {
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
