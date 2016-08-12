package com.microsoft.azure.keyvault.requests;

import com.microsoft.azure.keyvault.models.CertificateOperation;

/**
 * The update certificate operation request class.
 */
public final class UpdateCertificateOperationRequest {
    private final String vaultBaseUrl;
    private final String certificateName;
    private final CertificateOperation certificateOperation;

    /**
     * The {@link UpdateCertificateOperationRequest} builder.
     */
    public static class Builder {

        // Required parameters
        private final String vaultBaseUrl;
        private final String certificateName;

        // Optional parameters
        private Boolean cancellationRequested;

        /**
         * The builder for constructing {@link CreateCertificateRequest} object.
         * 
         * @param vaultBaseUrl
         *            The vault name, e.g. https://myvault.vault.azure.net.
         * @param certificateName
         *            The name of the certificate in the given vault.
         */
        public Builder(String vaultBaseUrl, String certificateName) {
            this.vaultBaseUrl = vaultBaseUrl;
            this.certificateName = certificateName;
        }

        /**
         * Set the cancellationRequested value.
         *
         * @param cancellationRequested
         *            Indicates if cancellation was requested on the certificate
         *            operation.
         * @return the Builder object itself.
         */
        public Builder withCancellationRequested(Boolean cancellationRequested) {
            this.cancellationRequested = cancellationRequested;
            return this;
        }

        /**
         * builds the {@link UpdateCertificateOperationRequest} object.
         * 
         * @return the {@link UpdateCertificateOperationRequest} object.
         */
        public UpdateCertificateOperationRequest build() {
            return new UpdateCertificateOperationRequest(this);
        }
    }

    private UpdateCertificateOperationRequest(Builder builder) {
        vaultBaseUrl = builder.vaultBaseUrl;
        certificateName = builder.certificateName;
        if (builder.cancellationRequested != null) {
            certificateOperation = new CertificateOperation().withCancellationRequested(builder.cancellationRequested);
        } else {
            certificateOperation = null;
        }
    }

    /**
     * @return the vault base url
     */
    public String vaultBaseUrl() {
        return vaultBaseUrl;
    }

    /**
     * @return the certificate name
     */
    public String certificateName() {
        return certificateName;
    }

    /**
     * @return the certificate policy
     */
    public CertificateOperation certificateOperation() {
        return certificateOperation;
    }
}
