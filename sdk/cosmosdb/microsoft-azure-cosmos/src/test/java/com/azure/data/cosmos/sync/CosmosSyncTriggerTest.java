// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosTriggerProperties;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.TriggerOperation;
import com.azure.data.cosmos.TriggerType;
import com.azure.data.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosSyncTriggerTest extends TestSuiteBase {
    private CosmosSyncClient client;
    private CosmosSyncContainer container;

    @Factory(dataProvider = "clientBuilders")
    public CosmosSyncTriggerTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        assertThat(this.client).isNull();
        this.client = clientBuilder().buildSyncClient();
        CosmosContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().id()).getContainer(asyncContainer.id());
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createTrigger() throws Exception {
        CosmosTriggerProperties trigger = getCosmosTriggerProperties();

        CosmosSyncTriggerResponse triggerResponse = container.getScripts().createTrigger(trigger);
        validateResponse(trigger, triggerResponse);

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readTrigger() throws Exception {
        CosmosTriggerProperties trigger = getCosmosTriggerProperties();

        container.getScripts().createTrigger(trigger);

        CosmosSyncTriggerResponse readResponse = container.getScripts().getTrigger(trigger.id()).read();
        validateResponse(trigger, readResponse);

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void replaceTrigger() throws Exception {
        CosmosTriggerProperties trigger = getCosmosTriggerProperties();

        container.getScripts().createTrigger(trigger);

        CosmosTriggerProperties readTrigger = container.getScripts().getTrigger(trigger.id()).read().properties();
        readTrigger.body("function() {var x = 11;}");

        CosmosSyncTriggerResponse replace = container.getScripts().getTrigger(trigger.id()).replace(readTrigger);
        validateResponse(trigger, replace);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void deleteTrigger() throws Exception {
        CosmosTriggerProperties trigger = getCosmosTriggerProperties();

        container.getScripts().createTrigger(trigger);

        CosmosSyncResponse delete = container.getScripts().getTrigger(trigger.id()).delete();
    }


    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readAllTriggers() throws Exception {
        CosmosTriggerProperties trigger = getCosmosTriggerProperties();

        container.getScripts().createTrigger(trigger);

        FeedOptions feedOptions = new FeedOptions();
        feedOptions.enableCrossPartitionQuery(true);
        Iterator<FeedResponse<CosmosTriggerProperties>> feedResponseIterator3 =
                container.getScripts().readAllTriggers(feedOptions);
        assertThat(feedResponseIterator3.hasNext()).isTrue();
    }

    private CosmosTriggerProperties getCosmosTriggerProperties() {
        CosmosTriggerProperties trigger = new CosmosTriggerProperties();
        trigger.id(UUID.randomUUID().toString());
        trigger.body("function() {var x = 10;}");
        trigger.triggerOperation(TriggerOperation.CREATE);
        trigger.triggerType(TriggerType.PRE);
        return trigger;
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void queryTriggers() throws Exception {
        CosmosTriggerProperties properties = getCosmosTriggerProperties();
        container.getScripts().createTrigger(properties);
        String query = String.format("SELECT * from c where c.id = '%s'", properties.id());
        FeedOptions feedOptions = new FeedOptions().enableCrossPartitionQuery(true);

        Iterator<FeedResponse<CosmosTriggerProperties>> feedResponseIterator1 =
                container.getScripts().queryTriggers(query, feedOptions);
        assertThat(feedResponseIterator1.hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        Iterator<FeedResponse<CosmosTriggerProperties>> feedResponseIterator2 =
                container.getScripts().queryTriggers(query, feedOptions);
        assertThat(feedResponseIterator2.hasNext()).isTrue();
    }

    private void validateResponse(CosmosTriggerProperties properties,
                                  CosmosSyncTriggerResponse createResponse) {
        // Basic validation
        assertThat(createResponse.properties().id()).isNotNull();
        assertThat(createResponse.properties().id())
                .as("check Resource Id")
                .isEqualTo(properties.id());

    }

}
