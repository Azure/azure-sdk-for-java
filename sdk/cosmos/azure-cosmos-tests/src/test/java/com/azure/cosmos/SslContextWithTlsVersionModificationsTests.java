package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.UUID;

public class SslContextWithTlsVersionModificationsTests {

    private static final Logger logger = LoggerFactory.getLogger(SslContextWithTlsVersionModificationsTests.class);

    @DataProvider
    private Object[] isTls_v_1_3_enabledConfigs() {
        return new Object[] { false, true };
    }

    @Test(groups = { "simple" }, dataProvider = "isTls_v_1_3_enabledConfigs")
    public void readItem(boolean isTlsV_1_3_enabled) {

        if (isTlsV_1_3_enabled) {
            System.setProperty("COSMOS.IS_TLSv1_3_ENABLED", String.valueOf(isTlsV_1_3_enabled));
        }

        CosmosClient client = null;
        CosmosDatabase database = null;
        CosmosContainer container = null;

        try {

            client = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .endpointDiscoveryEnabled(true)
                .directMode()
                .buildClient();

            String databaseId = UUID.randomUUID().toString();
            String containerId = UUID.randomUUID().toString();

            client.createDatabase(databaseId);
            database = client.getDatabase(databaseId);
            database.createContainerIfNotExists(containerId,
                "/mypk", 500);

            container = database.getContainer(containerId);

            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);

            CosmosItemResponse<InternalObjectNode> readResponse1 = container.readItem(properties.getId(),
                new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                new CosmosItemRequestOptions(),
                InternalObjectNode.class);
        } catch (Exception ex) {
            logger.error("Exception thrown : ", ex);
        } finally {

            if (container != null) {
                container.delete();
            }

            if (database != null) {
                database.delete();
            }

            if (client != null) {
                client.close();
            }

            System.clearProperty("COSMOS.IS_TLSv1_3_ENABLED");
        }
    }

    private InternalObjectNode getDocumentDefinition(String documentId) {
        final String uuid = UUID.randomUUID().toString();
        final InternalObjectNode properties =
            new InternalObjectNode(String.format("{ "
                    + "\"id\": \"%s\", "
                    + "\"mypk\": \"%s\", "
                    + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                    + "}"
                , documentId, uuid));
        return properties;
    }

}
