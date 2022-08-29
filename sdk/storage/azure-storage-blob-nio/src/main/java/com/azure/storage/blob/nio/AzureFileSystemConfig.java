package com.azure.storage.blob.nio;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;

import java.nio.file.FileStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class AzureFileSystemConfig {
    // BlobServiceClient configs
    final StorageSharedKeyCredential sharedKeyCredential;
    final AzureSasCredential sasCredential;
    final HttpLogDetailLevel logDetailLevel;
    final RetryPolicyType retryPolicyType;
    final Integer maxRetries;
    final Integer tryTimeout;
    final Long retryDelayMs;
    final Long maxRetryDelayMs;
    final String secondaryHost;
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
        logDetailLevel = (HttpLogDetailLevel) config.get(AzureFileSystem.AZURE_STORAGE_HTTP_LOG_DETAIL_LEVEL);
        retryPolicyType = (RetryPolicyType) config.get(AzureFileSystem.AZURE_STORAGE_RETRY_POLICY_TYPE);
        maxRetries = (Integer) config.get(AzureFileSystem.AZURE_STORAGE_MAX_TRIES);
        tryTimeout = (Integer) config.get(AzureFileSystem.AZURE_STORAGE_TRY_TIMEOUT);
        retryDelayMs = (Long) config.get(AzureFileSystem.AZURE_STORAGE_RETRY_DELAY_IN_MS);
        maxRetryDelayMs = (Long) config.get(AzureFileSystem.AZURE_STORAGE_MAX_RETRY_DELAY_IN_MS);
        secondaryHost = (String) config.get(AzureFileSystem.AZURE_STORAGE_SECONDARY_HOST);
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
        throw new RuntimeException("not implemented");
    }

    public Map<String, ?> toMap() {
        Map<String, Object> result = new HashMap<>();

        if (sharedKeyCredential != null) result.put(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL, sharedKeyCredential);
        if (sasCredential != null) result.put(AzureFileSystem.AZURE_STORAGE_SAS_TOKEN_CREDENTIAL, sasCredential);
        if (logDetailLevel != null) result.put(AzureFileSystem.AZURE_STORAGE_HTTP_LOG_DETAIL_LEVEL, logDetailLevel);
        if (retryPolicyType != null) result.put(AzureFileSystem.AZURE_STORAGE_RETRY_POLICY_TYPE, retryPolicyType);
        if (maxRetries != null) result.put(AzureFileSystem.AZURE_STORAGE_MAX_TRIES, maxRetries);
        if (tryTimeout != null) result.put(AzureFileSystem.AZURE_STORAGE_TRY_TIMEOUT, tryTimeout);
        if (retryDelayMs != null) result.put(AzureFileSystem.AZURE_STORAGE_RETRY_DELAY_IN_MS, retryDelayMs);
        if (maxRetryDelayMs != null) result.put(AzureFileSystem.AZURE_STORAGE_MAX_RETRY_DELAY_IN_MS, maxRetryDelayMs);
        if (secondaryHost != null) result.put(AzureFileSystem.AZURE_STORAGE_SECONDARY_HOST, secondaryHost);
        if (httpClient != null) result.put(AzureFileSystem.AZURE_STORAGE_HTTP_CLIENT, httpClient);
        if (!policyList.isEmpty()) result.put(AzureFileSystem.AZURE_STORAGE_HTTP_POLICIES, policyList.toArray());
        if (blockSize != null) result.put(AzureFileSystem.AZURE_STORAGE_UPLOAD_BLOCK_SIZE, blockSize);
        if (putBlobThreshold != null) result.put(AzureFileSystem.AZURE_STORAGE_PUT_BLOB_THRESHOLD, putBlobThreshold);
        if (maxConcurrencyPerRequest != null) result.put(AzureFileSystem.AZURE_STORAGE_MAX_CONCURRENCY_PER_REQUEST, maxConcurrencyPerRequest);
        if (downloadResumeRetries != null) result.put(AzureFileSystem.AZURE_STORAGE_DOWNLOAD_RESUME_RETRIES, downloadResumeRetries);
        if (!fileStoreNames.isEmpty()) result.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, String.join(",", fileStoreNames));
        if (skipInitialContainerCheck != null) result.put(AzureFileSystem.AZURE_STORAGE_SKIP_INITIAL_CONTAINER_CHECK, skipInitialContainerCheck);

        return result;
    }

    public HttpLogOptions getLogOptions() {
        return BlobServiceClientBuilder.getDefaultHttpLogOptions().setLogLevel(logDetailLevel);
    }

    public RequestRetryOptions getRetryOptions() {
        return new RequestRetryOptions(
            retryPolicyType,
            maxRetries,
            tryTimeout,
            retryDelayMs,
            maxRetryDelayMs,
            secondaryHost);
    }
}
