// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosTriggerProperties;
import com.azure.cosmos.models.CosmosTriggerResponse;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.TriggerOperation;
import com.azure.cosmos.models.TriggerType;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class FabricCosmosTriggerTest extends FabricTestSuiteBase {
    private CosmosClient client;
    private CosmosContainer container;
    private static final String CONTAINER_ID = "fabric-java-test-container-" + UUID.randomUUID();

    @Factory(dataProvider = "clientBuildersWithGatewayForFabric")
    public FabricCosmosTriggerTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "fabric-test" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        CosmosDatabase database = this.client.getDatabase(DATABASE_ID);
        database.createContainerIfNotExists(CONTAINER_ID, "/mypk",
            ThroughputProperties.createManualThroughput(16000));
        this.container = database.getContainer(CONTAINER_ID);
    }

    @AfterClass(groups = { "fabric-test" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        deleteCollection(container);
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {"fabric-test"}, timeOut = TIMEOUT)
    public void createTrigger() throws Exception {
        CosmosTriggerProperties trigger = getCosmosTriggerProperties();

        CosmosTriggerResponse triggerResponse = container.getScripts().createTrigger(trigger);
        validateResponse(trigger, triggerResponse);

    }

    @Test(groups = {"fabric-test"}, timeOut = TIMEOUT)
    public void readTrigger() throws Exception {
        CosmosTriggerProperties trigger = getCosmosTriggerProperties();

        container.getScripts().createTrigger(trigger);

        CosmosTriggerResponse readResponse = container.getScripts().getTrigger(trigger.getId()).read();
        validateResponse(trigger, readResponse);

    }

    @Test(groups = {"fabric-test"}, timeOut = TIMEOUT)
    public void replaceTrigger() throws Exception {
        CosmosTriggerProperties trigger = getCosmosTriggerProperties();

        container.getScripts().createTrigger(trigger);

        CosmosTriggerProperties readTrigger = container.getScripts().getTrigger(trigger.getId()).read().getProperties();
        readTrigger.setBody("function() {var x = 11;}");

        CosmosTriggerResponse replace = container.getScripts().getTrigger(trigger.getId()).replace(readTrigger);
        validateResponse(trigger, replace);
    }

    @Test(groups = {"fabric-test"}, timeOut = TIMEOUT)
    public void deleteTrigger() throws Exception {
        CosmosTriggerProperties trigger = getCosmosTriggerProperties();

        container.getScripts().createTrigger(trigger);

        container.getScripts().getTrigger(trigger.getId()).delete();
    }


    @Test(groups = {"fabric-test"}, timeOut = TIMEOUT)
    public void readAllTriggers() throws Exception {
        CosmosTriggerProperties trigger = getCosmosTriggerProperties();

        container.getScripts().createTrigger(trigger);

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<CosmosTriggerProperties> feedResponseIterator3 =
                container.getScripts().readAllTriggers(cosmosQueryRequestOptions);
        assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();
    }

    private CosmosTriggerProperties getCosmosTriggerProperties() {
        CosmosTriggerProperties trigger = new CosmosTriggerProperties(
            UUID.randomUUID().toString(),
            "function() {var x = 10;}"
        );
        trigger.setTriggerOperation(TriggerOperation.CREATE);
        trigger.setTriggerType(TriggerType.PRE);

        return trigger;
    }

    @Test(groups = {"fabric-test"}, timeOut = TIMEOUT)
    public void queryTriggers() throws Exception {
        CosmosTriggerProperties properties = getCosmosTriggerProperties();
        container.getScripts().createTrigger(properties);
        String query = String.format("SELECT * from c where c.id = '%s'", properties.getId());
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<CosmosTriggerProperties> feedResponseIterator1 =
                container.getScripts().queryTriggers(query, cosmosQueryRequestOptions);
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedIterable<CosmosTriggerProperties> feedResponseIterator2 =
                container.getScripts().queryTriggers(query, cosmosQueryRequestOptions);
        assertThat(feedResponseIterator2.iterator().hasNext()).isTrue();
    }

    private void validateResponse(CosmosTriggerProperties properties,
                                  CosmosTriggerResponse createResponse) {
        // Basic validation
        assertThat(createResponse.getProperties().getId()).isNotNull();
        assertThat(createResponse.getProperties().getId())
                .as("check Resource Id")
                .isEqualTo(properties.getId());

    }

}
