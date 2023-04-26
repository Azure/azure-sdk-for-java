// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * A certificate operation is returned in case of long running service requests.
 */
public final class CertificateOperation {
    private static final ClientLogger LOGGER = new ClientLogger(CertificateOperation.class);

    /**
     * URL for the Azure KeyVault service.
     */
    private String vaultUrl;

    /**
     * The Certificate name.
     */
    private String name;

    /**
     * The certificate id.
     */
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    /**
     * Name of the referenced issuer object or reserved names; for example,
     * 'Self' or 'Unknown'.
     */
    private String issuerName;

    /**
     * Type of certificate to be requested from the issuer provider.
     */
    private String certificateType;

    /**
     * Indicates if the certificates generated under this policy should be
     * published to certificate transparency logs.
     */
    private boolean certificateTransparency;

    /**
     * The certificate signing request (CSR) that is being used in the
     * certificate operation.
     */
    @JsonProperty(value = "csr")
    private byte[] csr;

    /**
     * Indicates if cancellation was requested on the certificate operation.
     */
    @JsonProperty(value = "cancellation_requested")
    private Boolean cancellationRequested;

    /**
     * Status of the certificate operation.
     */
    @JsonProperty(value = "status")
    private String status;

    /**
     * The status details of the certificate operation.
     */
    @JsonProperty(value = "status_details")
    private String statusDetails;

    /**
     * Error encountered, if any, during the certificate operation.
     */
    @JsonProperty(value = "error")
    private CertificateOperationError error;

    /**
     * Location which contains the result of the certificate operation.
     */
    @JsonProperty(value = "target")
    private String target;

    /**
     * Identifier for the certificate operation.
     */
    @JsonProperty(value = "request_id")
    private String requestId;

    /**
     * Get the identifier.
     *
     * @return the identifier.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the issuer name.
     *
     * @return the issuer name
     */
    public String getIssuerName() {
        return this.issuerName;
    }

    /**
     * Get the certificate type.
     *
     * @return the certificateType
     */
    public String getCertificateType() {
        return this.certificateType;
    }

    /**
     * Get the certificate transparency status.
     *
     * @return the certificateTransparency status.
     */
    public boolean isCertificateTransparent() {
        return this.certificateTransparency;
    }

    /**
     * Get the csr.
     *
     * @return the csr.
     */
    public byte[] getCsr() {
        return CoreUtils.clone(this.csr);
    }

    /**
     * Get the cancellation requested status.
     *
     * @return the cancellationRequested status.
     */
    public Boolean getCancellationRequested() {
        return this.cancellationRequested;
    }

    /**
     * Get the status.
     *
     * @return the status
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * Get the status details.
     *
     * @return the status details
     */
    public String getStatusDetails() {
        return this.statusDetails;
    }

    /**
     * Get the error.
     *
     * @return the error
     */
    public CertificateOperationError getError() {
        return this.error;
    }

    /**
     * Get the target.
     *
     * @return the target
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * Get the requestId.
     *
     * @return the requestId
     */
    public String getRequestId() {
        return this.requestId;
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

    @JsonProperty("issuer")
    private void unpackIssuerParameters(Map<String, Object> issuerParameters) {
        issuerName = (String) issuerParameters.get("name");
        certificateType =  (String) issuerParameters.get("cty");
        certificateTransparency = issuerParameters.get("cert_transparency") != null ? (Boolean) issuerParameters.get("cert_transparency") : false;
    }

    @JsonProperty(value = "id")
    void unpackId(String id) {
        if (id != null && id.length() > 0) {
            this.id = id;
            try {
                URL url = new URL(id);
                String[] tokens = url.getPath().split("/");
                this.vaultUrl = (tokens.length >= 2 ? tokens[1] : null);
                this.name = (tokens.length >= 3 ? tokens[2] : null);
            } catch (MalformedURLException e) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("The Azure Key Vault endpoint url is malformed.", e));
            }
        }
    }
}
