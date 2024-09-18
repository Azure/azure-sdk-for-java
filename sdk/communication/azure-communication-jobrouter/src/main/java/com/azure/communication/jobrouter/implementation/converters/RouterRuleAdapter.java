// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.accesshelpers.RouterValueConstructorProxy;
import com.azure.communication.jobrouter.implementation.models.DirectMapRouterRuleInternal;
import com.azure.communication.jobrouter.implementation.models.ExpressionRouterRuleInternal;
import com.azure.communication.jobrouter.implementation.models.FunctionRouterRuleInternal;
import com.azure.communication.jobrouter.implementation.models.RouterRuleInternal;
import com.azure.communication.jobrouter.implementation.models.StaticRouterRuleInternal;
import com.azure.communication.jobrouter.implementation.models.WebhookRouterRuleInternal;
import com.azure.communication.jobrouter.models.DirectMapRouterRule;
import com.azure.communication.jobrouter.models.ExpressionRouterRule;
import com.azure.communication.jobrouter.models.ExpressionRouterRuleLanguage;
import com.azure.communication.jobrouter.models.FunctionRouterRule;
import com.azure.communication.jobrouter.models.FunctionRouterRuleCredential;
import com.azure.communication.jobrouter.models.OAuth2WebhookClientCredential;
import com.azure.communication.jobrouter.models.RouterRule;
import com.azure.communication.jobrouter.models.StaticRouterRule;
import com.azure.communication.jobrouter.models.WebhookRouterRule;

/**
 * RouterRuleAdapter.
 */
public class RouterRuleAdapter {
    /**
     * Converts routerRule from external to internal.
     * @param routerRule external RouterRule.
     * @return internal RouterRule.
     */
    public static RouterRuleInternal getRouterRuleInternal(RouterRule routerRule) {
        RouterRuleInternal prioritizationRuleInternal = null;
        if (routerRule.getClass() == ExpressionRouterRule.class) {
            ExpressionRouterRule expressionRouterRule = (ExpressionRouterRule) routerRule;
            prioritizationRuleInternal = new ExpressionRouterRuleInternal(expressionRouterRule.getExpression())
                .setLanguage(expressionRouterRule.getLanguage());
        } else if (routerRule.getClass() == DirectMapRouterRule.class) {
            DirectMapRouterRule directMapRouterRule = (DirectMapRouterRule) routerRule;
            prioritizationRuleInternal = new DirectMapRouterRuleInternal();
        } else if (routerRule.getClass() == FunctionRouterRule.class) {
            FunctionRouterRule functionRouterRule = (FunctionRouterRule) routerRule;
            prioritizationRuleInternal = new FunctionRouterRuleInternal(functionRouterRule.getFunctionUri())
                .setCredential(functionRouterRule.getCredential());
        } else if (routerRule.getClass() == StaticRouterRule.class) {
            StaticRouterRule staticRouterRule = (StaticRouterRule) routerRule;
            prioritizationRuleInternal = new StaticRouterRuleInternal()
                .setValue(RouterValueAdapter.getValue(staticRouterRule.getValue()));
        } else if (routerRule.getClass() == WebhookRouterRule.class) {
            WebhookRouterRule webhookRouterRule = (WebhookRouterRule) routerRule;
            prioritizationRuleInternal = new WebhookRouterRuleInternal()
                .setAuthorizationServerUri(webhookRouterRule.getAuthorizationServerUri())
                .setClientCredential(webhookRouterRule.getClientCredential())
                .setWebhookUri(webhookRouterRule.getWebhookUri());
        }
        return prioritizationRuleInternal;
    }

    public static RouterRule convertRouterRuleToPublic(RouterRuleInternal rule) {
        if (rule instanceof DirectMapRouterRuleInternal) {
            return new DirectMapRouterRule();
        } else if (rule instanceof ExpressionRouterRuleInternal) {
            return new ExpressionRouterRule(((ExpressionRouterRuleInternal) rule).getExpression());
        } else if (rule instanceof FunctionRouterRuleInternal) {
            FunctionRouterRuleInternal functionRouterRule = (FunctionRouterRuleInternal) rule;
            return new FunctionRouterRule(functionRouterRule.getFunctionUri())
                    .setCredential(new FunctionRouterRuleCredential()
                    .setFunctionKey(functionRouterRule.getCredential().getFunctionKey())
                    .setAppKey(functionRouterRule.getCredential().getAppKey())
                    .setClientId(functionRouterRule.getCredential().getClientId()));
        } else if (rule instanceof StaticRouterRuleInternal) {
            return new StaticRouterRule()
                .setValue(RouterValueConstructorProxy.create(((StaticRouterRuleInternal) rule).getValue()));
        } else if (rule instanceof WebhookRouterRuleInternal) {
            WebhookRouterRuleInternal webhookRouterRule = (WebhookRouterRuleInternal) rule;
            return new WebhookRouterRule()
                .setWebhookUri(webhookRouterRule.getWebhookUri())
                .setClientCredential(new OAuth2WebhookClientCredential()
                        .setClientId(webhookRouterRule.getClientCredential().getClientId())
                        .setClientSecret(webhookRouterRule.getClientCredential().getClientSecret()))
                        .setAuthorizationServerUri(webhookRouterRule.getAuthorizationServerUri());
        }

        return null;
    }

    public static RouterRuleInternal convertRouterRuleToInternal(RouterRule rule) {
        if (rule instanceof DirectMapRouterRule) {
            return new DirectMapRouterRuleInternal();
        } else if (rule instanceof ExpressionRouterRule) {
            return new ExpressionRouterRuleInternal(((ExpressionRouterRule) rule).getExpression())
                .setLanguage(ExpressionRouterRuleLanguage.POWER_FX);
        } else if (rule instanceof FunctionRouterRule) {
            FunctionRouterRule functionRouterRule = (FunctionRouterRule) rule;
            return new FunctionRouterRuleInternal(functionRouterRule.getFunctionUri())
                    .setCredential(new FunctionRouterRuleCredential()
                    .setFunctionKey(functionRouterRule.getCredential().getFunctionKey())
                    .setAppKey(functionRouterRule.getCredential().getAppKey())
                    .setClientId(functionRouterRule.getCredential().getClientId()));
        } else if (rule instanceof StaticRouterRule) {
            return new StaticRouterRuleInternal().setValue(RouterValueAdapter.getValue(((StaticRouterRule) rule).getValue()));
        } else if (rule instanceof WebhookRouterRule) {
            WebhookRouterRule webhookRouterRule = (WebhookRouterRule) rule;
            return new WebhookRouterRuleInternal().setWebhookUri(webhookRouterRule.getWebhookUri())
                .setClientCredential(new OAuth2WebhookClientCredential()
                    .setClientId(webhookRouterRule.getClientCredential().getClientId())
                    .setClientSecret(webhookRouterRule.getClientCredential().getClientSecret()))
                .setAuthorizationServerUri(webhookRouterRule.getAuthorizationServerUri());
        }

        return null;
    }
}
