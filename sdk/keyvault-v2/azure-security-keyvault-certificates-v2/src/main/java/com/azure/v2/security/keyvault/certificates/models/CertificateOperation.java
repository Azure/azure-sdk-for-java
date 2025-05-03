// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.certificates.models;

import com.azure.v2.security.keyvault.certificates.implementation.CertificateOperationHelper;
import com.azure.v2.security.keyvault.certificates.implementation.IdMetadata;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;

import static com.azure.v2.security.keyvault.certificates.implementation.CertificatesUtils.getIdMetadata;

/**
 * A certificate operation is returned in case of long-running service requests.
 */
public final class CertificateOperation implements JsonSerializable<CertificateOperation> {
    private static final ClientLogger LOGGER = new ClientLogger(CertificateOperation.class);

    static {
        CertificateOperationHelper.setAccessor(CertificateOperation::new);
    }

    private final com.azure.v2.security.keyvault.certificates.implementation.models.CertificateOperation impl;

    /**
     * Creates an instance of {@link CertificateOperation}.
     */
    public CertificateOperation() {
        this(new com.azure.v2.security.keyvault.certificates.implementation.models.CertificateOperation());
    }

    private CertificateOperation(
        com.azure.v2.security.keyvault.certificates.implementation.models.CertificateOperation impl) {
        this.impl = impl;
        IdMetadata idMetadata = getIdMetadata(impl.getId(), 1, 2, -1, LOGGER);

        this.vaultUrl = idMetadata.getVaultUrl();
        this.name = idMetadata.getName();
    }

    /**
     * URL for the Azure KeyVault service.
     */
    private final String vaultUrl;

    /**
     * The Certificate name.
     */
    private final String name;

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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return impl.toJson(jsonWriter);
    }

    /**
     * Reads a JSON stream into a {@link CertificateOperation}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link CertificateOperation} that the JSON stream represented, may return null.
     * @throws IOException If a {@link CertificateOperation} fails to be read from the {@code jsonReader}.
     */
    public static CertificateOperation fromJson(JsonReader jsonReader) throws IOException {
        return new CertificateOperation(
            com.azure.v2.security.keyvault.certificates.implementation.models.CertificateOperation
                .fromJson(jsonReader));
    }
}
