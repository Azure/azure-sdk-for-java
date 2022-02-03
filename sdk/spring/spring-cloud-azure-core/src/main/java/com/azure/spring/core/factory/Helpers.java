package com.azure.spring.core.factory;

import com.azure.core.client.traits.AzureKeyCredentialTrait;
import com.azure.core.client.traits.AzureNamedKeyCredentialTrait;
import com.azure.core.client.traits.AzureSasCredentialTrait;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.ConnectionStringTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;

import java.util.Optional;

public class Helpers {
    public static <TBuilder> TBuilder configureBuilder(TBuilder builder,
                                                        Configuration configuration,
                                                        TokenCredential defaultTokenCredential,
                                                        Optional<AzureServiceClientBuilderCustomizer<TBuilder>> builderCustomizer) {
        setCredentials(builder, configuration, defaultTokenCredential);

        if (builder instanceof ConfigurationTrait) {
            ((ConfigurationTrait<?>) builder).configuration(configuration);
        }

        if (builderCustomizer.isPresent()) {
            builderCustomizer.get().customize(builder);
        }

        return builder;
    }

    private static <TBuilder> void setCredentials(TBuilder builder,
                                                  Configuration configuration,
                                                  TokenCredential defaultTokenCredential) {

        if (builder instanceof ConnectionStringTrait && configuration.contains("connection-string")) {
            return;
        }

        if (builder instanceof AzureKeyCredentialTrait) {
            AzureKeyCredential keyCredential = AzureKeyCredential.fromConfiguration(configuration, null);
            if (keyCredential != null) {
                ((AzureKeyCredentialTrait<?>) builder).credential(keyCredential);
                return;
            }
        }

        if (builder instanceof AzureNamedKeyCredentialTrait) {
            AzureNamedKeyCredential namedKeyCredential = AzureNamedKeyCredential.fromConfiguration(configuration, null);
            if (namedKeyCredential != null) {
                ((AzureNamedKeyCredentialTrait<?>) builder).credential(namedKeyCredential);
                return;
            }
        }

        if (builder instanceof AzureSasCredentialTrait) {
            AzureSasCredential sasCredential = AzureSasCredential.fromConfiguration(configuration, null);
            if (sasCredential != null) {
                ((AzureSasCredentialTrait<?>) builder).credential(sasCredential);
                return;
            }
        }

        if (builder instanceof TokenCredentialTrait) {
            TokenCredential tokenCredential = defaultTokenCredential;

            // TODO need a better way to check if configuration section has client-specific credentials
            // alternative is to create DefaultAzureCredentialBuilder per each client builder, which is sub-optimal
            if (configuration.contains("credential.tenant-id")) {
                tokenCredential = new DefaultAzureCredentialBuilder().configuration(configuration).build();
            }

            ((TokenCredentialTrait<?>)builder).credential(tokenCredential);
        }
    }
}
