// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.identity;

import com.azure.identity.IntelliJCredential;
import com.azure.identity.IntelliJCredentialBuilder;
import com.azure.identity.VisualStudioCodeCredential;
import com.azure.identity.VisualStudioCodeCredentialBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

public class SpringVisualStudioCodeCredentialBuilder extends SpringCredentialBuilderBase<VisualStudioCodeCredential> {

    private String tenantId;

    public SpringVisualStudioCodeCredentialBuilder(Environment environment) {
        super(environment);
        this.tenantId = new AzureEnvironment(environment).getTenantId();
        this.delegateCredentialBuilder = new VisualStudioCodeCredentialBuilder();
    }


    /**
     * Sets the tenant id of the user to authenticate through the {@link IntelliJCredential}. The default is
     * the tenant the user originally authenticated to via the Azure Toolkit for IntelliJ plugin.
     *
     * @param tenantId the tenant ID to set.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public SpringVisualStudioCodeCredentialBuilder tenantId(String tenantId) {
        // TODO (xiada) should we check the tenantId's format here?
        this.tenantId = tenantId;
        return this;
    }

    @Override
    public VisualStudioCodeCredential internalBuild() {
        return ((VisualStudioCodeCredentialBuilder) this.delegateCredentialBuilder).tenantId(this.tenantId).build();
    }
}
