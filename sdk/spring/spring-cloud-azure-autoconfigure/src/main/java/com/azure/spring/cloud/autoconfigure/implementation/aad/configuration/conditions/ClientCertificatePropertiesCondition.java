// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.conditions;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * Condition that checks for OAuth2 client JWK resolver.
 */
public class ClientCertificatePropertiesCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage
            .forCondition("Azure AD OAuth2 client JWK resolver Condition");
        AzureGlobalProperties globalProperties =
            Binder.get(context.getEnvironment())
                  .bind("spring.cloud.azure", AzureGlobalProperties.class)
                  .orElse(null);
        AadAuthenticationProperties properties =
            Binder.get(context.getEnvironment())
                  .bind("spring.cloud.azure.active-directory", AadAuthenticationProperties.class)
                  .orElse(null);
        if (globalProperties == null && properties == null) {
            return ConditionOutcome.noMatch(message.notAvailable("Azure AD authentication properties"));
        }

        if (globalProperties != null
            && StringUtils.hasText(globalProperties.getCredential().getClientCertificatePath())
            && StringUtils.hasText(globalProperties.getCredential().getClientCertificatePassword())) {
            return ConditionOutcome.match(
                message.foundExactly("'client-certificate-path' and 'client-certificate-password' "
                    + "under the prefix 'spring.cloud.azure.credential'."));

        }

        if (StringUtils.hasText(properties.getCredential().getClientCertificatePath())
            && StringUtils.hasText(properties.getCredential().getClientCertificatePassword())) {
            return ConditionOutcome.match(
                message.foundExactly("'client-certificate-path' and 'client-certificate-password' "
                    + "under the prefix 'spring.cloud.azure.active-directory.credential'."));

        }
        return ConditionOutcome.noMatch(
            message.because("No attribute configuration found "
                + "for 'client-certificate-path' and 'client-certificate-password'."));
    }
}
