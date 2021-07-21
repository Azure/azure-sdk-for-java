// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.identity;

import com.azure.identity.VisualStudioCodeCredential;
import com.azure.identity.VisualStudioCodeCredentialBuilder;
import org.springframework.util.StringUtils;

/**
 * A wrapper builder for the SpringVisualStudioCodeCredentialBuilder, could be removed when the EnvironmentCredential
 * could accept a Configuration as a constructor parameter.
 */
public class SpringVisualStudioCodeCredentialBuilder extends SpringCredentialBuilderBase<SpringVisualStudioCodeCredentialBuilder, VisualStudioCodeCredential> {

    private String tenantId;

    public SpringVisualStudioCodeCredentialBuilder() {
        this.delegateCredentialBuilder = new VisualStudioCodeCredentialBuilder();
    }

    public SpringVisualStudioCodeCredentialBuilder tenantId(String tenantId) {
        if (StringUtils.hasText(tenantId)) {
            this.tenantId = tenantId;
        }
        return this;
    }

    @Override
    public VisualStudioCodeCredential build() {
        return ((VisualStudioCodeCredentialBuilder) this.delegateCredentialBuilder).tenantId(this.tenantId).build();
    }
}
