// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

/**
 * Conditions for activating AAD beans.
 */
public final class AADConditions {

    public static final String BEARER_TOKEN_AUTHENTICATION_TOKEN_CLASS_NAME =
        "org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken";


    /**
     * Web application, web resource server or all in scenario condition.
     */
    static final class ClientRegistrationCondition extends AnyNestedCondition {
        ClientRegistrationCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        /**
         * Web application scenario condition.
         */
        @ConditionalOnMissingClass(BEARER_TOKEN_AUTHENTICATION_TOKEN_CLASS_NAME)
        @ConditionalOnClass({ EnableWebSecurity.class, ClientRegistration.class })
        @ConditionalOnExpression("${azure.activedirectory.enable-web-app-resource-server:false} == false")
        static class WebAppClientMode {

        }

        /**
         * Web resource server scenario condition.
         */
        @ConditionalOnClass({
            EnableWebSecurity.class,
            ClientRegistration.class,
            BearerTokenAuthenticationToken.class
        })
        @ConditionalOnExpression("${azure.activedirectory.enable-web-app-resource-server:false} == false")
        static class WebApiClientMode {

        }

        /**
         * Web application and Web resource server scenario condition.
         */
        @ConditionalOnClass({
            EnableWebSecurity.class,
            ClientRegistration.class,
            BearerTokenAuthenticationToken.class
        })
        @ConditionalOnProperty(value = "azure.activedirectory.enable-web-app-resource-server", havingValue = "true")
        static class AllInClientMode {

        }
    }

    /**
     * Web application or all in scenario condition.
     */
    public static final class WebAppCondition extends AnyNestedCondition {
        WebAppCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        /**
         * Web application scenario condition.
         */
        @ConditionalOnMissingClass(BEARER_TOKEN_AUTHENTICATION_TOKEN_CLASS_NAME)
        @ConditionalOnClass({ EnableWebSecurity.class, ClientRegistration.class })
        @ConditionalOnExpression("${azure.activedirectory.enable-web-app-resource-server:false} == false")
        static class WebAppClientMode {

        }

        /**
         * Web application and Web resource server scenario condition.
         */
        @ConditionalOnClass({
            EnableWebSecurity.class,
            ClientRegistration.class,
            BearerTokenAuthenticationToken.class
        })
        @ConditionalOnProperty(value = "azure.activedirectory.enable-web-app-resource-server", havingValue = "true")
        static class AllInClientMode {

        }
    }

    /**
     * Resource server or all in scenario condition.
     */
    public static final class WebApiCondition extends AnyNestedCondition {
        WebApiCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        /**
         * Web resource server scenario condition.
         */
        @ConditionalOnClass(BearerTokenAuthenticationToken.class)
        @ConditionalOnExpression("${azure.activedirectory.enable-web-app-resource-server:false} == false")
        static class WebApiClientMode {

        }

        /**
         * Web application and Web resource server scenario condition.
         */
        @ConditionalOnClass({
            EnableWebSecurity.class,
            ClientRegistration.class,
            BearerTokenAuthenticationToken.class
        })
        @ConditionalOnProperty(value = "azure.activedirectory.enable-web-app-resource-server", havingValue = "true")
        static class AllInClientMode {

        }
    }
}
