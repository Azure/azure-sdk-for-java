// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.authorization.models.CertificateCredential;
import com.azure.resourcemanager.authorization.models.CertificateType;
import com.azure.resourcemanager.authorization.fluent.models.KeyCredentialInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;

/** Implementation for ServicePrincipal and its parent interfaces. */
class CertificateCredentialImpl<T extends HasCredential<T>>
    extends IndexableRefreshableWrapperImpl<CertificateCredential, KeyCredentialInner>
    implements CertificateCredential, CertificateCredential.Definition<T>, CertificateCredential.UpdateDefinition<T> {

    private String name;
    private HasCredential<T> parent;
    private OutputStream authFile;
    private String privateKeyPath;
    private String privateKeyPassword;
    private final ClientLogger logger = new ClientLogger(CertificateCredentialImpl.class);

    CertificateCredentialImpl(KeyCredentialInner keyCredential) {
        super(keyCredential);
        if (keyCredential.customKeyIdentifier() != null && !keyCredential.customKeyIdentifier().isEmpty()) {
            this.name = new String(Base64.getMimeDecoder().decode(keyCredential.customKeyIdentifier()),
                StandardCharsets.UTF_8);
        } else {
            this.name = keyCredential.keyId();
        }
    }

    CertificateCredentialImpl(String name, HasCredential<T> parent) {
        super(
            new KeyCredentialInner()
                .withUsage("Verify")
                .withCustomKeyIdentifier(Base64.getEncoder().encodeToString(name.getBytes(StandardCharsets.UTF_8)))
                .withStartDate(OffsetDateTime.now())
                .withEndDate(OffsetDateTime.now().plusYears(1)));
        this.name = name;
        this.parent = parent;
    }

    @Override
    public Mono<CertificateCredential> refreshAsync() {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Cannot refresh credentials."));
    }

    @Override
    protected Mono<KeyCredentialInner> getInnerAsync() {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Cannot refresh credentials."));
    }

    @Override
    public OffsetDateTime startDate() {
        return innerModel().startDate();
    }

    @Override
    public OffsetDateTime endDate() {
        return innerModel().endDate();
    }

    @Override
    public String value() {
        return innerModel().value();
    }

    @Override
    public T attach() {
        return parent.withCertificateCredential(this);
    }

    @Override
    public CertificateCredentialImpl<T> withStartDate(OffsetDateTime startDate) {
        OffsetDateTime original = startDate();
        innerModel().withStartDate(startDate);
        // Adjust end time
        withDuration(Duration.between(original, endDate()));
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withDuration(Duration duration) {
        innerModel().withEndDate(startDate().plus(duration));
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withAsymmetricX509Certificate() {
        innerModel().withType(CertificateType.ASYMMETRIC_X509_CERT.toString());
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withSymmetricEncryption() {
        innerModel().withType(CertificateType.SYMMETRIC.toString());
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withPublicKey(byte[] certificate) {
        innerModel().withValue(Base64.getEncoder().encodeToString(certificate));
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withSecretKey(byte[] secret) {
        innerModel().withValue(Base64.getEncoder().encodeToString(secret));
        return this;
    }

    void exportAuthFile(ServicePrincipalImpl servicePrincipal) {
        if (authFile == null) {
            return;
        }
        AzureEnvironment environment = AzureEnvironment.AZURE;
        StringBuilder builder = new StringBuilder("{\n");
        builder
            .append("  ")
            .append(String.format("\"clientId\": \"%s\",", servicePrincipal.applicationId()))
            .append("\n");
        builder
            .append("  ")
            .append(String.format("\"clientCertificate\": \"%s\",", privateKeyPath.replace("\\", "\\\\")))
            .append("\n");
        builder
            .append("  ")
            .append(String.format("\"clientCertificatePassword\": \"%s\",", privateKeyPassword))
            .append("\n");
        builder
            .append("  ")
            .append(String.format("\"tenantId\": \"%s\",", servicePrincipal.manager().tenantId()))
            .append("\n");
        builder
            .append("  ")
            .append(String.format("\"subscriptionId\": \"%s\",", servicePrincipal.assignedSubscription))
            .append("\n");
        builder
            .append("  ")
            .append(String.format("\"activeDirectoryEndpointUrl\": \"%s\",", environment.getActiveDirectoryEndpoint()))
            .append("\n");
        builder
            .append("  ")
            .append(String.format("\"resourceManagerEndpointUrl\": \"%s\",", environment.getResourceManagerEndpoint()))
            .append("\n");
        builder
            .append("  ")
            .append(String.format("\"activeDirectoryGraphResourceId\": \"%s\",", environment.getGraphEndpoint()))
            .append("\n");
        builder
            .append("  ")
            .append(String.format("\"managementEndpointUrl\": \"%s\"", environment.getManagementEndpoint()))
            .append("\n");
        builder.append("}");
        try {
            authFile.write(builder.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
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
        return innerModel().keyId();
    }

    @Override
    public String name() {
        return this.name;
    }
}
