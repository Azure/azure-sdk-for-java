// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.certificates.implementation.CertificateOperationHelper;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A certificate operation is returned in case of long-running service requests.
 */
public final class CertificateOperation {
    private static final ClientLogger LOGGER = new ClientLogger(CertificateOperation.class);

    static {
        CertificateOperationHelper.setAccessor(CertificateOperation::new);
    }

    private final com.azure.security.keyvault.certificates.implementation.models.CertificateOperation impl;

    /**
     * Creates an instance of {@link CertificateOperation}.
     */
    public CertificateOperation() {
        impl = new com.azure.security.keyvault.certificates.implementation.models.CertificateOperation();
    }

    private CertificateOperation(
        com.azure.security.keyvault.certificates.implementation.models.CertificateOperation impl) {
        this.impl = impl;
        unpackId(impl.getId(), this);
    }

    /**
     * URL for the Azure KeyVault service.
     */
    private String vaultUrl;

    /**
     * The Certificate name.
     */
    private String name;

    /**
     * Get the identifier.
     *
     * @return the identifier.
     */
    public String getId() {
        return impl.getId();
    }

    /**
     * Get the issuer name.
     *
     * @return the issuer name
     */
    public String getIssuerName() {
        return impl.getIssuerParameters() == null ? null : impl.getIssuerParameters().getName();
    }

    /**
     * Get the certificate type.
     *
     * @return the certificateType
     */
    public String getCertificateType() {
        return impl.getIssuerParameters() == null ? null : impl.getIssuerParameters().getCertificateType();
    }

    /**
     * Get the certificate transparency status.
     *
     * @return the certificateTransparency status.
     */
    public boolean isCertificateTransparent() {
        return impl.getIssuerParameters() != null && impl.getIssuerParameters().isCertificateTransparency();
    }

    /**
     * Get the csr.
     *
     * @return the csr.
     */
    public byte[] getCsr() {
        return impl.getCsr();
    }

    /**
     * Get the cancellation requested status.
     *
     * @return the cancellationRequested status.
     */
    public Boolean getCancellationRequested() {
        return impl.isCancellationRequested();
    }

    /**
     * Get the status.
     *
     * @return the status
     */
    public String getStatus() {
        return impl.getStatus();
    }

    /**
     * Get the status details.
     *
     * @return the status details
     */
    public String getStatusDetails() {
        return impl.getStatusDetails();
    }

    /**
     * Get the error.
     *
     * @return the error
     */
    public CertificateOperationError getError() {
        return impl.getError();
    }

    /**
     * Get the target.
     *
     * @return the target
     */
    public String getTarget() {
        return impl.getTarget();
    }

    /**
     * Get the requestId.
     *
     * @return the requestId
     */
    public String getRequestId() {
        return impl.getRequestId();
    }

    /**
     * Get the URL for the Azure KeyVault service.
     *
     * @return the value of the URL for the Azure KeyVault service.
     */
    public String getVaultUrl() {
        return this.vaultUrl;
    }

    /**
     * Get the certificate name.
     *
     * @return the name of the certificate.
     */
    public String getName() {
        return this.name;
    }

    static void unpackId(String id, CertificateOperation operation) {
        if (id != null && id.length() > 0) {
            try {
                URL url = new URL(id);
                String[] tokens = url.getPath().split("/");
                operation.vaultUrl = (tokens.length >= 2 ? tokens[1] : null);
                operation.name = (tokens.length >= 3 ? tokens[2] : null);
            } catch (MalformedURLException e) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("The Azure Key Vault endpoint url is malformed.", e));
            }
        }
    }
}
