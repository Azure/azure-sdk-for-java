package com.azure.cosmos.encryption;

import com.azure.core.cryptography.KeyEncryptionKeyResolver;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.PartitionKind;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptionAsyncApiHierarchicalPkTest extends TestSuiteBase {
    private CosmosAsyncClient client;
    private CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;

    CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer;
    CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase;

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public EncryptionAsyncApiHierarchicalPkTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"encryption"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildAsyncClient();
        KeyEncryptionKeyResolver keyEncryptionKeyResolver = new TestKeyEncryptionKeyResolver();
        cosmosEncryptionAsyncClient = new CosmosEncryptionClientBuilder().cosmosAsyncClient(this.client).keyEncryptionKeyResolver(
            keyEncryptionKeyResolver).keyEncryptionKeyResolverName("TEST_KEY_RESOLVER").buildAsyncClient();
        cosmosEncryptionAsyncDatabase = getSharedEncryptionDatabase(cosmosEncryptionAsyncClient);
        cosmosEncryptionAsyncContainer = getSharedEncryptionContainer(cosmosEncryptionAsyncClient);

        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.setKind(PartitionKind.MULTI_HASH);
        partitionKeyDefinition.setVersion(PartitionKeyDefinitionVersion.V2);
        List<String> subParitionKeyPaths = new ArrayList<>();
        subParitionKeyPaths.add("/sensitiveNestedPojo");
        subParitionKeyPaths.add("/mypk");
        partitionKeyDefinition.setPaths(subParitionKeyPaths);

        ClientEncryptionPolicy clientEncryptionWithPolicyFormatVersion2 = new ClientEncryptionPolicy(getPaths(2), 2);
        String containerId = UUID.randomUUID().toString();
        CosmosContainerProperties properties = getCollectionDefinition(containerId, partitionKeyDefinition);
        properties.setClientEncryptionPolicy(clientEncryptionWithPolicyFormatVersion2);
        cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().createContainer(properties).block();

    }

    @AfterClass(groups = {"encryption"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void crudOnHierarchicalPk() {
        EncryptionPojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = cosmosEncryptionAsyncContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions()).block();
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        EncryptionPojo readItem = cosmosEncryptionAsyncContainer.readItem(properties.getId(), new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions(), EncryptionPojo.class).block().getItem();
        validateResponse(properties, readItem);

        // Deleting this item so query max of string in the query_aggregate test passes
        cosmosEncryptionAsyncContainer.deleteItem(properties.getId(), new PartitionKey(properties.getMypk())).block();
    }


}
