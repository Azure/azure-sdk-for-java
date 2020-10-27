// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.context.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.azure.spring.cloud.context.core.storage.StorageConnectionStringBuilder;
import com.microsoft.azure.AzureEnvironment;

import java.text.MessageFormat;
import org.junit.Test;

/**
 * @author dsibilio
 */
public class StorageConnectionStringBuilderTest {

    private static final String PROTOCOL_FORMAT = "{0}={1};";
    private static final String ACCOUNT_NAME = "accountName";
    private static final String ACCOUNT_KEY = "accountKey";
    private static final AzureEnvironment ENVIRONMENT = AzureEnvironment.AZURE;
    private static final String DEFAULT_PROTOCOL = "DefaultEndpointsProtocol";

    @Test
    public void protocolShouldBeHttpsIfSecureTransferEnabled() {
        String connectionString = StorageConnectionStringBuilder.build(ACCOUNT_NAME, ACCOUNT_KEY, ENVIRONMENT, true);
        assertThat(connectionString).contains(MessageFormat.format(PROTOCOL_FORMAT, DEFAULT_PROTOCOL, "https"));
    }

    @Test
    public void protocolShouldBeHttpIfSecureTransferDisabled() {
        String connectionString = StorageConnectionStringBuilder.build(ACCOUNT_NAME, ACCOUNT_KEY, ENVIRONMENT, false);
        assertThat(connectionString).contains(MessageFormat.format(PROTOCOL_FORMAT, DEFAULT_PROTOCOL, "http"));
    }

}
