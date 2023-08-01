// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.properties;

import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.spring.cloud.core.properties.PasswordlessProperties;

import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A mapping util used to convert a {@link PasswordlessProperties} instance to a {@link Properties} instance.
 */
public enum AzurePasswordlessPropertiesMapping {

    /**
     * Getter function and setter biConsumer for scopes.
     */
    SCOPES(p -> p.getScopes(),
        (p, s) -> p.setProperty(AuthProperty.SCOPES.getPropertyKey(), s)),

    /**
     * Getter function and setter biConsumer for clientCertificatePassword.
     */
    CLIENT_CERTIFICATE_PASSWORD(p -> p.getCredential().getClientCertificatePassword(),
        (p, s) -> p.setProperty(AuthProperty.CLIENT_CERTIFICATE_PASSWORD.getPropertyKey(), s)),

    /**
     * Getter function and setter biConsumer for clientCertificatePath.
     */
    CLIENT_CERTIFICATE_PATH(p -> p.getCredential().getClientCertificatePath(),
        (p, s) -> p.setProperty(AuthProperty.CLIENT_CERTIFICATE_PATH.getPropertyKey(), s)),

    /**
     * Getter function and setter biConsumer for clientId.
     */
    CLIENT_ID(p -> p.getCredential().getClientId(),
        (p, s) -> p.setProperty(AuthProperty.CLIENT_ID.getPropertyKey(), s)),

    /**
     * Getter function and setter biConsumer for clientSecret.
     */
    CLIENT_SECRET(p -> p.getCredential().getClientSecret(),
        (p, s) -> p.setProperty(AuthProperty.CLIENT_SECRET.getPropertyKey(), s)),

    /**
     * Getter function and setter biConsumer for managedIdentityEnabled.
     */
    MANAGED_IDENTITY_ENABLED(p -> String.valueOf(p.getCredential().isManagedIdentityEnabled()),
        (p, s) -> p.setProperty(AuthProperty.MANAGED_IDENTITY_ENABLED.getPropertyKey(), s)),

    /**
     * Getter function and setter biConsumer for password.
     */
    PASSWORD(p -> p.getCredential().getPassword(),
        (p, s) -> p.setProperty(AuthProperty.PASSWORD.getPropertyKey(), s)),

    /**
     * Getter function and setter biConsumer for username.
     */
    USERNAME(p -> p.getCredential().getUsername(),
        (p, s) -> p.setProperty(AuthProperty.USERNAME.getPropertyKey(), s)),

    /**
     * Getter function and setter biConsumer for tenantId.
     */
    TENANT_ID(p -> p.getProfile().getTenantId(),
        (p, s) -> p.setProperty(AuthProperty.TENANT_ID.getPropertyKey(), s)),

    /**
     * Getter function and setter biConsumer for authorityHost.
     */
    AUTHORITY_HOST(p -> p.getProfile().getEnvironment().getActiveDirectoryEndpoint(),
        (p, s) -> p.setProperty(AuthProperty.AUTHORITY_HOST.getPropertyKey(), s));

    private Function<PasswordlessProperties, String> getter;
    private BiConsumer<Properties, String> setter;

    public Function<PasswordlessProperties, String> getGetter() {
        return getter;
    }

    public BiConsumer<Properties, String> getSetter() {
        return setter;
    }

    AzurePasswordlessPropertiesMapping(Function<PasswordlessProperties, String> getter, BiConsumer<Properties,
        String> setter) {
        this.getter = getter;
        this.setter = setter;
    }
}
