// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.storage.credential;

import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.service.storage.common.StorageProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StorageSharedKeyCredentialResolverTests {

    private StorageSharedKeyCredentialResolver resolver = new StorageSharedKeyCredentialResolver();

    @Test
    void resolveAzurePropertiesReturnNull() {
        TestStorageProperties properties = new TestStorageProperties();
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

    class TestStorageProperties implements StorageProperties {

        private String accountName;
        private String accountKey;

        public void setAccountName(String accountName) {
            this.accountName = accountName;
        }

        public void setAccountKey(String accountKey) {
            this.accountKey = accountKey;
        }

        @Override
        public Client getClient() {
            return null;
        }

        @Override
        public String getConnectionString() {
            return null;
        }

        @Override
        public TokenCredential getCredential() {
            return null;
        }

        @Override
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
        public Profile getProfile() {
            return null;
        }

        @Override
        public Proxy getProxy() {
            return null;
        }

        @Override
        public Retry getRetry() {
            return null;
        }

        @Override
        public String getSasToken() {
            return null;
        }
    }

    class TestAzureProperties implements AzureProperties  {

        @Override
        public Client getClient() {
            return null;
        }

        @Override
        public TokenCredential getCredential() {
            return null;
        }

        @Override
        public Profile getProfile() {
            return null;
        }

        @Override
        public Proxy getProxy() {
            return null;
        }

        @Override
        public Retry getRetry() {
            return null;
        }
    }
}
