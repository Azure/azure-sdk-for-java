// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.identity;

import com.azure.identity.IntelliJCredential;
import com.azure.identity.IntelliJCredentialBuilder;
import org.springframework.util.StringUtils;

public class SpringIntelliJCredentialBuilder extends SpringCredentialBuilderBase<SpringIntelliJCredentialBuilder, IntelliJCredential> {

    private String tenantId;

    public SpringIntelliJCredentialBuilder() {
        this.delegateCredentialBuilder = new IntelliJCredentialBuilder();
    }

    /**
     * Sets the tenant id of the user to authenticate through the {@link IntelliJCredential}. The default is
     * the tenant the user originally authenticated to via the Azure Toolkit for IntelliJ plugin.
     *
     * @param tenantId the tenant ID to set.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public SpringIntelliJCredentialBuilder tenantId(String tenantId) {
        if (StringUtils.hasText(tenantId)) {
            this.tenantId = tenantId;
        }
        return this;
    }

    public SpringIntelliJCredentialBuilder keePassDatabasePath(String databasePath) {
        ((IntelliJCredentialBuilder) this.delegateCredentialBuilder).keePassDatabasePath(databasePath);
        return this;
    }

    @Override
    public IntelliJCredential build() {
        return ((IntelliJCredentialBuilder) this.delegateCredentialBuilder).tenantId(this.tenantId).build();
    }
}
