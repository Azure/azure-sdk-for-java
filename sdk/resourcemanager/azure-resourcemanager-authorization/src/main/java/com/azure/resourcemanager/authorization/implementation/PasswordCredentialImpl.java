// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.authorization.fluent.models.MicrosoftGraphPasswordCredentialInner;
import com.azure.resourcemanager.authorization.models.PasswordCredential;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Implementation for ServicePrincipal and its parent interfaces. */
class PasswordCredentialImpl<T extends HasCredential<T>>
    extends IndexableRefreshableWrapperImpl<PasswordCredential, MicrosoftGraphPasswordCredentialInner>
    implements PasswordCredential, PasswordCredential.Definition<T> {

    private String name;
    private HasCredential<T> parent;
    OutputStream authFile;
    private String subscriptionId;
    private final List<Consumer<? super PasswordCredential>> consumers = new ArrayList<>();
    private final ClientLogger logger = new ClientLogger(PasswordCredentialImpl.class);

    PasswordCredentialImpl(MicrosoftGraphPasswordCredentialInner passwordCredential) {
        super(passwordCredential);
        if (passwordCredential.displayName() != null) {
            this.name = passwordCredential.displayName();
        } else {
            this.name = passwordCredential.keyId().toString();
        }
    }

    PasswordCredentialImpl(String name, HasCredential<T> parent) {
        super(
            new MicrosoftGraphPasswordCredentialInner()
                .withDisplayName(name)
                .withStartDateTime(OffsetDateTime.now())
                .withEndDateTime(OffsetDateTime.now().plusYears(1)));
        this.name = name;
        this.parent = parent;
    }

    @Override
    public Mono<PasswordCredential> refreshAsync() {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Cannot refresh credentials."));
    }

    @Override
    protected Mono<MicrosoftGraphPasswordCredentialInner> getInnerAsync() {
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
        return innerModel().secretText();
    }

    @Override
    public T attach() {
        return parent.withPasswordCredential(this);
    }

    @Override
    public PasswordCredentialImpl<T> withStartDate(OffsetDateTime startDate) {
        OffsetDateTime original = startDate();
        innerModel().withStartDateTime(startDate);
        // Adjust end time
        withDuration(Duration.between(original, endDate()));
        return this;
    }

    @Override
    public PasswordCredentialImpl<T> withDuration(Duration duration) {
        innerModel().withEndDateTime(startDate().plus(duration));
        return this;
    }

    @Override
    public PasswordCredentialImpl<T> withAuthFileToExport(OutputStream outputStream) {
        this.authFile = outputStream;
        return this;
    }

    void exportAuthFile(ServicePrincipalImpl servicePrincipal) {
        exportAuthFile(servicePrincipal.manager().environment(),
            servicePrincipal.applicationId(),
            servicePrincipal.manager().tenantId());
    }

    void exportAuthFile(ActiveDirectoryApplicationImpl activeDirectoryApplication) {
        exportAuthFile(activeDirectoryApplication.manager().environment(),
            activeDirectoryApplication.applicationId(),
            activeDirectoryApplication.manager().tenantId());
    }

    void exportAuthFile(AzureEnvironment environment, String clientId, String tenantId) {
        if (authFile == null) {
            return;
        }

        StringBuilder builder = new StringBuilder("{\n");
        builder
            .append("  ")
            .append(String.format("\"clientId\": \"%s\",", clientId))
            .append("\n");
        builder.append("  ").append(String.format("\"clientSecret\": \"%s\",", value())).append("\n");
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

    void consumeSecret() {
        consumers.forEach(consumer -> consumer.accept(this));
    }

    @Override
    public PasswordCredentialImpl<T> withSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
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

    @Override
    public PasswordCredentialImpl<T> withPasswordConsumer(Consumer<? super PasswordCredential> passwordConsumer) {
        consumers.add(passwordConsumer);
        return this;
    }
}
