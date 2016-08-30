package com.microsoft.azure.keyvault.requests;

import com.microsoft.azure.keyvault.models.IssuerAttributes;
import com.microsoft.azure.keyvault.models.IssuerBundle;

/**
 * The set certificate issuer request class.
 */
public final class SetCertificateIssuerRequest {

    private final String vaultBaseUrl;
    private final String issuerName;
    private final IssuerBundle issuer;

    /**
     * The {@link SetCertificateIssuerRequest} builder.
     */
    public static class Builder {

        // Required parameters
        private final String vaultBaseUrl;
        private final String issuerName;

        // Optional parameters
        private IssuerBundle issuer;

        /**
         * The builder for constructing {@link SetCertificateIssuerRequest}
         * object.
         * 
         * @param vaultBaseUrl
         *            The vault name, e.g. https://myvault.vault.azure.net.
         * @param issuerName
         *            The name of the issuer.
         */
        public Builder(String vaultBaseUrl, String issuerName) {
            this.vaultBaseUrl = vaultBaseUrl;
            this.issuerName = issuerName;
        }

        /**
         * Set the issuer value.
         * 
         * @param issuer
         *            The issuer bundle.
         * @return the Builder object itself.
         */
        public Builder withIssuer(IssuerBundle issuer) {
            this.issuer = issuer;
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
        if (builder.issuer != null) {
            issuer = new IssuerBundle().withProvider(builder.issuer.provider())
                    .withOrganizationDetails(builder.issuer.organizationDetails())
                    .withCredentials(builder.issuer.credentials());
            if (builder.issuer.attributes() != null) {
                issuer.withAttributes(new IssuerAttributes().withEnabled(builder.issuer.attributes().enabled()));
            }
        } else {
            issuer = null;
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
     * @return the issuer
     */
    public IssuerBundle issuer() {
        return issuer;
    }

}
