package com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.resolver;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.implementation.JdbcPluginPropertiesUtils.Mapping;
import org.springframework.util.StringUtils;

import java.util.Map;

public class NativeJdbcTokenCredentialResolver implements TokenCredentialResolver{

    @Override
    public TokenCredential resolve(Map<String,String> map) {
        if (map == null) {
            return new DefaultAzureCredentialBuilder().build();
        }

        final String tenantId = map.get(Mapping.tenantId.propertyKey());
        final String clientId = map.get(Mapping.clientId.propertyKey());
        final String clientSecret = map.get(Mapping.clientSecret.propertyKey());

        // todo replace; Spring context
        final boolean isClientIdSet = StringUtils.hasText(clientId);
        if (StringUtils.hasText(tenantId)) {

            if (isClientIdSet && StringUtils.hasText(clientSecret)) {

                return new ClientSecretCredentialBuilder().clientId(clientId)
                    .clientSecret(clientSecret)
                    .tenantId(tenantId)
                    .build();
            }

            final String clientCertificatePath = map.get(Mapping.clientCertificatePath.propertyKey());
            final String clientCertificatePassword = map.get(Mapping.clientCertificatePassword.propertyKey());
            if (StringUtils.hasText(clientCertificatePath)) {
                ClientCertificateCredentialBuilder builder = new ClientCertificateCredentialBuilder().tenantId(tenantId)
                    .clientId(clientId);

                if (StringUtils.hasText(clientCertificatePassword)) {
                    builder.pfxCertificate(clientCertificatePath, clientCertificatePassword);
                } else {
                    builder.pemCertificate(clientCertificatePath);
                }

                return builder.build();
            }
        }

        final String username = map.get(Mapping.username.propertyKey());
        final String password = map.get(Mapping.password.propertyKey());
        if (isClientIdSet && StringUtils.hasText(username)
            && StringUtils.hasText(password)) {
            return new UsernamePasswordCredentialBuilder().username(username)
                .password(password)
                .clientId(clientId)
                .tenantId(tenantId)
                .build();
        }
        final String managedIdentityEnabled = map.get(Mapping.managedIdentityEnabled.propertyKey());

        if ("true".equalsIgnoreCase(managedIdentityEnabled)) {
            ManagedIdentityCredentialBuilder builder = new ManagedIdentityCredentialBuilder();
            if (isClientIdSet) {
                builder.clientId(clientId);
            }
            return builder.build();
        }
        return new DefaultAzureCredentialBuilder().build();
    }
}
