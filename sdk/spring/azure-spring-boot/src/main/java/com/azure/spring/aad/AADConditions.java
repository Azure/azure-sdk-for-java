// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Conditions for activating AAD beans.
 * <pre>
 *| Mode                                | Has dependency: spring-security-oauth2-client | Has dependency: spring-security-oauth2-resource-server | azure.activedirectory.enable-web-app-and-resource-server=true |
 *|-------------------------------------|-----------------------------------------------|--------------------------------------------------------|-----------------------------------------------------------|
 *| Web application                     |                      Yes                      |                          No                            |                            No                             |
 *| Resource Server                     |                      No                       |                          Yes                           |                            No                             |
 *| Resource Server with OBO function   |                      Yes                      |                          Yes                           |                            No                             |
 *| Web Application and Resource Server |                      Yes                      |                          Yes                           |                            Yes                            |
 *
 * See more on https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-boot-starter-active-directory/README.md#dependency-matrix
 * </pre>
 */
public final class AADConditions {

    public static final String BEARER_TOKEN_AUTHENTICATION_TOKEN_CLASS_NAME =
        "org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken";


    /**
     * Web application, web resource server or all in scenario condition.
     */
    static final class ClientRegistrationCondition extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            ConditionMessage.Builder message = ConditionMessage.forCondition(
                "AAD Application Client Condition");
            AADAuthenticationProperties properties = Binder.get(context.getEnvironment())
                                                           .bind("azure.activedirectory",
                                                               AADAuthenticationProperties.class)
                                                           .orElse(null);
            if (properties == null) {
                return ConditionOutcome.noMatch(message.notAvailable("aad authorization properties"));
            }

            AADApplicationType applicationType = AADApplicationType.applicationType(properties);
            if (applicationType == null) {
                return ConditionOutcome.noMatch(message.because("Not found the AAD application type."));
            }

            StringBuilder details = new StringBuilder();
            switch (applicationType) {
                case WEB_APPLICATION:
                    details.append("classes EnableWebSecurity and ClientRegistration");
                    break;
                case RESOURCE_SERVER_WITH_OBO:
                    details.append("classes EnableWebSecurity, ClientRegistration and BearerTokenAuthenticationToken");
                    break;
                case WEB_APPLICATION_AND_RESOURCE_SERVER:
                    details.append("classes EnableWebSecurity, ClientRegistration and BearerTokenAuthenticationToken "
                        + "and property 'azure.activedirectory.enable-web-app-and-resource-server=true'");
                    break;
                default:
                    return ConditionOutcome.noMatch(
                        message.didNotFind("necessary dependencies")
                               .items("classes EnableWebSecurity, ClientRegistration and BearerTokenAuthenticationToken",
                                   "'azure.activedirectory.enable-web-app-and-resource-server=true'"));
            }
            return ConditionOutcome.match(message.foundExactly(details.toString()));
        }
    }


    /**
     * Web application or all in scenario condition.
     */
    public static final class WebAppCondition extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            ConditionMessage.Builder message = ConditionMessage.forCondition(
                "AAD Web Application Condition");
            AADAuthenticationProperties properties = Binder.get(context.getEnvironment())
                                                           .bind("azure.activedirectory",
                                                               AADAuthenticationProperties.class)
                                                           .orElse(null);
            if (properties == null) {
                return ConditionOutcome.noMatch(message.notAvailable("aad authorization properties"));
            }

            AADApplicationType applicationType = AADApplicationType.applicationType(properties);
            if (applicationType == null) {
                return ConditionOutcome.noMatch(message.because("Not found the AAD application type."));
            }

            StringBuilder details = new StringBuilder();
            switch (applicationType) {
                case WEB_APPLICATION:
                    details.append("classes EnableWebSecurity and ClientRegistration");
                    break;
                case WEB_APPLICATION_AND_RESOURCE_SERVER:
                    details.append("classes EnableWebSecurity, ClientRegistration and BearerTokenAuthenticationToken "
                        + "and property 'azure.activedirectory.enable-web-app-and-resource-server=true'");
                    break;
                default:
                    return ConditionOutcome.noMatch(
                        message.didNotFind("necessary dependencies")
                               .items("classes EnableWebSecurity, ClientRegistration and BearerTokenAuthenticationToken",
                                   "'azure.activedirectory.enable-web-app-and-resource-server=true'"));
            }
            return ConditionOutcome.match(message.foundExactly(details.toString()));
        }
    }

    /**
     * Resource server or all in scenario condition.
     */
    public static final class WebApiCondition extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            ConditionMessage.Builder message = ConditionMessage.forCondition(
                "AAD Resource Server Condition");
            AADAuthenticationProperties properties = Binder.get(context.getEnvironment())
                                                           .bind("azure.activedirectory",
                                                               AADAuthenticationProperties.class)
                                                           .orElse(null);
            if (properties == null) {
                return ConditionOutcome.noMatch(message.notAvailable("aad authorization properties"));
            }

            AADApplicationType applicationType = AADApplicationType.applicationType(properties);
            if (applicationType == null) {
                return ConditionOutcome.noMatch(message.because("Not found the AAD application type."));
            }

            StringBuilder details = new StringBuilder();
            switch (applicationType) {
                case RESOURCE_SERVER:
                    details.append("classes BearerTokenAuthenticationToken");
                    break;
                case RESOURCE_SERVER_WITH_OBO:
                    details.append("classes EnableWebSecurity, ClientRegistration and BearerTokenAuthenticationToken "
                        + "and property 'azure.activedirectory.enable-web-app-and-resource-server=false'");
                    break;
                case WEB_APPLICATION_AND_RESOURCE_SERVER:
                    details.append("classes EnableWebSecurity, ClientRegistration and BearerTokenAuthenticationToken "
                        + "and property 'azure.activedirectory.enable-web-app-and-resource-server=true'");
                    break;
                default:
                    return ConditionOutcome.noMatch(
                        message.didNotFind("necessary dependencies")
                               .items("classes EnableWebSecurity, ClientRegistration and BearerTokenAuthenticationToken",
                                   "'azure.activedirectory.enable-web-app-and-resource-server=true'"));
            }
            return ConditionOutcome.match(message.foundExactly(details.toString()));
        }
    }
}
