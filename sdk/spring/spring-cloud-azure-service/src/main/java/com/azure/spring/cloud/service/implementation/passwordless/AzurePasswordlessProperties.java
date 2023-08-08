// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.passwordless;

import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.cloud.core.properties.client.ClientProperties;
import com.azure.spring.cloud.core.properties.profile.AzureProfileProperties;
import com.azure.spring.cloud.core.properties.proxy.ProxyProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import com.azure.spring.cloud.core.provider.authentication.TokenCredentialOptionsProvider;

import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Implement {@link TokenCredentialOptionsProvider} and {@link AzureProfileOptionsProvider} for Spring Cloud Azure
 * support for other third party services.
 */
public class AzurePasswordlessProperties implements AzureProperties {

    private AzureProfileProperties profile = new AzureProfileProperties();

    private String scopes;

    private TokenCredentialProperties credential = new TokenCredentialProperties();

    // Use client options inside credential for azure identity
    private ClientProperties client = new ClientProperties();

    // Use proxy options inside credential for azure identity
    private ProxyProperties proxy = new ProxyProperties();

    // Whether to enable supporting azure identity token credentials
    private boolean passwordlessEnabled = false;

    @Override
    public AzureProfileProperties getProfile() {
        return profile;
    }

    public void setProfile(AzureProfileProperties profile) {
        this.profile = profile;
    }

    @Override
    public TokenCredentialProperties getCredential() {
        return credential;
    }

    public void setCredential(TokenCredentialProperties credential) {
        this.credential = credential;
    }

    @Override
    public ClientOptions getClient() {
        return client;
    }

    public void setClient(ClientProperties client) {
        this.client = client;
    }

    @Override
    public ProxyOptions getProxy() {
        return proxy;
    }

    public void setProxy(ProxyProperties proxy) {
        this.proxy = proxy;
    }

    public boolean isPasswordlessEnabled() {
        return passwordlessEnabled;
    }

    public void setPasswordlessEnabled(boolean passwordlessEnabled) {
        this.passwordlessEnabled = passwordlessEnabled;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public Properties toProperties() {
        Properties target = new Properties();
        for (AzurePasswordlessPropertiesMapping m : AzurePasswordlessPropertiesMapping.values()) {
            if (m.getter.apply(this) != null) {
                m.setter.accept(target, m.getter.apply(this));
            }
        }
        return target;
    }

    private enum AzurePasswordlessPropertiesMapping {

        SCOPES(p -> p.getScopes(),
            (p, s) -> p.setProperty(AuthProperty.SCOPES.getPropertyKey(), s)),

        CLIENT_CERTIFICATE_PASSWORD(p -> p.getCredential().getClientCertificatePassword(),
            (p, s) -> p.setProperty(AuthProperty.CLIENT_CERTIFICATE_PASSWORD.getPropertyKey(), s)),

        CLIENT_CERTIFICATE_PATH(p -> p.getCredential().getClientCertificatePath(),
            (p, s) -> p.setProperty(AuthProperty.CLIENT_CERTIFICATE_PATH.getPropertyKey(), s)),

        CLIENT_ID(p -> p.getCredential().getClientId(),
            (p, s) -> p.setProperty(AuthProperty.CLIENT_ID.getPropertyKey(), s)),

        CLIENT_SECRET(p -> p.getCredential().getClientSecret(),
            (p, s) -> p.setProperty(AuthProperty.CLIENT_SECRET.getPropertyKey(), s)),

        MANAGED_IDENTITY_ENABLED(p -> String.valueOf(p.getCredential().isManagedIdentityEnabled()),
            (p, s) -> p.setProperty(AuthProperty.MANAGED_IDENTITY_ENABLED.getPropertyKey(), s)),

        PASSWORD(p -> p.getCredential().getPassword(),
            (p, s) -> p.setProperty(AuthProperty.PASSWORD.getPropertyKey(), s)),

        USERNAME(p -> p.getCredential().getUsername(),
            (p, s) -> p.setProperty(AuthProperty.USERNAME.getPropertyKey(), s)),

        TENANT_ID(p -> p.getProfile().getTenantId(),
            (p, s) -> p.setProperty(AuthProperty.TENANT_ID.getPropertyKey(), s)),

        AUTHORITY_HOST(p -> p.getProfile().getEnvironment().getActiveDirectoryEndpoint(),
            (p, s) -> p.setProperty(AuthProperty.AUTHORITY_HOST.getPropertyKey(), s));

        private Function<AzurePasswordlessProperties, String> getter;
        private BiConsumer<Properties, String> setter;

        AzurePasswordlessPropertiesMapping(Function<AzurePasswordlessProperties, String> getter, BiConsumer<Properties,
            String> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public Function<AzurePasswordlessProperties, String> getter() {
            return getter;
        }

        public BiConsumer<Properties, String> setter() {
            return setter;
        }

    }
}
