// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.core.cryptography.KeyEncryptionKeyResolver;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.encryption.implementation.ReflectionUtils;
import com.azure.cosmos.encryption.models.CosmosEncryptionAlgorithm;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosEncryptionClientCachesTest extends TestSuiteBase {
    private CosmosAsyncClient client;
    private static final int TIMEOUT = 6000_000;
    private CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;
    private CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase;
    private CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer;
    private EncryptionKeyWrapMetadata metadata1;
    private EncryptionKeyWrapMetadata metadata2;

    @Factory(dataProvider = "clientBuilders")
    public CosmosEncryptionClientCachesTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"encryption"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildAsyncClient();
        KeyEncryptionKeyResolver keyEncryptionKeyResolver = new EncryptionAsyncApiCrudTest.TestKeyEncryptionKeyResolver();
        //Creating DB
        CosmosDatabaseProperties cosmosDatabaseProperties = this.client.createDatabase("TestDBForEncryptionCacheTest"
            , ThroughputProperties.createManualThroughput(1000)).block().getProperties();
        cosmosEncryptionAsyncClient = new CosmosEncryptionClientBuilder().cosmosAsyncClient(this.client).keyEncryptionKeyResolver(
            keyEncryptionKeyResolver).keyEncryptionKeyResolver(keyEncryptionKeyResolver).keyEncryptionKeyResolverName("TEST_KEY_RESOLVER").buildAsyncClient();
        cosmosEncryptionAsyncDatabase =
            cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(cosmosDatabaseProperties.getId());
        //Create ClientEncryptionKeys
        metadata1 = new EncryptionKeyWrapMetadata("TEST_KEY_RESOLVER", "key1", "tempmetadata1", "RSA-OAEP");
        metadata2 = new EncryptionKeyWrapMetadata("TEST_KEY_RESOLVER", "key2", "tempmetadata2", "RSA-OAEP");
        cosmosEncryptionAsyncDatabase.createClientEncryptionKey("key1",
            CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName(), metadata1).block();
        cosmosEncryptionAsyncDatabase.createClientEncryptionKey("key2",
            CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName(), metadata2).block();

        //Create collection with clientEncryptionPolicy
        ClientEncryptionPolicy clientEncryptionPolicy = new ClientEncryptionPolicy(getPaths());
        CosmosContainerProperties containerProperties = new CosmosContainerProperties("TestCollForEncryptionCacheTest"
            , "/mypk");
        containerProperties.setClientEncryptionPolicy(clientEncryptionPolicy);
        cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().createContainer(containerProperties).block();
        cosmosEncryptionAsyncContainer =
            cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(containerProperties.getId());
    }

    @Test(groups = {"encryption"}, priority = 0, timeOut = TIMEOUT)
    public void emptyCache() {
        AsyncCache<String, CosmosContainerProperties> clientEncryptionPolicyAsyncCache =  ReflectionUtils.getContainerPropertiesCacheByContainerId(cosmosEncryptionAsyncClient);
        ConcurrentHashMap<String, ?>  clientEncryptionPolicyMap= ReflectionUtils.getValueMap(clientEncryptionPolicyAsyncCache);
        assertThat(clientEncryptionPolicyMap.size()).isEqualTo(0);

        AsyncCache<String, CosmosClientEncryptionKeyProperties> clientEncryptionKeyPropertiesAsyncCache =  ReflectionUtils.getClientEncryptionKeyPropertiesCacheByKeyId(cosmosEncryptionAsyncClient);
        ConcurrentHashMap<String, ?>  clientEncryptionKeyMap= ReflectionUtils.getValueMap(clientEncryptionKeyPropertiesAsyncCache);
        assertThat(clientEncryptionKeyMap.size()).isEqualTo(0);
    }

    @Test(groups = {"encryption"}, priority = 1, timeOut = TIMEOUT)
    @SuppressWarnings("unchecked")
    public void cacheAfterInitialization() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        CosmosEncryptionAsyncClient spyCosmosEncryptionAsyncClient = Mockito.spy(cosmosEncryptionAsyncClient);
        ReflectionUtils.setCosmosEncryptionAsyncClient(cosmosEncryptionAsyncContainer.getEncryptionProcessor(), spyCosmosEncryptionAsyncClient);
        EncryptionPojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = cosmosEncryptionAsyncContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions()).block();
        Mockito.verify(spyCosmosEncryptionAsyncClient, Mockito.times(2)).fetchClientEncryptionKeyPropertiesAsync(Mockito.any(CosmosAsyncContainer.class), Mockito.anyString(), Mockito.any(RequestOptions.class));

        //Testing clientEncryptionPolicy cache
        AsyncCache<String, CosmosContainerProperties> cosmosContainerPropertiesAsyncCache =  ReflectionUtils.getContainerPropertiesCacheByContainerId(cosmosEncryptionAsyncClient);
        ConcurrentHashMap<String, ?>   cosmosContainerPropertiesMap= ReflectionUtils.getValueMap(cosmosContainerPropertiesAsyncCache);
        assertThat(cosmosContainerPropertiesMap.size()).isEqualTo(1);
        Object cosmosContainerPropertiesAsyncLazy = cosmosContainerPropertiesMap.get(cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().getId()+"/"+cosmosEncryptionAsyncContainer.getCosmosAsyncContainer().getId());

        Class<?> AsyncLazyClass = Class.forName("com.azure.cosmos.implementation.caches.AsyncLazy");
        Field cosmosContainerPropertyMonoSingle = AsyncLazyClass.getDeclaredField("single");
        cosmosContainerPropertyMonoSingle.setAccessible(true);
        Mono<CosmosContainerProperties> clientEncryptionPolicyMono = (Mono<CosmosContainerProperties>) cosmosContainerPropertyMonoSingle.get(cosmosContainerPropertiesAsyncLazy);
        CosmosContainerProperties containerProperties = clientEncryptionPolicyMono.block();
        assertThat(containerProperties.getClientEncryptionPolicy().getIncludedPaths().size()).isEqualTo(13);

        //Testing clientEncryptionKey cache
        AsyncCache<String, CosmosClientEncryptionKeyProperties> clientEncryptionKeyPropertiesAsyncCache =  ReflectionUtils.getClientEncryptionKeyPropertiesCacheByKeyId(cosmosEncryptionAsyncClient);
        ConcurrentHashMap<String, ?>  clientEncryptionKeyMap= ReflectionUtils.getValueMap(clientEncryptionKeyPropertiesAsyncCache);
        assertThat(clientEncryptionKeyMap.size()).isEqualTo(2);

        String databaseRid = cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().read().block().getProperties().getResourceId();
        Object ClientEncryptionKeyAyncLazy1 = clientEncryptionKeyMap.get(databaseRid+"/"+"key1");
        AsyncLazyClass = Class.forName("com.azure.cosmos.implementation.caches.AsyncLazy");
        Field clientEncryptionKeyMonoSingle1 = AsyncLazyClass.getDeclaredField("single");
        clientEncryptionKeyMonoSingle1.setAccessible(true);
        Mono<CosmosClientEncryptionKeyProperties> clientEncryptionKeyMono1 = (Mono<CosmosClientEncryptionKeyProperties>) clientEncryptionKeyMonoSingle1.get(ClientEncryptionKeyAyncLazy1);
        CosmosClientEncryptionKeyProperties clientEncryptionKey1 = clientEncryptionKeyMono1.block();
        assertThat(clientEncryptionKey1.getEncryptionKeyWrapMetadata().getName()).isEqualTo("key1");

        Object ClientEncryptionKeyAyncLazy2 = clientEncryptionKeyMap.get(databaseRid+"/"+"key2");
        AsyncLazyClass = Class.forName("com.azure.cosmos.implementation.caches.AsyncLazy");
        Field clientEncryptionKeyMonoSingle2 = AsyncLazyClass.getDeclaredField("single");
        clientEncryptionKeyMonoSingle2.setAccessible(true);
        spyCosmosEncryptionAsyncClient = Mockito.spy(cosmosEncryptionAsyncClient);
        Mono<CosmosClientEncryptionKeyProperties> clientEncryptionKeyMono2 = (Mono<CosmosClientEncryptionKeyProperties>) clientEncryptionKeyMonoSingle2.get(ClientEncryptionKeyAyncLazy2);
        CosmosClientEncryptionKeyProperties clientEncryptionKey2 = clientEncryptionKeyMono2.block();
        assertThat(clientEncryptionKey2.getEncryptionKeyWrapMetadata().getName()).isEqualTo("key2");

        cosmosEncryptionAsyncContainer =
            cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer("TestCollForEncryptionCacheTest");
        spyCosmosEncryptionAsyncClient = Mockito.spy(cosmosEncryptionAsyncClient);
        ReflectionUtils.setCosmosEncryptionAsyncClient(cosmosEncryptionAsyncContainer.getEncryptionProcessor(), spyCosmosEncryptionAsyncClient);
        cosmosEncryptionAsyncContainer.readItem(properties.getId(),
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions(), EncryptionCodeSnippet.Pojo.class).block();
        Mockito.verify(spyCosmosEncryptionAsyncClient, Mockito.times(0)).fetchClientEncryptionKeyPropertiesAsync(Mockito.any(CosmosAsyncContainer.class), Mockito.anyString(), Mockito.any(RequestOptions.class));
    }

    @AfterClass(groups = {"encryption"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        assertThat(this.cosmosEncryptionAsyncDatabase).isNotNull();
        cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().delete().block();
        this.client.close();
    }
}
