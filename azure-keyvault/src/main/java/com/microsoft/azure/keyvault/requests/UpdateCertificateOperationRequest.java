package com.microsoft.azure.keyvault.requests;

/**
 * The update certificate operation request class.
 */
public final class UpdateCertificateOperationRequest {
    private final String vaultBaseUrl;
    private final String certificateName;
    private final Boolean cancellationRequested;

    /**
     * The {@link UpdateCertificateOperationRequest} builder.
     */
    public static class Builder {

        // Required parameters
        private final String vaultBaseUrl;
        private final String certificateName;
        private final Boolean cancellationRequested;

        // Optional parameters

        /**
         * The builder for constructing {@link CreateCertificateRequest} object.
         * 
         * @param vaultBaseUrl
         *            The vault name, e.g. https://myvault.vault.azure.net.
         * @param certificateName
         *            The name of the certificate in the given vault.
         * @param cancellationRequested
         *            Indicates if cancellation was requested on the certificate operation.
         */
        public Builder(String vaultBaseUrl, String certificateName, Boolean cancellationRequested) {
            this.vaultBaseUrl = vaultBaseUrl;
            this.certificateName = certificateName;
            this.cancellationRequested = cancellationRequested;
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
        cancellationRequested = builder.cancellationRequested;
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
    public Boolean cancellationRequested() {
        return cancellationRequested;
    }
}
