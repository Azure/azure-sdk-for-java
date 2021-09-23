// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.fileshare;

import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Auto-configuration for a {@link ShareServiceClientBuilder} and file share service clients.
 */
@ConditionalOnClass(ShareServiceClientBuilder.class)
@AzureStorageFileShareAutoConfiguration.ConditionalOnStorageFileShare
public class AzureStorageFileShareAutoConfiguration extends AzureServiceConfigurationBase {

    public AzureStorageFileShareAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(AzureStorageFileShareProperties.PREFIX)
    public AzureStorageFileShareProperties azureStorageFileShareProperties() {
        return loadProperties(this.azureGlobalProperties, new AzureStorageFileShareProperties());
    }

    @Bean
    @ConditionalOnMissingBean
    public ShareServiceClient shareServiceClient(ShareServiceClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public ShareServiceAsyncClient shareServiceAsyncClient(ShareServiceClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public ShareServiceClientBuilderFactory shareServiceClientBuilderFactory(AzureStorageFileShareProperties properties) {
        return new ShareServiceClientBuilderFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ShareServiceClientBuilder shareServiceClientBuilder(ShareServiceClientBuilderFactory factory) {
        return factory.build();
    }

    /**
     * Condition indicates when storage file share should be auto-configured.
     */
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnExpression("${spring.cloud.azure.storage.fileshare.enabled:true} and ("
                                 + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.storage.fileshare.account-name:}') or "
                                 + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.storage.fileshare.endpoint:}') or "
                                 + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.storage.fileshare.connection-string:}'))")
    public @interface ConditionalOnStorageFileShare {
    }

}
