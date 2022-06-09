// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage.credential;

import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;
import com.azure.spring.cloud.service.implementation.storage.common.StorageProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class StorageSharedKeyCredentialResolverTests {

    private final StorageSharedKeyCredentialResolver resolver = new StorageSharedKeyCredentialResolver();

    @Test
    void resolveAzurePropertiesReturnNull() {
        AzureTestProperties properties = new AzureTestProperties();
        assertNull(resolver.resolve(properties));
    }

    @Test
    void resolveStoragePropertiesReturnNull() {
        TestStorageProperties properties = new TestStorageProperties();
        assertNull(resolver.resolve(properties));
    }

    @Test
    void resolveStorageProperties() {
        TestStorageProperties properties = new TestStorageProperties();
        properties.setAccountKey("test-key");
        properties.setAccountName("test-ame");
        assertNotNull(resolver.resolve(properties));
    }

    static class TestStorageProperties extends AzureHttpSdkProperties implements StorageProperties {

        private String accountName;
        private String accountKey;

        public void setAccountName(String accountName) {
            this.accountName = accountName;
        }

        public void setAccountKey(String accountKey) {
            this.accountKey = accountKey;
        }

        @Override
        public String getConnectionString() {
            return null;
        }

        public String getEndpoint() {
            return null;
        }

        @Override
        public String getAccountName() {
            return accountName;
        }

        @Override
        public String getAccountKey() {
            return accountKey;
        }

        @Override
        public String getSasToken() {
            return null;
        }
    }

    static class AzureTestProperties extends AzureHttpSdkProperties {

    }
}
