/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.google.common.io.BaseEncoding;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.PasswordCredential;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import com.microsoft.rest.RestClient;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import rx.Observable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Implementation for ServicePrincipal and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
class PasswordCredentialImpl<T>
        extends IndexableRefreshableWrapperImpl<PasswordCredential, PasswordCredentialInner>
        implements
        PasswordCredential,
        PasswordCredential.Definition<T>,
        PasswordCredential.UpdateDefinition<T> {

    private String name;
    private HasCredential<?> parent;
    OutputStream authFile;
    private String subscriptionId;

    PasswordCredentialImpl(PasswordCredentialInner passwordCredential) {
        super(passwordCredential);
        if (passwordCredential.customKeyIdentifier() != null && !passwordCredential.customKeyIdentifier().isEmpty()) {
            this.name = new String(BaseEncoding.base64().decode(passwordCredential.customKeyIdentifier()));
        } else {
            this.name = passwordCredential.keyId();
        }
    }

    PasswordCredentialImpl(String name, HasCredential<?> parent) {
        super(new PasswordCredentialInner()
                .withCustomKeyIdentifier(BaseEncoding.base64().encode(name.getBytes()))
                .withStartDate(DateTime.now())
                .withEndDate(DateTime.now().plusYears(1)));
        this.name = name;
        this.parent = parent;
    }

    @Override
    public Observable<PasswordCredential> refreshAsync() {
        throw new UnsupportedOperationException("Cannot refresh credentials.");
    }

    @Override
    protected Observable<PasswordCredentialInner> getInnerAsync() {
        throw new UnsupportedOperationException("Cannot refresh credentials.");
    }

    @Override
    public String id() {
        return inner().keyId();
    }

    @Override
    public DateTime startDate() {
        return inner().startDate();
    }

    @Override
    public DateTime endDate() {
        return inner().endDate();
    }

    @Override
    public String value() {
        return inner().value();
    }


    @Override
    @SuppressWarnings("unchecked")
    public T attach() {
        parent.withPasswordCredential(this);
        return (T) parent;
    }

    @Override
    public PasswordCredentialImpl<T> withPasswordValue(String password) {
        inner().withValue(password);
        return this;
    }

    @Override
    public PasswordCredentialImpl<T> withStartDate(DateTime startDate) {
        DateTime original = startDate();
        inner().withStartDate(startDate);
        // Adjust end time
        withDuration(Duration.millis(endDate().getMillis() - original.getMillis()));
        return this;
    }

    @Override
    public PasswordCredentialImpl<T> withDuration(Duration duration) {
        inner().withEndDate(startDate().plus(duration.getMillis()));
        return this;
    }

    @Override
    public String name() {
        return name;
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
        RestClient restClient = servicePrincipal.manager().roleInner().restClient();
        AzureEnvironment environment = null;
        if (restClient.credentials() instanceof AzureTokenCredentials) {
            environment = ((AzureTokenCredentials) restClient.credentials()).environment();
        } else {
            String baseUrl = restClient.retrofit().baseUrl().toString();
            for (AzureEnvironment env : AzureEnvironment.knownEnvironments()) {
                if (env.resourceManagerEndpoint().toLowerCase().contains(baseUrl.toLowerCase())) {
                    environment = env;
                    break;
                }
            }
            if (environment == null) {
                throw new IllegalArgumentException("Unknown resource manager endpoint " + baseUrl);
            }
        }

        StringBuilder builder = new StringBuilder("{\n");
        builder.append("  ").append(String.format("\"clientId\": \"%s\",", servicePrincipal.applicationId())).append("\n");
        builder.append("  ").append(String.format("\"clientSecret\": \"%s\",", value())).append("\n");
        builder.append("  ").append(String.format("\"tenantId\": \"%s\",", servicePrincipal.manager().tenantId())).append("\n");
        builder.append("  ").append(String.format("\"subscriptionId\": \"%s\",", servicePrincipal.assignedSubscription)).append("\n");
        builder.append("  ").append(String.format("\"activeDirectoryEndpointUrl\": \"%s\",", environment.activeDirectoryEndpoint())).append("\n");
        builder.append("  ").append(String.format("\"resourceManagerEndpointUrl\": \"%s\",", environment.resourceManagerEndpoint())).append("\n");
        builder.append("  ").append(String.format("\"activeDirectoryGraphResourceId\": \"%s\",", environment.graphEndpoint())).append("\n");
        builder.append("  ").append(String.format("\"managementEndpointUrl\": \"%s\"", environment.managementEndpoint())).append("\n");
        builder.append("}");
        try {
            authFile.write(builder.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PasswordCredentialImpl<T> withSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }
}
