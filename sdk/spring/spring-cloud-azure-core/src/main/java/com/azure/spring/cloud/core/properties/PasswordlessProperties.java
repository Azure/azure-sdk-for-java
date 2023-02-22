// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties;

import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import com.azure.spring.cloud.core.provider.authentication.TokenCredentialOptionsProvider;

import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Unified properties for Azure passwordless clients.
 */
public interface PasswordlessProperties extends TokenCredentialOptionsProvider, AzureProfileOptionsProvider {

    /**
     * Gets the scopes required for the access token.
     *
     * @return scopes required for the access token
     */
    String getScopes();

    /**
     * Set the scopes to get the access token.
     *
     * @param scopes the scopes required for the access token
     */
    void setScopes(String scopes);

    /**
     * Whether to enable connections authenticating with Azure AD, default is false.
     *
     * @return Whether to enable connections authenticating with Azure AD.
     */
    boolean isPasswordlessEnabled();

    /**
     * Set the passwordlessEnabled value.
     *
     * @param passwordlessEnabled the passwordlessEnabled
     */
    void setPasswordlessEnabled(boolean passwordlessEnabled);

    /**
     * Convert {@link PasswordlessProperties} to {@link Properties}.
     * @return converted {@link Properties} instance
     */
    default Properties toPasswordlessProperties() {
        Properties target = new Properties();
        for (AzurePasswordlessPropertiesMapping m : AzurePasswordlessPropertiesMapping.values()) {
            if (m.getter.apply(this) != null) {
                m.setter.accept(target, m.getter.apply(this));
            }
        }
        return target;
    }

    /**
     * A mapping util used to convert a {@link PasswordlessProperties} instance to a {@link Properties} instance.
     */
    enum AzurePasswordlessPropertiesMapping {

        /**
         * Getter function and setter biConsumer for scopes.
         */
        scopes(p -> p.getScopes(),
            (p, s) -> p.setProperty(AuthProperty.SCOPES.getPropertyKey(), s)),

        /**
         * Getter function and setter biConsumer for clientCertificatePassword.
         */
        clientCertificatePassword(p -> p.getCredential().getClientCertificatePassword(),
            (p, s) -> p.setProperty(AuthProperty.CLIENT_CERTIFICATE_PASSWORD.getPropertyKey(), s)),

        /**
         * Getter function and setter biConsumer for clientCertificatePath.
         */
        clientCertificatePath(p -> p.getCredential().getClientCertificatePath(),
            (p, s) -> p.setProperty(AuthProperty.CLIENT_CERTIFICATE_PATH.getPropertyKey(), s)),

        /**
         * Getter function and setter biConsumer for clientId.
         */
        clientId(p -> p.getCredential().getClientId(),
            (p, s) -> p.setProperty(AuthProperty.CLIENT_ID.getPropertyKey(), s)),

        /**
         * Getter function and setter biConsumer for clientSecret.
         */
        clientSecret(p -> p.getCredential().getClientSecret(),
            (p, s) -> p.setProperty(AuthProperty.CLIENT_SECRET.getPropertyKey(), s)),

        /**
         * Getter function and setter biConsumer for managedIdentityEnabled.
         */
        managedIdentityEnabled(p -> String.valueOf(p.getCredential().isManagedIdentityEnabled()),
            (p, s) -> p.setProperty(AuthProperty.MANAGED_IDENTITY_ENABLED.getPropertyKey(), s)),

        /**
         * Getter function and setter biConsumer for password.
         */
        password(p -> p.getCredential().getPassword(),
            (p, s) -> p.setProperty(AuthProperty.PASSWORD.getPropertyKey(), s)),

        /**
         * Getter function and setter biConsumer for username.
         */
        username(p -> p.getCredential().getUsername(),
            (p, s) -> p.setProperty(AuthProperty.USERNAME.getPropertyKey(), s)),

        /**
         * Getter function and setter biConsumer for tenantId.
         */
        tenantId(p -> p.getProfile().getTenantId(),
            (p, s) -> p.setProperty(AuthProperty.TENANT_ID.getPropertyKey(), s)),

        /**
         * Getter function and setter biConsumer for authorityHost.
         */
        authorityHost(p -> p.getProfile().getEnvironment().getActiveDirectoryEndpoint(),
            (p, s) -> p.setProperty(AuthProperty.AUTHORITY_HOST.getPropertyKey(), s));

        private Function<PasswordlessProperties, String> getter;
        private BiConsumer<Properties, String> setter;

        AzurePasswordlessPropertiesMapping(Function<PasswordlessProperties, String> getter, BiConsumer<Properties,
            String> setter) {
            this.getter = getter;
            this.setter = setter;
        }

    }
}

