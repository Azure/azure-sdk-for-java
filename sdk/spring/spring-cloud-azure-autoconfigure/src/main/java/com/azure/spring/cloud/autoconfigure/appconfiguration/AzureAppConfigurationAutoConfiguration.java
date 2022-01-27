// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.appconfiguration;

import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.ConfigurationProperty;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.core.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

// TODO: move to implementation

/**
 * Autoconfiguration for a {@link ConfigurationClientBuilder} and Azure App Configuration clients.
 */
@ConditionalOnClass(ConfigurationClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.appconfiguration.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.appconfiguration", name = {"endpoint", "connection-string"})
public class AzureAppConfigurationAutoConfiguration extends AzureServiceConfigurationBase {


    private static class SdkPropertySource implements com.azure.core.util.ConfigurationSource {

        private final Environment env;
        public SdkPropertySource(Environment env) {
            this.env = env;
        }

        @Override
        public Set<String> getChildKeys(String path) {
            MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
            return StreamSupport.stream(propSrcs.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                .flatMap(Arrays::<String>stream)
                .filter(propName -> propName.startsWith(path) && propName.length() > path.length() && propName.charAt(path.length()) == '.')
                .collect(Collectors.toSet());
        }

        @Override
        public String getValue(String propertyName) {
            return env.getProperty(propertyName);
        }
    }


    private final Configuration sdkConfiguration;
    public AzureAppConfigurationAutoConfiguration(AzureGlobalProperties azureGlobalProperties, Environment env) {
        super(azureGlobalProperties);
        // TODO: ConfigurationBuilder should be a Bean
        this.sdkConfiguration = new ConfigurationBuilder( new SdkPropertySource(env))
            .root(AzureGlobalProperties.PREFIX)
            .section("appconfiguration")
            .build();
    }

    /*@Bean
    @ConditionalOnMissingBean
    com.azure.core.util.ConfigurationBuilder sdkConfigurationBuilder(Environment env) {
        return new ConfigurationBuilder( new SdkPropertySource(env)).root(AzureGlobalProperties.PREFIX);
    }*/


    @Bean
    @ConditionalOnMissingBean
    public ConfigurationClient azureConfigurationClient(ConfigurationClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConfigurationAsyncClient azureConfigurtionAsyncClient(ConfigurationClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    ConfigurationClientBuilder configurationClientBuilder() {
        // TODO credentials

        return new ConfigurationClientBuilder()
            .configuration(sdkConfiguration);
    }

    // TODO
    @Bean
    @ConditionalOnProperty("spring.cloud.azure.appconfiguration.connection-string")
    public StaticConnectionStringProvider<AzureServiceType.AppConfiguration> staticAppConfigurationConnectionStringProvider() {

        return new StaticConnectionStringProvider<>(AzureServiceType.APP_CONFIGURATION, sdkConfiguration.get(ConfigurationProperty.stringPropertyBuilder("connection-string").build()));
    }
}
