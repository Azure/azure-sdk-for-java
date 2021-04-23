// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.EncryptionCodeSnippet;
import com.azure.cosmos.encryption.models.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.implementation.ReflectionUtils;
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
import com.azure.cosmos.rx.TestSuiteBase;
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

        //Creating DB
        CosmosDatabaseProperties cosmosDatabaseProperties = this.client.createDatabase("TestDBForEncryptionCacheTest"
            , ThroughputProperties.createManualThroughput(1000)).block().getProperties();
        cosmosEncryptionAsyncClient = CosmosEncryptionAsyncClient.createCosmosEncryptionAsyncClient(this.client,
            new EncryptionCrudTest.TestEncryptionKeyStoreProvider());
        cosmosEncryptionAsyncDatabase =
            cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(cosmosDatabaseProperties.getId());
        //Create ClientEncryptionKeys
        metadata1 = new EncryptionKeyWrapMetadata("key1", "tempmetadata1");
        metadata2 = new EncryptionKeyWrapMetadata("key2", "tempmetadata2");
        cosmosEncryptionAsyncDatabase.createClientEncryptionKey("key1",
            CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, metadata1).block();
        cosmosEncryptionAsyncDatabase.createClientEncryptionKey("key2",
            CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, metadata2).block();

        //Create collection with clientEncryptionPolicy
        ClientEncryptionPolicy clientEncryptionPolicy = new ClientEncryptionPolicy(EncryptionCrudTest.getPaths());
        CosmosContainerProperties containerProperties = new CosmosContainerProperties("TestCollForEncryptionCacheTest"
            , "/mypk");
        containerProperties.setClientEncryptionPolicy(clientEncryptionPolicy);
        cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().createContainer(containerProperties).block();
        cosmosEncryptionAsyncContainer =
            cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(containerProperties.getId());
    }

    @Test(groups = {"encryption"}, priority = 0, timeOut = TIMEOUT)
    public void emptyCache() {
        AsyncCache<String, ClientEncryptionPolicy> clientEncryptionPolicyAsyncCache =  ReflectionUtils.getClientEncryptionPolicyCacheByContainerId(cosmosEncryptionAsyncClient);
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
        EncryptionCrudTest.Pojo properties = EncryptionCrudTest.getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionCrudTest.Pojo> itemResponse = cosmosEncryptionAsyncContainer.createItem(properties,
            new PartitionKey(properties.mypk), new CosmosItemRequestOptions()).block();
        Mockito.verify(spyCosmosEncryptionAsyncClient, Mockito.times(2)).fetchClientEncryptionKeyPropertiesAsync(Mockito.any(CosmosAsyncContainer.class), Mockito.anyString());

        //Testing clientEncryptionPolicy cache
        AsyncCache<String, ClientEncryptionPolicy> clientEncryptionPolicyAsyncCache =  ReflectionUtils.getClientEncryptionPolicyCacheByContainerId(cosmosEncryptionAsyncClient);
        ConcurrentHashMap<String, ?>  clientEncryptionPolicyMap= ReflectionUtils.getValueMap(clientEncryptionPolicyAsyncCache);
        assertThat(clientEncryptionPolicyMap.size()).isEqualTo(1);
        Object clientEncryptionPolicyAyncLazy = clientEncryptionPolicyMap.get(cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().getId()+"/"+cosmosEncryptionAsyncContainer.getCosmosAsyncContainer().getId());

        Class<?> AsyncLazyClass = Class.forName("com.azure.cosmos.implementation.caches.AsyncLazy");
        Field clientEncryptionPolicyMonoSingle = AsyncLazyClass.getDeclaredField("single");
        clientEncryptionPolicyMonoSingle.setAccessible(true);
        Mono<ClientEncryptionPolicy> clientEncryptionPolicyMono = (Mono<ClientEncryptionPolicy>) clientEncryptionPolicyMonoSingle.get(clientEncryptionPolicyAyncLazy);
        ClientEncryptionPolicy clientEncryptionPolicy = clientEncryptionPolicyMono.block();
        assertThat(clientEncryptionPolicy.getIncludedPaths().size()).isEqualTo(13);

        //Testing clientEncryptionKey cache
        AsyncCache<String, CosmosClientEncryptionKeyProperties> clientEncryptionKeyPropertiesAsyncCache =  ReflectionUtils.getClientEncryptionKeyPropertiesCacheByKeyId(cosmosEncryptionAsyncClient);
        ConcurrentHashMap<String, ?>  clientEncryptionKeyMap= ReflectionUtils.getValueMap(clientEncryptionKeyPropertiesAsyncCache);
        assertThat(clientEncryptionKeyMap.size()).isEqualTo(2);

        Object ClientEncryptionKeyAyncLazy1 = clientEncryptionKeyMap.get(cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().getId()+"/"+"key1");
        AsyncLazyClass = Class.forName("com.azure.cosmos.implementation.caches.AsyncLazy");
        Field clientEncryptionKeyMonoSingle1 = AsyncLazyClass.getDeclaredField("single");
        clientEncryptionKeyMonoSingle1.setAccessible(true);
        Mono<CosmosClientEncryptionKeyProperties> clientEncryptionKeyMono1 = (Mono<CosmosClientEncryptionKeyProperties>) clientEncryptionKeyMonoSingle1.get(ClientEncryptionKeyAyncLazy1);
        CosmosClientEncryptionKeyProperties clientEncryptionKey1 = clientEncryptionKeyMono1.block();
        assertThat(clientEncryptionKey1.getEncryptionKeyWrapMetadata().getName()).isEqualTo("key1");

        Object ClientEncryptionKeyAyncLazy2 = clientEncryptionKeyMap.get(cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().getId()+"/"+"key2");
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
        cosmosEncryptionAsyncContainer.readItem(properties.id,
            new PartitionKey(properties.mypk), new CosmosItemRequestOptions(), EncryptionCodeSnippet.Pojo.class).block();
        Mockito.verify(spyCosmosEncryptionAsyncClient, Mockito.times(0)).fetchClientEncryptionKeyPropertiesAsync(Mockito.any(CosmosAsyncContainer.class), Mockito.anyString());
    }

    @AfterClass(groups = {"encryption"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        assertThat(this.cosmosEncryptionAsyncDatabase).isNotNull();
        cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().delete().block();
        this.client.close();
    }
}
