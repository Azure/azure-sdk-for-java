// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.storage;

import static com.microsoft.azure.spring.cloud.context.core.util.Constants.SPRING_CLOUD_STORAGE_BLOB_APPLICATION_ID;
import static com.microsoft.azure.spring.cloud.context.core.util.Constants.SPRING_CLOUD_STORAGE_FILE_SHARE_APPLICATION_ID;

import java.util.Collections;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.implementation.util.BuilderHelper;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureResourceManager20AutoConfiguration;
import com.microsoft.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;
import com.microsoft.azure.spring.cloud.context.core.impl.StorageAccountManager;
import com.microsoft.azure.spring.cloud.context.core.storage.StorageConnectionStringProvider;
import com.microsoft.azure.spring.cloud.context.core.storage.StorageEndpointStringBuilder;
import com.microsoft.azure.spring.cloud.storage.AzureStorageProtocolResolver;
import com.microsoft.azure.spring.cloud.telemetry.TelemetryCollector;

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
    private static final Logger LOG = LoggerFactory.getLogger(AzureStorageAutoConfiguration.class);
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
            EnvironmentProvider environmentProvider, TokenCredential tokenCredential) {

        BlobServiceClientBuilder authenticatedClientBuilder = null;

        // Use storage credentials where provided, default identity otherwise.
        if (StringUtils.isNotBlank(storageProperties.getAccessKey())) {
            String connectionString = null;
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
            authenticatedClientBuilder = new BlobServiceClientBuilder().credential(tokenCredential)
                    .endpoint(StorageEndpointStringBuilder.buildBlobEndpoint(storageProperties.getAccount(),
                            environmentProvider.getEnvironment(), storageProperties.isSecureTransfer()));
        }

        return authenticatedClientBuilder
                .httpLogOptions(new HttpLogOptions().setApplicationId(SPRING_CLOUD_STORAGE_BLOB_APPLICATION_ID));
    }

    @Bean
    @ConditionalOnMissingBean
    public ShareServiceClientBuilder shareServiceClientBuilder(AzureStorageProperties storageProperties,
            EnvironmentProvider environmentProvider, TokenCredential tokenCredential) {

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
    public StorageAccountManager storageAccountManager(AzureResourceManager azureResourceManagement, AzureProperties azureProperties) {
        return new StorageAccountManager(azureResourceManagement, azureProperties);
    }
}
