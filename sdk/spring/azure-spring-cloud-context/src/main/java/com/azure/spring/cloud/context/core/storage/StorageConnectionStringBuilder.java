// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.context.core.storage;

import com.azure.core.management.AzureEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The builder class to build the storage connection string.
 */
public class StorageConnectionStringBuilder {

    private static final String HTTP = "http";

    private static final String HTTPS = "https";

    private static final String DEFAULT_PROTOCOL = "DefaultEndpointsProtocol";

    private static final String ACCOUNT_NAME = "AccountName";

    private static final String ACCOUNT_KEY = "AccountKey";

    private static final String ENDPOINT_SUFFIX = "EndpointSuffix";

    private static final String SEPARATOR = ";";

    private static String build(String accountName, Optional<String> accountKey, AzureEnvironment environment,
            boolean isSecureTransfer) {
        Map<String, String> map = new HashMap<>();
        map.put(DEFAULT_PROTOCOL, resolveProtocol(isSecureTransfer));
        map.put(ACCOUNT_NAME, accountName);

        accountKey.ifPresent(s -> map.put(ACCOUNT_KEY, s));
        // Remove starting dot since AzureEnvironment.storageEndpointSuffix() starts
        // with dot
        map.put(ENDPOINT_SUFFIX, environment.getStorageEndpointSuffix().substring(1));

        return map.entrySet().stream().map(Object::toString).collect(Collectors.joining(SEPARATOR));
    }

    public static String build(String accountName, String accountKey, AzureEnvironment environment,
            boolean isSecureTransfer) {
        return build(accountName, Optional.of(accountKey), environment, isSecureTransfer);
    }

    public static String build(String accountName, String accountKey, AzureEnvironment environment) {
        return build(accountName, Optional.of(accountKey), environment, true);
    }

    public static String build(String accountName, AzureEnvironment environment, boolean isSecureTransfer) {
        return build(accountName, Optional.empty(), environment, isSecureTransfer);
    }

    private static String resolveProtocol(boolean isSecureTransfer) {
        return isSecureTransfer ? HTTPS : HTTP;
    }
}
