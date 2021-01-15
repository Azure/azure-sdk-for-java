// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.authorization.fluent.models.MicrosoftGraphKeyCredentialInner;
import com.azure.resourcemanager.authorization.models.CertificateCredential;
import com.azure.resourcemanager.authorization.models.CertificateType;
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
    extends IndexableRefreshableWrapperImpl<CertificateCredential, MicrosoftGraphKeyCredentialInner>
    implements CertificateCredential, CertificateCredential.Definition<T> {

    private String name;
    private HasCredential<T> parent;
    private OutputStream authFile;
    private String privateKeyPath;
    private String privateKeyPassword;
    private final ClientLogger logger = new ClientLogger(CertificateCredentialImpl.class);

    CertificateCredentialImpl(MicrosoftGraphKeyCredentialInner keyCredential) {
        super(keyCredential);
        if (keyCredential.displayName() != null) {
            this.name = keyCredential.displayName();
        } else {
            this.name = keyCredential.keyId().toString();
        }
    }

    CertificateCredentialImpl(String name, HasCredential<T> parent) {
        super(
            new MicrosoftGraphKeyCredentialInner()
                .withUsage("Verify")
                .withDisplayName(name)
                .withStartDateTime(OffsetDateTime.now())
                .withEndDateTime(OffsetDateTime.now().plusYears(1)));
        this.name = name;
        this.parent = parent;
    }

    @Override
    public Mono<CertificateCredential> refreshAsync() {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Cannot refresh credentials."));
    }

    @Override
    protected Mono<MicrosoftGraphKeyCredentialInner> getInnerAsync() {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Cannot refresh credentials."));
    }

    @Override
    public OffsetDateTime startDate() {
        return innerModel().startDateTime();
    }

    @Override
    public OffsetDateTime endDate() {
        return innerModel().endDateTime();
    }

    @Override
    public String value() {
        return Base64.getEncoder().encodeToString(innerModel().key());
    }

    @Override
    public T attach() {
        return parent.withCertificateCredential(this);
    }

    @Override
    public CertificateCredentialImpl<T> withStartDate(OffsetDateTime startDate) {
        OffsetDateTime original = startDate();
        innerModel().withStartDateTime(startDate);
        // Adjust end time
        withDuration(Duration.between(original, endDate()));
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withDuration(Duration duration) {
        innerModel().withEndDateTime(startDate().plus(duration));
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
        innerModel().withKey(certificate);
        return this;
    }

    @Override
    public CertificateCredentialImpl<T> withSecretKey(byte[] secret) {
        innerModel().withKey(secret);
        return this;
    }

    void exportAuthFile(ServicePrincipalImpl servicePrincipal) {
        exportAuthFile(servicePrincipal.manager().environment(),
            servicePrincipal.applicationId(),
            servicePrincipal.manager().tenantId(),
            servicePrincipal.assignedSubscription);
    }

    void exportAuthFile(ActiveDirectoryApplicationImpl activeDirectoryApplication) {
        exportAuthFile(activeDirectoryApplication.manager().environment(),
            activeDirectoryApplication.applicationId(),
            activeDirectoryApplication.manager().tenantId(),
            null);
    }

    void exportAuthFile(AzureEnvironment environment, String clientId, String tenantId, String subscriptionId) {
        if (authFile == null) {
            return;
        }
        StringBuilder builder = new StringBuilder("{\n");
        builder
            .append("  ")
            .append(String.format("\"clientId\": \"%s\",", clientId))
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
            .append(String.format("\"tenantId\": \"%s\",", tenantId))
            .append("\n");
        builder
            .append("  ")
            .append(String.format("\"subscriptionId\": \"%s\",", subscriptionId))
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
            .append(String.format("\"%s\": \"%s\",",
                AzureEnvironment.Endpoint.MICROSOFT_GRAPH.identifier(), environment.getMicrosoftGraphEndpoint()))
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
        return innerModel().keyId().toString();
    }

    @Override
    public String name() {
        return this.name;
    }
}
