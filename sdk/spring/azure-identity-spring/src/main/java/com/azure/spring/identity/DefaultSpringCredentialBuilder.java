// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.AzureCliCredential;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzurePowerShellCredential;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.IntelliJCredential;
import com.azure.identity.IntelliJCredentialBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.SharedTokenCacheCredential;
import com.azure.identity.VisualStudioCodeCredential;
import com.azure.identity.VisualStudioCodeCredentialBuilder;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * The default implementation of Spring token credential. It will populate credentials from the default
 * property prefix <i><b>azure.credential</b></i> and an alternative prefix if specified, for example
 * <i><b>spring.cloud.azure</b></i>.
 */
public class DefaultSpringCredentialBuilder extends SpringCredentialBuilderBase<DefaultSpringCredentialBuilder> {

    private String managedIdentityClientId;
    private String tenantId;

    public DefaultSpringCredentialBuilder(Environment environment) {
        super(environment);
        final AzureEnvironment azureEnvironment = new AzureEnvironment(environment);
        this.managedIdentityClientId = azureEnvironment.getClientId();
        this.tenantId = azureEnvironment.getTenantId();
    }

    public DefaultSpringCredentialBuilder managedIdentityClientId(String clientId) {
        this.managedIdentityClientId = clientId;
        return this;
    }

    public DefaultSpringCredentialBuilder tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Build a {@link DefaultSpringCredential} with below list of credentials:
     * <ol>
     * <li>{@link SpringEnvironmentCredential}</li>
     * <li>{@link ManagedIdentityCredential}</li>
     * <li>{@link SharedTokenCacheCredential}</li>
     * <li>{@link IntelliJCredential}</li>
     * <li>{@link VisualStudioCodeCredential}</li>
     * <li>{@link AzureCliCredential}</li>
     * <li>{@link AzurePowerShellCredential}</li>
     * </ol>
     *
     * @return the default Spring token credential.
     */
    public TokenCredential build() {
        List<TokenCredential> chain = new ArrayList<>();

        chain.add(new SpringEnvironmentCredential(this.environment));
        chain.add(new ManagedIdentityCredentialBuilder().clientId(this.managedIdentityClientId).build());
        // TODO (xiada) Add SharedTokenCacheCredential
        chain.add(new IntelliJCredentialBuilder().tenantId(this.tenantId).build());
        chain.add(new VisualStudioCodeCredentialBuilder().tenantId(this.tenantId).build());
        chain.add(new AzureCliCredentialBuilder().build());
        chain.add(new AzurePowerShellCredentialBuilder().build());

        return new DefaultSpringCredential(chain);
    }

}
