/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.auth;

public class BatchSharedKeyCredentials extends BatchCredentials {

    private String accountName;

    private String keyValue;


    public String getAccountName() {
        return accountName;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public BatchSharedKeyCredentials(String baseUrl, String accountName, String keyValue) {

        if (baseUrl == null) {
            throw new IllegalArgumentException("Parameter baseUrl is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (keyValue == null) {
            throw new IllegalArgumentException("Parameter keyValue is required and cannot be null.");
        }

        this.setBaseUrl(baseUrl);
        this.accountName = accountName;
        this.keyValue = keyValue;
    }
}
