// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.authorization.models.PasswordCredential;
import com.azure.resourcemanager.authorization.fluent.models.PasswordCredentialInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;

/** Implementation for ServicePrincipal and its parent interfaces. */
class PasswordCredentialImpl<T extends HasCredential<T>>
    extends IndexableRefreshableWrapperImpl<PasswordCredential, PasswordCredentialInner>
    implements PasswordCredential, PasswordCredential.Definition<T>, PasswordCredential.UpdateDefinition<T> {

    private String name;
    private HasCredential<T> parent;
    OutputStream authFile;
    private String subscriptionId;
    private final ClientLogger logger = new ClientLogger(PasswordCredentialImpl.class);

    PasswordCredentialImpl(PasswordCredentialInner passwordCredential) {
        super(passwordCredential);
        if (passwordCredential.customKeyIdentifier() != null && passwordCredential.customKeyIdentifier().length > 0) {
            this.name = new String(
                Base64.getMimeDecoder().decode(
                    new String(passwordCredential.customKeyIdentifier(), StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
        } else {
            this.name = passwordCredential.keyId();
        }
    }

    PasswordCredentialImpl(String name, HasCredential<T> parent) {
        super(
            new PasswordCredentialInner()
                .withCustomKeyIdentifier(Base64.getEncoder().encode(name.getBytes(StandardCharsets.UTF_8)))
                .withStartDate(OffsetDateTime.now())
                .withEndDate(OffsetDateTime.now().plusYears(1)));
        this.name = name;
        this.parent = parent;
    }

    @Override
    public Mono<PasswordCredential> refreshAsync() {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Cannot refresh credentials."));
    }

    @Override
    protected Mono<PasswordCredentialInner> getInnerAsync() {
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
        return parent.withPasswordCredential(this);
    }

    @Override
    public PasswordCredentialImpl<T> withPasswordValue(String password) {
        innerModel().withValue(password);
        return this;
    }

    @Override
    public PasswordCredentialImpl<T> withStartDate(OffsetDateTime startDate) {
        OffsetDateTime original = startDate();
        innerModel().withStartDate(startDate);
        // Adjust end time
        withDuration(Duration.between(original, endDate()));
        return this;
    }

    @Override
    public PasswordCredentialImpl<T> withDuration(Duration duration) {
        innerModel().withEndDate(startDate().plus(duration));
        return this;
    }

    @Override
    public PasswordCredentialImpl<T> withAuthFileToExport(OutputStream outputStream) {
        this.authFile = outputStream;
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
        builder.append("  ").append(String.format("\"clientSecret\": \"%s\",", value())).append("\n");
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
    public PasswordCredentialImpl<T> withSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
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
