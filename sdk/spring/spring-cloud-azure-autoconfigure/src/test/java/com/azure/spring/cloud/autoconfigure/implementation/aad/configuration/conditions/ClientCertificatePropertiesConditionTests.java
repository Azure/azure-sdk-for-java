// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.conditions;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

class ClientCertificatePropertiesConditionTests extends AbstractCondition {

    @Test
    void match() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.credential.client-certificate-path = client-certificate-path",
                "spring.cloud.azure.credential.client-certificate-password = client-certificate-password"
            )
            .withUserConfiguration(ClientCertificateProperties.class)
            .run(assertConditionMatch(true));
    }

    @Test
    void noMatch() {
        this.contextRunner
            .withUserConfiguration(ClientCertificateProperties.class)
            .run(assertConditionMatch(false));
    }

    @Configuration
    @Conditional(ClientCertificatePropertiesCondition.class)
    static class ClientCertificateProperties extends Config { }

}
