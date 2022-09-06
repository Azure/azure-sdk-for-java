// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.azure.storage.common.implementation.Constants.ConfigurationConstants.Nio;

class AzureFileSystemConfig {
    // BlobServiceClient configs
    private StorageSharedKeyCredential sharedKeyCredential;
    private AzureSasCredential sasCredential;
    private HttpLogOptions logOptions = new HttpLogOptions();
    private RequestRetryOptions retryOptions;
    private HttpClient httpClient;
    private final List<HttpPipelinePolicy> policyList = new ArrayList<>();

    // nio configs
    private Long blockSize;
    private Long putBlobThreshold;
    private Integer maxConcurrencyPerRequest;
    private Integer downloadResumeRetries;
    private final List<String> fileStoreNames = new ArrayList<>();
    private Boolean skipInitialContainerCheck;

    AzureFileSystemConfig() {}

    AzureFileSystemConfig(Map<String, ?> config) {
        sharedKeyCredential = (StorageSharedKeyCredential) config.get(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL);
        sasCredential = (AzureSasCredential) config.get(AzureFileSystem.AZURE_STORAGE_SAS_TOKEN_CREDENTIAL);
        logOptions.setLogLevel(
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

    AzureFileSystemConfig(Configuration config) {
        if (!CoreUtils.isNullOrEmpty(config.get(Nio.ENVIRONMENT_DEFAULT_ACCOUNT_NAME))
            && !CoreUtils.isNullOrEmpty(config.get(Nio.ENVIRONMENT_DEFAULT_ACCOUNT_KEY))) {
            sharedKeyCredential = new StorageSharedKeyCredential(
                config.get(Nio.ENVIRONMENT_DEFAULT_ACCOUNT_NAME), config.get(Nio.ENVIRONMENT_DEFAULT_ACCOUNT_KEY));
        } else {
            sharedKeyCredential = null;
        }
        sasCredential = getConfigFromStringConfiguration(config, Nio.ENVIRONMENT_DEFAULT_SAS_TOKEN,
            AzureSasCredential::new);

        // HttpLogOptions initialized already and constructor does the environment read for us

        retryOptions = RequestRetryOptions.fromConfiguration(config,
            config.get(Nio.ENVIRONMENT_DEFAULT_BLOB_ENDPOINT_SECONDARY));
        httpClient = null; // cannot load from environment

        // cannot load policies from environment

        blockSize = getConfigFromStringConfiguration(config, Nio.ENVIRONMENT_DEFAULT_BLOCK_SIZE, Long::valueOf);
        putBlobThreshold = getConfigFromStringConfiguration(config, Nio.ENVIRONMENT_DEFAULT_PUT_BLOB_THRESHOLD,
            Long::valueOf);
        maxConcurrencyPerRequest = getConfigFromStringConfiguration(config,
            Nio.ENVIRONMENT_DEFAULT_PER_REQUEST_CONCURRENCY, Integer::valueOf);
        downloadResumeRetries = getConfigFromStringConfiguration(config, Nio.ENVIRONMENT_DEFAULT_RESUME_RETRIES,
            Integer::valueOf);

        String fileStores = config.get(Nio.ENVIRONMENT_DEFAULT_FILE_STORES);
        Collections.addAll(fileStoreNames, (fileStores != null ? fileStores : "").split(","));

        skipInitialContainerCheck = getConfigFromStringConfiguration(config,
            Nio.ENVIRONMENT_DEFAULT_SKIP_CONTAINER_CHECK, Boolean::valueOf);
    }

    public StorageSharedKeyCredential getSharedKeyCredential() {
        return sharedKeyCredential;
    }

    public AzureFileSystemConfig setSharedKeyCredential(StorageSharedKeyCredential sharedKeyCredential) {
        this.sharedKeyCredential = sharedKeyCredential;
        return this;
    }

    public AzureSasCredential getSasCredential() {
        return sasCredential;
    }

    public AzureFileSystemConfig setSasCredential(AzureSasCredential sasCredential) {
        this.sasCredential = sasCredential;
        return this;
    }

    public HttpLogOptions getLogOptions() {
        return logOptions;
    }

    public AzureFileSystemConfig setLogOptions(HttpLogOptions logOptions) {
        this.logOptions = logOptions;
        return this;
    }

    public RequestRetryOptions getRetryOptions() {
        return retryOptions;
    }

    public AzureFileSystemConfig setRetryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public AzureFileSystemConfig setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public List<HttpPipelinePolicy> getPolicyList() {
        return policyList;
    }

    public Long getBlockSize() {
        return blockSize;
    }

    public AzureFileSystemConfig setBlockSize(Long blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    public Long getPutBlobThreshold() {
        return putBlobThreshold;
    }

    public AzureFileSystemConfig setPutBlobThreshold(Long putBlobThreshold) {
        this.putBlobThreshold = putBlobThreshold;
        return this;
    }

    public Integer getMaxConcurrencyPerRequest() {
        return maxConcurrencyPerRequest;
    }

    public AzureFileSystemConfig setMaxConcurrencyPerRequest(Integer maxConcurrencyPerRequest) {
        this.maxConcurrencyPerRequest = maxConcurrencyPerRequest;
        return this;
    }

    public Integer getDownloadResumeRetries() {
        return downloadResumeRetries;
    }

    public AzureFileSystemConfig setDownloadResumeRetries(Integer downloadResumeRetries) {
        this.downloadResumeRetries = downloadResumeRetries;
        return this;
    }

    public List<String> getFileStoreNames() {
        return fileStoreNames;
    }

    public Boolean getSkipInitialContainerCheck() {
        return skipInitialContainerCheck;
    }

    public AzureFileSystemConfig setSkipInitialContainerCheck(Boolean skipInitialContainerCheck) {
        this.skipInitialContainerCheck = skipInitialContainerCheck;
        return this;
    }

    /**
     * Determines whether this config contains minimum required info to create an AzureFileSystem.
     * @return true if minimum configuration is met, false otherwise.
     */
    public boolean isSufficient() {
        return (sasCredential != null || sharedKeyCredential != null) && !fileStoreNames.isEmpty();
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
