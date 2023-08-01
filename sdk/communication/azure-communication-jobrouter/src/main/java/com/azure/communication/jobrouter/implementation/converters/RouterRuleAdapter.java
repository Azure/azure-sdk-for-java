// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.accesshelpers.LabelValueConstructorProxy;
import com.azure.communication.jobrouter.implementation.models.DirectMapRouterRuleInternal;
import com.azure.communication.jobrouter.implementation.models.ExpressionRouterRuleInternal;
import com.azure.communication.jobrouter.implementation.models.ExpressionRouterRuleLanguageInternal;
import com.azure.communication.jobrouter.implementation.models.FunctionRouterRuleCredentialInternal;
import com.azure.communication.jobrouter.implementation.models.FunctionRouterRuleInternal;
import com.azure.communication.jobrouter.implementation.models.Oauth2ClientCredentialInternal;
import com.azure.communication.jobrouter.implementation.models.RouterRuleInternal;
import com.azure.communication.jobrouter.implementation.models.StaticRouterRuleInternal;
import com.azure.communication.jobrouter.implementation.models.WebhookRouterRuleInternal;
import com.azure.communication.jobrouter.models.DirectMapRouterRule;
import com.azure.communication.jobrouter.models.ExpressionRouterRule;
import com.azure.communication.jobrouter.models.FunctionRouterRule;
import com.azure.communication.jobrouter.models.FunctionRouterRuleCredential;
import com.azure.communication.jobrouter.models.Oauth2ClientCredential;
import com.azure.communication.jobrouter.models.RouterRule;
import com.azure.communication.jobrouter.models.StaticRouterRule;
import com.azure.communication.jobrouter.models.WebhookRouterRule;

/**
 * Converts between RouterRule and RouterRuleInternal
 */
public class RouterRuleAdapter {

    public static RouterRuleInternal convertRouterRuleToInternal(RouterRule rule) {
        if (rule instanceof DirectMapRouterRule) {
            return new DirectMapRouterRuleInternal();
        } else if (rule instanceof ExpressionRouterRule) {
            return new ExpressionRouterRuleInternal().setExpression(((ExpressionRouterRule) rule).getExpression())
                .setLanguage(ExpressionRouterRuleLanguageInternal.POWER_FX);
        } else if (rule instanceof FunctionRouterRule) {
            FunctionRouterRule functionRouterRule = (FunctionRouterRule) rule;
            return new FunctionRouterRuleInternal().setFunctionUri(functionRouterRule.getFunctionUri())
                .setCredential(new FunctionRouterRuleCredentialInternal()
                    .setFunctionKey(functionRouterRule.getCredential().getFunctionKey())
                    .setAppKey(functionRouterRule.getCredential().getAppKey())
                    .setClientId(functionRouterRule.getCredential().getClientId()));
        } else if (rule instanceof StaticRouterRule) {
            return new StaticRouterRuleInternal().setValue(((StaticRouterRule) rule).getValue().getValue());
        } else if (rule instanceof WebhookRouterRule) {
            WebhookRouterRule webhookRouterRule = (WebhookRouterRule) rule;
            return new WebhookRouterRuleInternal().setWebhookUri(webhookRouterRule.getWebhookUri())
                .setClientCredential(new Oauth2ClientCredentialInternal()
                    .setClientId(webhookRouterRule.getClientCredential().getClientId())
                    .setClientSecret(webhookRouterRule.getClientCredential().getClientSecret()))
                .setAuthorizationServerUri(webhookRouterRule.getAuthorizationServerUri());
        }

        return null;
    }

    public static RouterRule convertRouterRuleToPublic(RouterRuleInternal rule) {
        if (rule instanceof DirectMapRouterRuleInternal) {
            return new DirectMapRouterRule();
        } else if (rule instanceof ExpressionRouterRuleInternal) {
            return new ExpressionRouterRule(((ExpressionRouterRuleInternal) rule).getExpression());
        } else if (rule instanceof FunctionRouterRuleInternal) {
            FunctionRouterRuleInternal functionRouterRule = (FunctionRouterRuleInternal) rule;
            return new FunctionRouterRule().setFunctionUri(functionRouterRule.getFunctionUri())
                .setCredential(new FunctionRouterRuleCredential()
                    .setFunctionKey(functionRouterRule.getCredential().getFunctionKey())
                    .setAppKey(functionRouterRule.getCredential().getAppKey())
                    .setClientId(functionRouterRule.getCredential().getClientId()));
        } else if (rule instanceof StaticRouterRuleInternal) {
            return new StaticRouterRule(LabelValueConstructorProxy.create(((StaticRouterRuleInternal) rule).getValue()));
        } else if (rule instanceof WebhookRouterRuleInternal) {
            WebhookRouterRuleInternal webhookRouterRule = (WebhookRouterRuleInternal) rule;
            return new WebhookRouterRule(webhookRouterRule.getWebhookUri())
                .setClientCredential(new Oauth2ClientCredential(webhookRouterRule.getClientCredential().getClientId(),
                    webhookRouterRule.getClientCredential().getClientSecret()))
                .setAuthorizationServerUri(webhookRouterRule.getAuthorizationServerUri());
        }

        return null;
    }
}
