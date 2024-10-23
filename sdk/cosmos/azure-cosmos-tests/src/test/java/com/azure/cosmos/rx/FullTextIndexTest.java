package com.azure.cosmos.rx;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosFullTextIndex;
import com.azure.cosmos.models.CosmosFullTextPath;
import com.azure.cosmos.models.CosmosFullTextPolicy;
import com.azure.cosmos.models.ExcludedPath;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Ignore("TODO: Ignore these test cases until the public emulator with vector indexes is released.")
public class FullTextIndexTest extends TestSuiteBase{
    protected static final int TIMEOUT = 30000;
    protected static final int SETUP_TIMEOUT = 20000;
    protected static final int SHUTDOWN_TIMEOUT = 20000;

    protected static Logger logger = LoggerFactory.getLogger(VectorIndexTest.class.getSimpleName());
    private final ObjectMapper simpleObjectMapper = Utils.getSimpleObjectMapper();
    private final String databaseId = CosmosDatabaseForTest.generateId();
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_VectorIndexTest() {
        // set up the client
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode(DirectConnectionConfig.getDefaultConfig())
            .consistencyLevel(ConsistencyLevel.SESSION)
            .contentResponseOnWriteEnabled(true)
            .buildAsyncClient();

        database = createDatabase(client, databaseId);
    }

    @AfterClass(groups = {"emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(client);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT*10000)
    public void shouldCreateFullTextPolicy() throws Exception {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
        ExcludedPath excludedPath = new ExcludedPath("/*");
        indexingPolicy.setExcludedPaths(Collections.singletonList(excludedPath));

        IncludedPath includedPath1 = new IncludedPath("/name/?");
        IncludedPath includedPath2 = new IncludedPath("/description/?");
        indexingPolicy.setIncludedPaths(ImmutableList.of(includedPath1, includedPath2));

        CosmosFullTextIndex cosmosFullTextIndex = new CosmosFullTextIndex("/fts1");
        indexingPolicy.setCosmosFullTextIndexes(Arrays.asList(cosmosFullTextIndex));

        CosmosFullTextPath cosmosFullTextPath = new CosmosFullTextPath();
        cosmosFullTextPath.setPath("/fts1");
        cosmosFullTextPath.setLanguage("en-US");
        CosmosFullTextPolicy cosmosFullTextPolicy = new CosmosFullTextPolicy();
        cosmosFullTextPolicy.setPaths(Arrays.asList(cosmosFullTextPath));
        cosmosFullTextPolicy.setDefaultLanguage("en-US");

        collectionDefinition.setIndexingPolicy(indexingPolicy);
        collectionDefinition.setFullTextPolicy(cosmosFullTextPolicy);

        database.createContainer(collectionDefinition).block();
        CosmosAsyncContainer createdCollection = database.getContainer(collectionDefinition.getId());
        CosmosContainerProperties collectionProperties = createdCollection.read().block().getProperties();
        validateCollectionProperties(collectionDefinition, collectionProperties);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void shouldCreateFullTextPolicyWithDefaultLanguage() throws Exception {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
        ExcludedPath excludedPath = new ExcludedPath("/*");
        indexingPolicy.setExcludedPaths(Collections.singletonList(excludedPath));

        IncludedPath includedPath1 = new IncludedPath("/name/?");
        IncludedPath includedPath2 = new IncludedPath("/description/?");
        indexingPolicy.setIncludedPaths(ImmutableList.of(includedPath1, includedPath2));

        CosmosFullTextIndex cosmosFullTextIndex = new CosmosFullTextIndex("/fts1");
        indexingPolicy.setCosmosFullTextIndexes(Arrays.asList(cosmosFullTextIndex));

        CosmosFullTextPolicy cosmosFullTextPolicy = new CosmosFullTextPolicy();
        cosmosFullTextPolicy.setPaths(new ArrayList<CosmosFullTextPath>());
        cosmosFullTextPolicy.setDefaultLanguage("en-US");

        collectionDefinition.setIndexingPolicy(indexingPolicy);
        collectionDefinition.setFullTextPolicy(cosmosFullTextPolicy);

        database.createContainer(collectionDefinition).block();
        CosmosAsyncContainer createdCollection = database.getContainer(collectionDefinition.getId());
        CosmosContainerProperties collectionProperties = createdCollection.read().block().getProperties();
        validateCollectionProperties(collectionDefinition, collectionProperties);
    }

    private void validateCollectionProperties(CosmosContainerProperties collectionDefinition, CosmosContainerProperties collectionProperties) {
       assertThat(collectionProperties.getFullTextPolicy()).isNotNull();
       assertThat(collectionProperties.getFullTextPolicy().getPaths()).isNotNull();
       assertThat(collectionProperties.getFullTextPolicy().getPaths()).hasSize(1);
       assertThat(collectionProperties.getFullTextPolicy().getDefaultLanguage()).isEqualTo(collectionDefinition.getFullTextPolicy().getDefaultLanguage());
       assertThat(collectionProperties.getFullTextPolicy().getPaths().get(0).getPath()).isEqualTo(collectionDefinition.getFullTextPolicy().getPaths().get(0).getPath());
       assertThat(collectionProperties.getFullTextPolicy().getPaths().get(0).getLanguage()).isEqualTo(collectionDefinition.getFullTextPolicy().getPaths().get(0).getLanguage());

       assertThat(collectionProperties.getIndexingPolicy().getCosmosFullTextIndexes()).isNotNull();
       assertThat(collectionProperties.getIndexingPolicy().getCosmosFullTextIndexes()).hasSize(1);
       assertThat(collectionProperties.getIndexingPolicy().getCosmosFullTextIndexes().get(0).getPath()).isEqualTo(collectionDefinition.getIndexingPolicy().getCosmosFullTextIndexes().get(0).getPath());
    }
}
