// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosEncryptionClientBuilderTest {
    private TestSuiteBase.TestEncryptionKeyStoreProvider testEncryptionKeyStoreProvider =
        new TestSuiteBase.TestEncryptionKeyStoreProvider();

    @Test(groups = "unit")
    public void validateIncorrectAsyncClientCreation() {
        CosmosAsyncClient client = Mockito.mock(CosmosAsyncClient.class);
        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;
        try {
            cosmosEncryptionAsyncClient =
                new CosmosEncryptionClientBuilder().encryptionKeyWrapProvider(testEncryptionKeyStoreProvider).buildAsyncClient();
            fail("CosmosEncryptionAsyncClient initialization should fail");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).isEqualTo("CosmosAsyncClient has not been provided.");
        }

        try {
            cosmosEncryptionAsyncClient =
                new CosmosEncryptionClientBuilder().cosmosAsyncClient(client).buildAsyncClient();
            fail("CosmosEncryptionAsyncClient initialization should fail");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).isEqualTo("EncryptionKeyWrapProvider has not been provided.");
        }

        //this should be successful
        cosmosEncryptionAsyncClient =
            new CosmosEncryptionClientBuilder().cosmosAsyncClient(client).encryptionKeyWrapProvider(testEncryptionKeyStoreProvider).buildAsyncClient();
        cosmosEncryptionAsyncClient.close();
    }

    @Test(groups = "unit")
    public void validateIncorrectSyncClientCreation() {
        CosmosClient client = Mockito.mock(CosmosClient.class);
        CosmosEncryptionClient cosmosEncryptionClient;
        try {
            cosmosEncryptionClient =
                new CosmosEncryptionClientBuilder().encryptionKeyWrapProvider(testEncryptionKeyStoreProvider).buildClient();
            fail("CosmosEncryptionClient initialization should fail");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).isEqualTo("CosmosClient has not been provided.");
        }

        try {
            cosmosEncryptionClient = new CosmosEncryptionClientBuilder().cosmosClient(client).buildClient();
            fail("CosmosEncryptionClient initialization should fail");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).isEqualTo("EncryptionKeyWrapProvider has not been provided.");
        }
    }
}
