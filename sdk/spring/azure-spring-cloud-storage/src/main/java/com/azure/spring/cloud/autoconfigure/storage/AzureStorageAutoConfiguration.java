// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.spring.cloud.autoconfigure.context.AzureResourceManager20AutoConfiguration;
import com.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.StorageAccountManager;
import com.azure.spring.cloud.context.core.storage.StorageConnectionStringProvider;
import com.azure.spring.cloud.context.core.storage.StorageEndpointStringBuilder;
import com.azure.spring.cloud.storage.AzureStorageProtocolResolver;
import com.azure.spring.cloud.telemetry.TelemetryCollector;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.azure.storage.queue.implementation.util.BuilderHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import java.util.Collections;

import static com.azure.spring.cloud.context.core.util.Constants.SPRING_CLOUD_STORAGE_BLOB_APPLICATION_ID;
import static com.azure.spring.cloud.context.core.util.Constants.SPRING_CLOUD_STORAGE_FILE_SHARE_APPLICATION_ID;

/**
 * An auto-configuration for Azure Storage Account
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureResourceManager20AutoConfiguration.class)
@ConditionalOnClass({ BlobServiceClientBuilder.class, ShareServiceClientBuilder.class })
@ConditionalOnProperty(name = "spring.cloud.azure.storage.account")
@EnableConfigurationProperties(AzureStorageProperties.class)
public class AzureStorageAutoConfiguration {
    private static final String STORAGE = "Storage";
    private static final String ACCOUNT_NAME = "AccountName";

    @Autowired(required = false)
    private StorageAccountManager storageAccountManager;

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(STORAGE);
    }

    @Bean
    @ConditionalOnMissingBean
    public BlobServiceClientBuilder blobServiceClientBuilder(AzureStorageProperties storageProperties,
                                                             EnvironmentProvider environmentProvider,
                                                             TokenCredential tokenCredential) {

        BlobServiceClientBuilder authenticatedClientBuilder = null;

        // Use storage credentials where provided, default identity otherwise.
        if (StringUtils.isNotBlank(storageProperties.getAccessKey())) {
            String connectionString;
            if (storageAccountManager != null) {
                StorageAccount storageAccount = storageAccountManager.getOrCreate(storageProperties.getAccount());
                connectionString = StorageConnectionStringProvider.getConnectionString(storageAccount,
                    environmentProvider.getEnvironment(), storageProperties.isSecureTransfer());
            } else {
                connectionString = StorageConnectionStringProvider.getConnectionString(storageProperties.getAccount(),
                    storageProperties.getAccessKey(), environmentProvider.getEnvironment());
                TelemetryCollector.getInstance().addProperty(STORAGE, ACCOUNT_NAME, storageProperties.getAccount());
            }
            authenticatedClientBuilder = new BlobServiceClientBuilder().connectionString(connectionString);

        } else {
            final String endpoint = StorageEndpointStringBuilder.buildBlobEndpoint(storageProperties.getAccount(),
                environmentProvider.getEnvironment(), storageProperties.isSecureTransfer());
            authenticatedClientBuilder = new BlobServiceClientBuilder().credential(tokenCredential)
                                                                       .endpoint(endpoint);
        }

        return authenticatedClientBuilder
            .httpLogOptions(new HttpLogOptions().setApplicationId(SPRING_CLOUD_STORAGE_BLOB_APPLICATION_ID));
    }

    @Bean
    @ConditionalOnMissingBean
    public ShareServiceClientBuilder shareServiceClientBuilder(AzureStorageProperties storageProperties,
                                                               EnvironmentProvider environmentProvider,
                                                               TokenCredential tokenCredential) {

        ShareServiceClientBuilder authenticatedClientBuilder = null;
        // Use storage credentials where provided, default identity otherwise.
        if (StringUtils.isNotBlank(storageProperties.getAccessKey())) {
            String connectionString;
            if (storageAccountManager != null) {
                String accountName = storageProperties.getAccount();

                StorageAccount storageAccount = storageAccountManager.getOrCreate(accountName);
                connectionString = StorageConnectionStringProvider.getConnectionString(storageAccount,
                    environmentProvider.getEnvironment(), storageProperties.isSecureTransfer());
            } else {
                connectionString = StorageConnectionStringProvider.getConnectionString(storageProperties.getAccount(),
                    storageProperties.getAccessKey(), environmentProvider.getEnvironment());
                TelemetryCollector.getInstance().addProperty(STORAGE, ACCOUNT_NAME, storageProperties.getAccount());
            }
            authenticatedClientBuilder = new ShareServiceClientBuilder().connectionString(connectionString);
        } else {

            String endpoint = StorageEndpointStringBuilder.buildSharesEndpoint(storageProperties.getAccount(),
                environmentProvider.getEnvironment(), storageProperties.isSecureTransfer());

            HttpPipeline pipeline = BuilderHelper.buildPipeline(null, tokenCredential, null, endpoint,
                new RequestRetryOptions(), new HttpLogOptions(), HttpClient.createDefault(),
                Collections.emptyList(), new com.azure.core.util.Configuration(),
                new ClientLogger(this.getClass()));

            authenticatedClientBuilder = new ShareServiceClientBuilder().pipeline(pipeline);

        }

        return authenticatedClientBuilder
            .httpLogOptions(new HttpLogOptions().setApplicationId(SPRING_CLOUD_STORAGE_FILE_SHARE_APPLICATION_ID));
    }

    @Configuration
    @ConditionalOnClass(AzureStorageProtocolResolver.class)
    @Import(AzureStorageProtocolResolver.class)
    static class StorageResourceConfiguration {
    }

    @Bean
    @ConditionalOnMissingBean
    public StorageAccountManager storageAccountManager(AzureResourceManager azureResourceManagement,
                                                       AzureProperties azureProperties) {
        return new StorageAccountManager(azureResourceManagement, azureProperties);
    }
}
