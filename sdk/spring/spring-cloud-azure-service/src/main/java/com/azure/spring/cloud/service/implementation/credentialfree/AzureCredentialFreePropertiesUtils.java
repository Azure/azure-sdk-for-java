// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.credentialfree;

import com.azure.identity.providers.jdbc.enums.AuthProperty;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Store the constants for customized Azure properties with Kafka.
 */
public final class AzureCredentialFreePropertiesUtils {
    private AzureCredentialFreePropertiesUtils() {
    }

    private static final PropertyMapper PROPERTY_MAPPER = new PropertyMapper();

    public static void convertConfigMapToAzureProperties(Map<String, ?> source,
                                                         AzureCredentialFreeProperties target) {
        for (Mapping m : Mapping.values()) {
            PROPERTY_MAPPER.from(source.get(m.authProperty.getPropertyKey()))
                .to(p -> m.setter.accept(target, (String) p));
        }
    }

    public static void convertAzurePropertiesToConfigMap(AzureCredentialFreeProperties source,
                                                         Map<String, String> target) {
        for (Mapping m : Mapping.values()) {
            PROPERTY_MAPPER.from(m.getter.apply(source))
                .to(p -> target.putIfAbsent(m.authProperty.getPropertyKey(), p));
        }
    }

    public enum Mapping {

        clientCertificatePassword(AuthProperty.CLIENT_CERTIFICATE_PASSWORD,
            p -> p.getCredential().getClientCertificatePassword(),
            (p, s) -> p.getCredential().setClientCertificatePassword(s)),

        clientCertificatePath(AuthProperty.CLIENT_CERTIFICATE_PATH,
            p -> p.getCredential().getClientCertificatePath(),
            (p, s) -> p.getCredential().setClientCertificatePath(s)),

        clientId(AuthProperty.CLIENT_ID,
            p -> p.getCredential().getClientId(),
            (p, s) -> p.getCredential().setClientId(s)),

        clientSecret(AuthProperty.CLIENT_SECRET,
            p -> p.getCredential().getClientSecret(),
            (p, s) -> p.getCredential().setClientSecret(s)),

        managedIdentityEnabled(AuthProperty.MANAGED_IDENTITY_ENABLED,
            p -> String.valueOf(p.getCredential().isManagedIdentityEnabled()),
            (p, s) -> p.getCredential().setManagedIdentityEnabled(Boolean.valueOf(s))),

        password(AuthProperty.PASSWORD,
            p -> p.getCredential().getPassword(),
            (p, s) -> p.getCredential().setPassword(s)),

        username(AuthProperty.USERNAME,
            p -> p.getCredential().getUsername(),
            (p, s) -> p.getCredential().setUsername(s)),

        authorityHost(AuthProperty.AUTHORITY_HOST,
            p -> p.getProfile().getEnvironment().getActiveDirectoryEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setActiveDirectoryEndpoint(s)),

        tenantId(AuthProperty.TENANT_ID,
            p -> p.getProfile().getTenantId(),
            (p, s) -> p.getProfile().setTenantId(s));

        private AuthProperty authProperty;
        private Function<AzureProperties, String> getter;
        private BiConsumer<AzureCredentialFreeProperties, String> setter;

        Mapping(AuthProperty authProperty, Function<AzureProperties, String> getter, BiConsumer<AzureCredentialFreeProperties,
            String> setter) {
            this.authProperty = authProperty;
            this.getter = getter;
            this.setter = setter;
        }

        public AuthProperty getAuthProperty() {
            return authProperty;
        }

        public Function<AzureProperties, String> getGetter() {
            return getter;
        }

        public BiConsumer<AzureCredentialFreeProperties, String> getSetter() {
            return setter;
        }
    }

}
