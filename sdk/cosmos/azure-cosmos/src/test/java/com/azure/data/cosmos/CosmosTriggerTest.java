// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosTriggerTest extends TestSuiteBase {
    private CosmosClient client;
    private CosmosContainer container;

    @Factory(dataProvider = "clientBuilders")
    public CosmosTriggerTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        assertThat(this.client).isNull();
        this.client = clientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createTrigger() throws Exception {
        CosmosTriggerProperties trigger = getCosmosTriggerProperties();

        CosmosTriggerResponse triggerResponse = container.getScripts().createTrigger(trigger);
        validateResponse(trigger, triggerResponse);

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readTrigger() throws Exception {
        CosmosTriggerProperties trigger = getCosmosTriggerProperties();

        container.getScripts().createTrigger(trigger);

        CosmosTriggerResponse readResponse = container.getScripts().getTrigger(trigger.getId()).read();
        validateResponse(trigger, readResponse);

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void replaceTrigger() throws Exception {
        CosmosTriggerProperties trigger = getCosmosTriggerProperties();

        container.getScripts().createTrigger(trigger);

        CosmosTriggerProperties readTrigger = container.getScripts().getTrigger(trigger.getId()).read().getProperties();
        readTrigger.setBody("function() {var x = 11;}");

        CosmosTriggerResponse replace = container.getScripts().getTrigger(trigger.getId()).replace(readTrigger);
        validateResponse(trigger, replace);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void deleteTrigger() throws Exception {
        CosmosTriggerProperties trigger = getCosmosTriggerProperties();

        container.getScripts().createTrigger(trigger);

        container.getScripts().getTrigger(trigger.getId()).delete();
    }


    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readAllTriggers() throws Exception {
        CosmosTriggerProperties trigger = getCosmosTriggerProperties();

        container.getScripts().createTrigger(trigger);

        FeedOptions feedOptions = new FeedOptions();
        feedOptions.setEnableCrossPartitionQuery(true);
        Iterator<FeedResponse<CosmosTriggerProperties>> feedResponseIterator3 =
                container.getScripts().readAllTriggers(feedOptions);
        assertThat(feedResponseIterator3.hasNext()).isTrue();
    }

    private CosmosTriggerProperties getCosmosTriggerProperties() {
        CosmosTriggerProperties trigger = new CosmosTriggerProperties();
        trigger.setId(UUID.randomUUID().toString());
        trigger.setBody("function() {var x = 10;}");
        trigger.setTriggerOperation(TriggerOperation.CREATE);
        trigger.setTriggerType(TriggerType.PRE);
        return trigger;
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void queryTriggers() throws Exception {
        CosmosTriggerProperties properties = getCosmosTriggerProperties();
        container.getScripts().createTrigger(properties);
        String query = String.format("SELECT * from c where c.id = '%s'", properties.getId());
        FeedOptions feedOptions = new FeedOptions().setEnableCrossPartitionQuery(true);

        Iterator<FeedResponse<CosmosTriggerProperties>> feedResponseIterator1 =
                container.getScripts().queryTriggers(query, feedOptions);
        assertThat(feedResponseIterator1.hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        Iterator<FeedResponse<CosmosTriggerProperties>> feedResponseIterator2 =
                container.getScripts().queryTriggers(query, feedOptions);
        assertThat(feedResponseIterator2.hasNext()).isTrue();
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
