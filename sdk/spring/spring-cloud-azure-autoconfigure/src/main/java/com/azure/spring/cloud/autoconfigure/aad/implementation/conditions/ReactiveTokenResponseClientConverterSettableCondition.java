// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.implementation.conditions;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveAuthorizationCodeTokenResponseClient;

/**
 * Condition to evaluate if converters can be set on token response client.
 * This is not possible in spring boot prior to 2.6.
 */
public final class ReactiveTokenResponseClientConverterSettableCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition("AAD Token Response Client converter settable");
        try {
            WebClientReactiveAuthorizationCodeTokenResponseClient.class.getMethod("setHeadersConverter");
            WebClientReactiveAuthorizationCodeTokenResponseClient.class.getMethod("setParametersConverter");
            return ConditionOutcome.match(message.available("setHeadersConverter && setParametersConverter"));
        } catch (NoSuchMethodException e) {
            return ConditionOutcome.noMatch(e.getMessage());
        }
    }

}

