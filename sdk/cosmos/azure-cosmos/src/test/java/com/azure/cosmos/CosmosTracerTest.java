// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;
import com.azure.cosmos.implementation.CosmosItemProperties;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.TracerProvider;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosTriggerProperties;
import com.azure.cosmos.models.CosmosUserDefinedFunctionProperties;
import com.azure.cosmos.models.FeedOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.TriggerOperation;
import com.azure.cosmos.models.TriggerType;
import com.azure.cosmos.rx.TestSuiteBase;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ServiceLoader;
import java.util.UUID;

public class CosmosTracerTest extends TestSuiteBase {
    private static final String ITEM_ID = "tracerDoc";
    CosmosAsyncClient client;
    CosmosAsyncDatabase cosmosAsyncDatabase;
    CosmosAsyncContainer cosmosAsyncContainer;

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode(DirectConnectionConfig.getDefaultConfig())
            .buildAsyncClient();
        cosmosAsyncDatabase = getSharedCosmosDatabase(client);
        cosmosAsyncContainer = getSharedMultiPartitionCosmosContainer(client);

    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void cosmosAsyncClient() {
        TracerProvider tracer = Mockito.spy(new TracerProvider(ServiceLoader.load(Tracer.class)));
        ReflectionUtils.setTracerProvider(client, tracer);

        client.createDatabaseIfNotExists(cosmosAsyncDatabase.getId()).block();
        Mockito.verify(tracer, Mockito.times(1)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        client.readAllDatabases(new FeedOptions()).byPage().single().block();
        Mockito.verify(tracer, Mockito.times(2)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        String query = "select * from c where c.id = '" + cosmosAsyncDatabase.getId() + "'";
        client.queryDatabases(query, new FeedOptions()).byPage().single().block();
        Mockito.verify(tracer, Mockito.times(3)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void cosmosAsyncDatabase() {
        TracerProvider tracer = Mockito.spy(new TracerProvider(ServiceLoader.load(Tracer.class)));
        ReflectionUtils.setTracerProvider(client, tracer);

        cosmosAsyncDatabase.createContainerIfNotExists(cosmosAsyncContainer.getId(),
                "/pk", 5000).block();
        Mockito.verify(tracer, Mockito.times(1)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        try {
            cosmosAsyncDatabase.readProvisionedThroughput().block();
        } catch (CosmosException ex) {
            //do nothing
        }

        Mockito.verify(tracer, Mockito.times(2)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        cosmosAsyncDatabase.readAllUsers().byPage().single().block();
        Mockito.verify(tracer, Mockito.times(3)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        cosmosAsyncDatabase.readAllContainers().byPage().single().block();
        Mockito.verify(tracer, Mockito.times(4)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void cosmosAsyncContainer() {
        TracerProvider tracer = Mockito.spy(new TracerProvider(ServiceLoader.load(Tracer.class)));
        ReflectionUtils.setTracerProvider(client, tracer);

        cosmosAsyncContainer.read().block();
        Mockito.verify(tracer, Mockito.times(1)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        try {
            cosmosAsyncContainer.readProvisionedThroughput().block();
        } catch (CosmosException ex) {
            //do nothing
        }
        Mockito.verify(tracer, Mockito.times(2)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        CosmosItemProperties properties = new CosmosItemProperties();
        properties.setId(ITEM_ID);
        cosmosAsyncContainer.createItem(properties).block();
        Mockito.verify(tracer, Mockito.times(3)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        cosmosAsyncContainer.upsertItem(properties,
            new CosmosItemRequestOptions()).block();
        Mockito.verify(tracer, Mockito.times(4)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        cosmosAsyncContainer.readItem(ITEM_ID, PartitionKey.NONE,
            CosmosItemProperties.class).block();
        Mockito.verify(tracer, Mockito.times(5)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        cosmosAsyncContainer.deleteItem(ITEM_ID, PartitionKey.NONE).block();
        Mockito.verify(tracer, Mockito.times(6)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));
        cosmosAsyncContainer.readAllItems(new FeedOptions(), CosmosItemRequestOptions.class).byPage().single().block();
        Mockito.verify(tracer, Mockito.times(7)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        String query = "select * from c where c.id = '" + ITEM_ID + "'";
        cosmosAsyncContainer.queryItems(query, new FeedOptions(), CosmosItemRequestOptions.class).byPage().single().block();
        Mockito.verify(tracer, Mockito.times(8)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void cosmosAsyncScripts() {
        TracerProvider tracer = Mockito.spy(new TracerProvider(ServiceLoader.load(Tracer.class)));
        ReflectionUtils.setTracerProvider(client, tracer);

        cosmosAsyncContainer.getScripts().readAllStoredProcedures(new FeedOptions()).byPage().single().block();
        Mockito.verify(tracer, Mockito.times(1)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        cosmosAsyncContainer.getScripts().readAllTriggers(new FeedOptions()).byPage().single().block();
        Mockito.verify(tracer, Mockito.times(2)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        cosmosAsyncContainer.getScripts().readAllUserDefinedFunctions(new FeedOptions()).byPage().single().block();
        Mockito.verify(tracer, Mockito.times(3)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        CosmosUserDefinedFunctionProperties cosmosUserDefinedFunctionProperties =
            getCosmosUserDefinedFunctionProperties();
        CosmosUserDefinedFunctionProperties resultUdf =
            cosmosAsyncContainer.getScripts().createUserDefinedFunction(cosmosUserDefinedFunctionProperties).block().getProperties();
        Mockito.verify(tracer, Mockito.times(4)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        cosmosAsyncContainer.getScripts().getUserDefinedFunction(cosmosUserDefinedFunctionProperties.getId()).read().block();
        Mockito.verify(tracer, Mockito.times(5)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        cosmosUserDefinedFunctionProperties.setBody("function() {var x = 15;}");
        cosmosAsyncContainer.getScripts().getUserDefinedFunction(resultUdf.getId()).replace(resultUdf).block();
        Mockito.verify(tracer, Mockito.times(6)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        cosmosAsyncContainer.getScripts().readAllUserDefinedFunctions(new FeedOptions()).byPage().single().block();
        Mockito.verify(tracer, Mockito.times(7)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        cosmosAsyncContainer.getScripts().getUserDefinedFunction(cosmosUserDefinedFunctionProperties.getId()).delete().block();
        Mockito.verify(tracer, Mockito.times(8)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        CosmosTriggerProperties cosmosTriggerProperties = getCosmosTriggerProperties();
        CosmosTriggerProperties resultTrigger =
            cosmosAsyncContainer.getScripts().createTrigger(cosmosTriggerProperties).block().getProperties();
        Mockito.verify(tracer, Mockito.times(9)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        cosmosAsyncContainer.getScripts().getTrigger(cosmosTriggerProperties.getId()).read().block();
        Mockito.verify(tracer, Mockito.times(10)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        cosmosAsyncContainer.getScripts().getTrigger(cosmosTriggerProperties.getId()).replace(resultTrigger).block();
        Mockito.verify(tracer, Mockito.times(11)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));
        cosmosAsyncContainer.getScripts().readAllTriggers(new FeedOptions()).byPage().single().block();
        Mockito.verify(tracer, Mockito.times(12)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        cosmosAsyncContainer.getScripts().getTrigger(cosmosTriggerProperties.getId()).delete().block();
        Mockito.verify(tracer, Mockito.times(13)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        CosmosStoredProcedureProperties procedureProperties = getCosmosStoredProcedureProperties();
        CosmosStoredProcedureProperties resultSproc =
            cosmosAsyncContainer.getScripts().createStoredProcedure(procedureProperties).block().getProperties();
        Mockito.verify(tracer, Mockito.times(14)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        cosmosAsyncContainer.getScripts().getStoredProcedure(procedureProperties.getId()).read().block();
        Mockito.verify(tracer, Mockito.times(15)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        cosmosAsyncContainer.getScripts().getStoredProcedure(procedureProperties.getId()).replace(resultSproc).block();
        Mockito.verify(tracer, Mockito.times(16)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));

        cosmosAsyncContainer.getScripts().readAllStoredProcedures(new FeedOptions()).byPage().single().block();

        cosmosAsyncContainer.getScripts().getStoredProcedure(procedureProperties.getId()).delete().block();
        Mockito.verify(tracer, Mockito.times(18)).startSpan(Matchers.anyString(), Matchers.anyString(),
            Matchers.anyString(), Matchers.any(Context.class));
    }

    @AfterClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void afterClass() {
        LifeCycleUtils.closeQuietly(client);
    }

    private static CosmosUserDefinedFunctionProperties getCosmosUserDefinedFunctionProperties() {
        CosmosUserDefinedFunctionProperties udf = new CosmosUserDefinedFunctionProperties();
        udf.setId(UUID.randomUUID().toString());
        udf.setBody("function() {var x = 10;}");
        return udf;
    }

    private static CosmosTriggerProperties getCosmosTriggerProperties() {
        CosmosTriggerProperties trigger = new CosmosTriggerProperties();
        trigger.setId(UUID.randomUUID().toString());
        trigger.setBody("function() {var x = 10;}");
        trigger.setTriggerOperation(TriggerOperation.CREATE);
        trigger.setTriggerType(TriggerType.PRE);
        return trigger;
    }

    private static CosmosStoredProcedureProperties getCosmosStoredProcedureProperties() {
        CosmosStoredProcedureProperties storedProcedureDef = new CosmosStoredProcedureProperties();
        storedProcedureDef.setId(UUID.randomUUID().toString());
        storedProcedureDef.setBody("function() {var x = 10;}");
        return storedProcedureDef;
    }
}
