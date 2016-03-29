/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol;

import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.OkHttpClient;

/**
 * An instance of this class provides account name/key authentication for Azure
 * Batch service.
 */
public class BatchCredentials implements ServiceClientCredentials {
    private String accountName;

    /**
     * Optional.
     *
     * @return The AccountName value.
     */
    public String getAccountName() {
        return this.accountName;
    }

    private String batchKey;

    /**
     * Optional.
     *
     * @return The StorageKey value.
     */
    public String getBatchKey() {
        return this.batchKey;
    }

    /**
     * Construct for BatchCredntials class
     *
     * @param accountName
     *            account name
     * @param batchKey
     *            account key
     */
    public BatchCredentials(String accountName, String batchKey) {

        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (batchKey == null) {
            throw new IllegalArgumentException("Parameter batchKey is required and cannot be null.");
        }

        this.accountName = accountName;
        this.batchKey = batchKey;
    }

    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {
        clientBuilder.interceptors().add(new BatchCredentialsInterceptor(this));
    }
}
