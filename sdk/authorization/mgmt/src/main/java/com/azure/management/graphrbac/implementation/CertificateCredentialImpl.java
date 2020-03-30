/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.graphrbac.implementation;

import com.azure.core.management.AzureEnvironment;
import com.azure.management.graphrbac.CertificateCredential;
import com.azure.management.graphrbac.CertificateType;
import com.azure.management.graphrbac.models.KeyCredentialInner;
import com.azure.management.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;

/**
 * Implementation for ServicePrincipal and its parent interfaces.
 */
class CertificateCredentialImpl<T>
        extends IndexableRefreshableWrapperImpl<CertificateCredential, KeyCredentialInner>
        implements
        CertificateCredential,
        CertificateCredential.Definition<T>,
        CertificateCredential.UpdateDefinition<T> {

    private String name;
    private HasCredential<?> parent;
    private OutputStream authFile;
    private String privateKeyPath;
    private String privateKeyPassword;

    CertificateCredentialImpl(KeyCredentialInner keyCredential) {
        super(keyCredential);
        if (keyCredential.getCustomKeyIdentifier() != null && !keyCredential.getCustomKeyIdentifier().isEmpty()) {
            this.name = new String(Base64.getDecoder().decode(keyCredential.getCustomKeyIdentifier()));
        } else {
            this.name = keyCredential.getKeyId();
        }
    }

    CertificateCredentialImpl(String name, HasCredential<?> parent) {
        super(new KeyCredentialInner()
                .setUsage("Verify")
                .setCustomKeyIdentifier(Base64.getEncoder().encodeToString(name.getBytes()))
                .setStartDate(OffsetDateTime.now())
                .setEndDate(OffsetDateTime.now().plusYears(1)));
        this.name = name;
        this.parent = parent;
    }

    @Override
    public Mono<CertificateCredential> refreshAsync() {
        throw new UnsupportedOperationException("Cannot refresh credentials.");
    }

    @Override
    protected Mono<KeyCredentialInner> getInnerAsync() {
        throw new UnsupportedOperationException("Cannot refresh credentials.");
    }

    @Override
    public OffsetDateTime startDate() {
        return inner().getStartDate();
    }

    @Override
    public OffsetDateTime endDate() {
        return inner().getEndDate();
    }

    @Override
    public String value() {
        return inner().getValue();
    }


    @Override
    @SuppressWarnings("unchecked")
    public T attach() {
        parent.withCertificateCredential(this);
        return (T) parent;
    }

    @Override
    public CertificateCredentialImpl<T> withStartDate(OffsetDateTime startDate) {
        OffsetDateTime original = startDate();
        inner().setStartDate(startDate);
        // Adjust end time
        withDuration(Duration.between(original, endDate()));
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withDuration(Duration duration) {
        inner().setEndDate(startDate().plus(duration));
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withAsymmetricX509Certificate() {
        inner().setType(CertificateType.ASYMMETRIC_X509_CERT.toString());
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withSymmetricEncryption() {
        inner().setType(CertificateType.SYMMETRIC.toString());
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withPublicKey(byte[] certificate) {
        inner().setValue(Base64.getEncoder().encodeToString(certificate));
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withSecretKey(byte[] secret) {
        inner().setValue(Base64.getEncoder().encodeToString(secret));
        return this;
    }

    void exportAuthFile(ServicePrincipalImpl servicePrincipal) {
        if (authFile == null) {
            return;
        }
        AzureEnvironment environment = AzureEnvironment.AZURE;
        StringBuilder builder = new StringBuilder("{\n");
        builder.append("  ").append(String.format("\"clientId\": \"%s\",", servicePrincipal.applicationId())).append("\n");
        builder.append("  ").append(String.format("\"clientCertificate\": \"%s\",", privateKeyPath.replace("\\", "\\\\"))).append("\n");
        builder.append("  ").append(String.format("\"clientCertificatePassword\": \"%s\",", privateKeyPassword)).append("\n");
        builder.append("  ").append(String.format("\"tenantId\": \"%s\",", servicePrincipal.manager().tenantId())).append("\n");
        builder.append("  ").append(String.format("\"subscriptionId\": \"%s\",", servicePrincipal.assignedSubscription)).append("\n");
        builder.append("  ").append(String.format("\"activeDirectoryEndpointUrl\": \"%s\",", environment.getActiveDirectoryEndpoint())).append("\n");
        builder.append("  ").append(String.format("\"resourceManagerEndpointUrl\": \"%s\",", environment.getResourceManagerEndpoint())).append("\n");
        builder.append("  ").append(String.format("\"activeDirectoryGraphResourceId\": \"%s\",", environment.getGraphEndpoint())).append("\n");
        builder.append("  ").append(String.format("\"managementEndpointUrl\": \"%s\"", environment.getManagementEndpoint())).append("\n");
        builder.append("}");
        try {
            authFile.write(builder.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CertificateCredentialImpl<T> withAuthFileToExport(OutputStream outputStream) {
        this.authFile = outputStream;
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withPrivateKeyFile(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withPrivateKeyPassword(String privateKeyPassword) {
        this.privateKeyPassword = privateKeyPassword;
        return this;
    }

    @Override
    public String id() {
        return inner().getKeyId();
    }

    @Override
    public String name() {
        return this.name;
    }
}
