// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class CosmosFactoryTestIT {

    @Value("${cosmos.uri:}")
    private String cosmosDbUri;

    @Value("${cosmos.key:}")
    private String cosmosDbKey;

    @Test
    public void testNullKey() {
        CosmosAsyncClient ignored = null;
        try {
            ignored = CosmosFactory.createCosmosAsyncClient(new CosmosClientBuilder()
                .endpoint(cosmosDbUri)
                .key(null));
        } catch (Exception e) {
            assertThat(e instanceof NullPointerException).isTrue();
        } finally {
            if (ignored != null) {
                ignored.close();
            }
        }

        ignored = null;
        try {
            ignored = CosmosFactory.createCosmosAsyncClient(new CosmosClientBuilder()
                .endpoint(cosmosDbUri)
                .key(""));
        } catch (Exception e) {
            assertThat(e instanceof IllegalArgumentException).isTrue();
        } finally {
            if (ignored != null) {
                ignored.close();
            }
        }
    }

    @Test
    public void testNullEndpoint() {
        CosmosAsyncClient ignored = null;
        try {
            ignored = CosmosFactory.createCosmosAsyncClient(new CosmosClientBuilder()
                .endpoint(null)
                .key(cosmosDbKey));
        } catch (Exception e) {
            assertThat(e instanceof NullPointerException).isTrue();
        } finally {
            if (ignored != null) {
                ignored.close();
            }
        }

        ignored = null;
        try {
            ignored = CosmosFactory.createCosmosAsyncClient(new CosmosClientBuilder()
                .endpoint("")
                .key(cosmosDbKey));
        } catch (Exception e) {
            assertThat(e instanceof IllegalArgumentException).isTrue();
        } finally {
            if (ignored != null) {
                ignored.close();
            }
        }
    }

    @Test
    public void testConnectionPolicyUserAgentKept() throws IllegalAccessException, NoSuchFieldException {
        CosmosAsyncClient ignored = null;
        try {
            final CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
                .endpoint(cosmosDbUri)
                .key(cosmosDbKey);
            ignored = CosmosFactory.createCosmosAsyncClient(cosmosClientBuilder);

            final String uaSuffix = getUserAgentSuffixValue(cosmosClientBuilder);
            assertThat(uaSuffix).contains(Constants.USER_AGENT_SUFFIX);
        } finally {
            if (ignored != null) {
                ignored.close();
            }
        }
    }

    private String getUserAgentSuffixValue(CosmosClientBuilder cosmosClientBuilder) throws IllegalAccessException,
        NoSuchFieldException {
        Field userAgentSuffix = CosmosClientBuilder.class.getDeclaredField("userAgentSuffix");
        userAgentSuffix.setAccessible(true);
        return (String) userAgentSuffix.get(cosmosClientBuilder);
    }
}
