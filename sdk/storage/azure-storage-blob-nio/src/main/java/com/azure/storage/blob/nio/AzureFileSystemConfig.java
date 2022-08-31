package com.azure.storage.blob.nio;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationProperty;
import com.azure.core.util.ConfigurationPropertyBuilder;
import com.azure.core.util.CoreUtils;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.azure.storage.common.implementation.Constants.NioConstants.*;

class AzureFileSystemConfig {
    // BlobServiceClient configs
    final StorageSharedKeyCredential sharedKeyCredential;
    final AzureSasCredential sasCredential;
    final HttpLogOptions logOptions;
    final RequestRetryOptions retryOptions;
    final HttpClient httpClient;
    final List<HttpPipelinePolicy> policyList = new ArrayList<>();

    // nio configs
    final Long blockSize;
    final Long putBlobThreshold;
    final Integer maxConcurrencyPerRequest;
    final Integer downloadResumeRetries;
    final List<String> fileStoreNames = new ArrayList<>();
    final Boolean skipInitialContainerCheck;

    public AzureFileSystemConfig(Map<String, ?> config) {
        sharedKeyCredential = (StorageSharedKeyCredential) config.get(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL);
        sasCredential = (AzureSasCredential) config.get(AzureFileSystem.AZURE_STORAGE_SAS_TOKEN_CREDENTIAL);
        logOptions = new HttpLogOptions().setLogLevel(
            (HttpLogDetailLevel) config.get(AzureFileSystem.AZURE_STORAGE_HTTP_LOG_DETAIL_LEVEL));
        retryOptions = new RequestRetryOptions(
            (RetryPolicyType) config.get(AzureFileSystem.AZURE_STORAGE_RETRY_POLICY_TYPE),
            (Integer) config.get(AzureFileSystem.AZURE_STORAGE_MAX_TRIES),
            (Integer) config.get(AzureFileSystem.AZURE_STORAGE_TRY_TIMEOUT),
            (Long) config.get(AzureFileSystem.AZURE_STORAGE_RETRY_DELAY_IN_MS),
            (Long) config.get(AzureFileSystem.AZURE_STORAGE_MAX_RETRY_DELAY_IN_MS),
            (String) config.get(AzureFileSystem.AZURE_STORAGE_SECONDARY_HOST));
        httpClient = (HttpClient) config.get(AzureFileSystem.AZURE_STORAGE_HTTP_CLIENT);

        HttpPipelinePolicy[] policyArray = (HttpPipelinePolicy[]) config.get(AzureFileSystem.AZURE_STORAGE_HTTP_POLICIES);
        Collections.addAll(policyList, policyArray != null ? policyArray : new HttpPipelinePolicy[0]);

        blockSize = (Long) config.get(AzureFileSystem.AZURE_STORAGE_UPLOAD_BLOCK_SIZE);
        putBlobThreshold = (Long) config.get(AzureFileSystem.AZURE_STORAGE_PUT_BLOB_THRESHOLD);
        maxConcurrencyPerRequest = (Integer) config.get(AzureFileSystem.AZURE_STORAGE_MAX_CONCURRENCY_PER_REQUEST);
        downloadResumeRetries = (Integer) config.get(AzureFileSystem.AZURE_STORAGE_DOWNLOAD_RESUME_RETRIES);

        String fileStores = (String) config.get(AzureFileSystem.AZURE_STORAGE_FILE_STORES);
        Collections.addAll(fileStoreNames, (fileStores != null ? fileStores : "").split(","));

        skipInitialContainerCheck = (Boolean) config.get(AzureFileSystem.AZURE_STORAGE_SKIP_INITIAL_CONTAINER_CHECK);
    }

    public AzureFileSystemConfig(Configuration config) {
        if (!CoreUtils.isNullOrEmpty(config.get(ENVIRONMENT_DEFAULT_ACCOUNT_NAME)) &&
            !CoreUtils.isNullOrEmpty(config.get(ENVIRONMENT_DEFAULT_ACCOUNT_KEY))) {
            sharedKeyCredential = new StorageSharedKeyCredential(
                config.get(ENVIRONMENT_DEFAULT_ACCOUNT_NAME), config.get(ENVIRONMENT_DEFAULT_ACCOUNT_KEY));
        } else {
            sharedKeyCredential = null;
        }
        sasCredential = getConfigFromStringConfiguration(config, ENVIRONMENT_DEFAULT_SAS_TOKEN,
            AzureSasCredential::new);

        // HttpLogOptions constructor does the environment read for us
        logOptions = new HttpLogOptions();
        retryOptions = RequestRetryOptions.fromConfiguration(config,
            config.get(ENVIRONMENT_DEFAULT_BLOB_ENDPOINT_SECONDARY));
        httpClient = null; // cannot load from environment

        // cannot load policies from environment

        blockSize = getConfigFromStringConfiguration(config, ENVIRONMENT_DEFAULT_BLOCK_SIZE, Long::valueOf);
        putBlobThreshold = getConfigFromStringConfiguration(config, ENVIRONMENT_DEFAULT_PUT_BLOB_THRESHOLD,
            Long::valueOf);
        maxConcurrencyPerRequest = getConfigFromStringConfiguration(config, ENVIRONMENT_DEFAULT_PER_REQUEST_CONCURRENCY,
            Integer::valueOf);
        downloadResumeRetries = getConfigFromStringConfiguration(config, ENVIRONMENT_DEFAULT_RESUME_RETRIES,
            Integer::valueOf);

        String fileStores = config.get(ENVIRONMENT_DEFAULT_FILE_STORES);
        Collections.addAll(fileStoreNames, (fileStores != null ? fileStores : "").split(","));

        skipInitialContainerCheck = getConfigFromStringConfiguration(config, ENVIRONMENT_DEFAULT_SKIP_CONTAINER_CHECK,
            Boolean::valueOf);
    }

    private static <T> T getConfigFromStringConfiguration(
        Configuration config, String key, Function<String, T> parseValue) {
        String rawValue = config.get(key);
        if (!CoreUtils.isNullOrEmpty(rawValue)) {
            return parseValue.apply(rawValue);
        }
        return null;
    }
}
