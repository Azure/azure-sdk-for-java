package com.azure.cosmos.cris.querystuckrepro;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ReproTest extends TestSuiteBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private CosmosAsyncClient client;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "simpleGatewayClient")
    public ReproTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"fast"}, timeOut = SETUP_TIMEOUT)
    public void before_ReproTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildAsyncClient();
        this.container = getSharedSinglePartitionCosmosContainer(this.client);
        logger.info("Using Container - {}:{} ({})",
            this.container.getDatabase().getId(),
            this.container.getId(),
            this.container.read().block().getProperties().getResourceId());
    }

    @AfterClass(groups = {"fast"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT * 1_000_000)
    public void runICM497415681OriginalReproTest() throws Exception {
        logger.info("Creating test docs");
        for (int i = 0; i < 1000; i++) {
            String id = UUID.randomUUID().toString();
            ObjectNode newDoc = getDocumentDefinition(id, id);
            this.container.createItem(newDoc, new PartitionKey(id), new CosmosItemRequestOptions()).block();

            if ((i % 100) == 0) {
                logger.info("So far ingested {} documents.", i);
            }
        }

        logger.info("Finished Creating test docs");

        PartReadAttris partReadAttris = new PartReadAttris(1);
        CosmosDBSqlApiReaderDTO dto = new CosmosDBSqlApiReaderDTO(
            partReadAttris,
            this.client,
            this.container,
            "SELECT * FROM c",
            "/mypk"
        );

        DataSession session = new DataSession();
        CosmosDBSqlApiRowByRowReader reader = new CosmosDBSqlApiRowByRowReader(dto);
        while (true) {
            ReadAttributes readerAttributes = new ReadAttributes( 1);
            if (reader.read(session, readerAttributes) == EReturnStatus.NO_MORE_DATA) {
                break;
            }
        };

        assertThat(session.getNumberOfRecordsRetrievedFromDatabase()).isEqualTo(2000);
        assertThat(session.getNumberOfPagesRetrievedFromDatabase()).isEqualTo(2000);
    }

    private ObjectNode getDocumentDefinition(String documentId, String pkId) throws JsonProcessingException {

        String json = String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
            , documentId, pkId);
        return
            OBJECT_MAPPER.readValue(json, ObjectNode.class);
    }
}
