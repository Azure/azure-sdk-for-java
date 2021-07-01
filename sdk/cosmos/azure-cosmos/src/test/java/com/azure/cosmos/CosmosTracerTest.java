// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.TracerProvider;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosTriggerProperties;
import com.azure.cosmos.models.CosmosUserDefinedFunctionProperties;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.TriggerOperation;
import com.azure.cosmos.models.TriggerType;
import com.azure.cosmos.rx.TestSuiteBase;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

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
        Tracer mockTracer = getMockTracer();
        TracerProvider tracerProvider = Mockito.spy(new TracerProvider(mockTracer));
        ReflectionUtils.setTracerProvider(client, tracerProvider);
        int traceApiCounter = 1;

        TracerProviderCapture tracerProviderCapture = new TracerProviderCapture();
        Mockito.doAnswer(tracerProviderCapture).when(tracerProvider).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any());

        client.createDatabaseIfNotExists(cosmosAsyncDatabase.getId(), ThroughputProperties.createManualThroughput(5000)).block();
        Context context = tracerProviderCapture.getResult();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "createDatabaseIfNotExists." + cosmosAsyncDatabase.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        client.readAllDatabases(new CosmosQueryRequestOptions()).byPage().single().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "readAllDatabases", context, null, traceApiCounter, null);
        traceApiCounter++;


        String query = "select * from c where c.id = '" + cosmosAsyncDatabase.getId() + "'";
        client.queryDatabases(query, new CosmosQueryRequestOptions()).byPage().single().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "queryDatabases", context, null, traceApiCounter, null);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void cosmosAsyncDatabase() {
        Tracer mockTracer = getMockTracer();
        TracerProvider tracerProvider = Mockito.spy(new TracerProvider(mockTracer));
        ReflectionUtils.setTracerProvider(client, tracerProvider);
        TracerProviderCapture tracerProviderCapture = new TracerProviderCapture();
        Mockito.doAnswer(tracerProviderCapture).when(tracerProvider).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        int traceApiCounter = 1;

        cosmosAsyncDatabase.createContainerIfNotExists(cosmosAsyncContainer.getId(),
            "/pk", 5000).block();
        Context context = tracerProviderCapture.getResult();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "createContainerIfNotExists." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        cosmosAsyncDatabase.readAllUsers().byPage().single().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "readAllUsers." + cosmosAsyncDatabase.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        cosmosAsyncDatabase.readAllContainers().byPage().single().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "readAllContainers." + cosmosAsyncDatabase.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        String errorType = null;
        try {
            cosmosAsyncDatabase.readThroughput().block();
        } catch (CosmosException ex) {
            errorType = ex.getClass().getName();
        }

        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "readThroughput." + cosmosAsyncDatabase.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, errorType);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void cosmosAsyncContainer() {
        Tracer mockTracer = getMockTracer();
        TracerProvider tracerProvider = Mockito.spy(new TracerProvider(mockTracer));
        ReflectionUtils.setTracerProvider(client, tracerProvider);
        TracerProviderCapture tracerProviderCapture = new TracerProviderCapture();
        Mockito.doAnswer(tracerProviderCapture).when(tracerProvider).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        int traceApiCounter = 1;

        cosmosAsyncContainer.read().block();
        Context context = tracerProviderCapture.getResult();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "readContainer." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        try {
            cosmosAsyncContainer.readThroughput().block();
        } catch (CosmosException ex) {
            //do nothing
        }
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "readThroughput." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        InternalObjectNode item = new InternalObjectNode();
        item.setId(ITEM_ID);
        cosmosAsyncContainer.createItem(item).block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "createItem." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        cosmosAsyncContainer.upsertItem(item,
            new CosmosItemRequestOptions()).block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "upsertItem." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        InternalObjectNode node = cosmosAsyncContainer.readItem(ITEM_ID, PartitionKey.NONE,
            InternalObjectNode.class).block().getItem();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "readItem." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        cosmosAsyncContainer.deleteItem(ITEM_ID, PartitionKey.NONE).block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "deleteItem." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        cosmosAsyncContainer.readAllItems(new CosmosQueryRequestOptions(), CosmosItemRequestOptions.class).byPage().single().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "readAllItems." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        String query = "select * from c where c.id = '" + ITEM_ID + "'";
        cosmosAsyncContainer.queryItems(query, new CosmosQueryRequestOptions(), CosmosItemRequestOptions.class).byPage().single().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "queryItems." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void cosmosAsyncScripts() {
        Tracer mockTracer = getMockTracer();
        TracerProvider tracerProvider = Mockito.spy(new TracerProvider(mockTracer));
        ReflectionUtils.setTracerProvider(client, tracerProvider);
        TracerProviderCapture tracerProviderCapture = new TracerProviderCapture();
        Mockito.doAnswer(tracerProviderCapture).when(tracerProvider).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        int traceApiCounter = 1;

        cosmosAsyncContainer.getScripts().readAllStoredProcedures(new CosmosQueryRequestOptions()).byPage().single().block();
        Context context = tracerProviderCapture.getResult();

        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "readAllStoredProcedures." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        cosmosAsyncContainer.getScripts().readAllTriggers(new CosmosQueryRequestOptions()).byPage().single().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "readAllTriggers." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        cosmosAsyncContainer.getScripts().readAllUserDefinedFunctions(new CosmosQueryRequestOptions()).byPage().single().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "readAllUserDefinedFunctions." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        CosmosUserDefinedFunctionProperties cosmosUserDefinedFunctionProperties =
            getCosmosUserDefinedFunctionProperties();
        CosmosUserDefinedFunctionProperties resultUdf =
            cosmosAsyncContainer.getScripts().createUserDefinedFunction(cosmosUserDefinedFunctionProperties).block().getProperties();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "createUserDefinedFunction." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        cosmosAsyncContainer.getScripts().getUserDefinedFunction(cosmosUserDefinedFunctionProperties.getId()).read().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "readUserDefinedFunction." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        cosmosUserDefinedFunctionProperties.setBody("function() {var x = 15;}");
        cosmosAsyncContainer.getScripts().getUserDefinedFunction(resultUdf.getId()).replace(resultUdf).block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "replaceUserDefinedFunction." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        cosmosAsyncContainer.getScripts().readAllUserDefinedFunctions(new CosmosQueryRequestOptions()).byPage().single().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        traceApiCounter++;

        cosmosAsyncContainer.getScripts().getUserDefinedFunction(cosmosUserDefinedFunctionProperties.getId()).delete().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "deleteUserDefinedFunction." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        CosmosTriggerProperties cosmosTriggerProperties = getCosmosTriggerProperties();
        CosmosTriggerProperties resultTrigger =
            cosmosAsyncContainer.getScripts().createTrigger(cosmosTriggerProperties).block().getProperties();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "createTrigger." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        cosmosAsyncContainer.getScripts().getTrigger(cosmosTriggerProperties.getId()).read().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "readTrigger." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        cosmosAsyncContainer.getScripts().getTrigger(cosmosTriggerProperties.getId()).replace(resultTrigger).block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "replaceTrigger." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        cosmosAsyncContainer.getScripts().readAllTriggers(new CosmosQueryRequestOptions()).byPage().single().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        traceApiCounter++;

        cosmosAsyncContainer.getScripts().getTrigger(cosmosTriggerProperties.getId()).delete().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "deleteTrigger." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        CosmosStoredProcedureProperties procedureProperties = getCosmosStoredProcedureProperties();
        CosmosStoredProcedureProperties resultSproc =
            cosmosAsyncContainer.getScripts().createStoredProcedure(procedureProperties).block().getProperties();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "createStoredProcedure." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        cosmosAsyncContainer.getScripts().getStoredProcedure(procedureProperties.getId()).read().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "readStoredProcedure." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        cosmosAsyncContainer.getScripts().getStoredProcedure(procedureProperties.getId()).replace(resultSproc).block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "replaceStoredProcedure." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        cosmosAsyncContainer.getScripts().readAllStoredProcedures(new CosmosQueryRequestOptions()).byPage().single().block();

        cosmosAsyncContainer.getScripts().getStoredProcedure(procedureProperties.getId()).delete().block();
        traceApiCounter++;
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "deleteStoredProcedure." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void tracerExceptionSpan() {
        Tracer mockTracer = getMockTracer();
        TracerProvider tracerProvider = Mockito.spy(new TracerProvider(mockTracer));
        ReflectionUtils.setTracerProvider(client, tracerProvider);
        int traceApiCounter = 1;

        TracerProviderCapture tracerProviderCapture = new TracerProviderCapture();
        Mockito.doAnswer(tracerProviderCapture).when(tracerProvider).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));

        InternalObjectNode item = new InternalObjectNode();
        item.setId("testDoc");
        cosmosAsyncContainer.createItem(item).block();
        Context context = tracerProviderCapture.getResult();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "createItem." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null);
        traceApiCounter++;

        String errorType = null;
        try {
            PartitionKey partitionKey = new PartitionKey("wrongPk");
            cosmosAsyncContainer.readItem("testDoc", partitionKey, null, InternalObjectNode.class).block();
            fail("readItem should fail due to wrong pk");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            errorType = ex.getClass().getName();
        }
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(mockTracer, "readItem." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter
            , errorType);
    }

    @AfterClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void afterClass() {
        LifeCycleUtils.closeQuietly(client);
    }

    private static CosmosUserDefinedFunctionProperties getCosmosUserDefinedFunctionProperties() {
        CosmosUserDefinedFunctionProperties udf =
            new CosmosUserDefinedFunctionProperties(UUID.randomUUID().toString(), "function() {var x = 10;}");
        return udf;
    }

    private static CosmosTriggerProperties getCosmosTriggerProperties() {
        CosmosTriggerProperties trigger = new CosmosTriggerProperties(UUID.randomUUID().toString(), "function() {var " +
            "x = 10;}");
        trigger.setTriggerOperation(TriggerOperation.CREATE);
        trigger.setTriggerType(TriggerType.PRE);
        return trigger;
    }

    private static CosmosStoredProcedureProperties getCosmosStoredProcedureProperties() {
        CosmosStoredProcedureProperties storedProcedureDef =
            new CosmosStoredProcedureProperties(UUID.randomUUID().toString(), "function() {var x = 10;}");
        return storedProcedureDef;
    }

    private Tracer getMockTracer() {
        Tracer mockTracer = Mockito.mock(Tracer.class);
        Mockito.when(mockTracer.start(ArgumentMatchers.any(), ArgumentMatchers.any(Context.class))).thenReturn(Context.NONE);
        return mockTracer;
    }

    private void verifyTracerAttributes(Tracer mockTracer, String methodName, Context context, String databaseName,
                                        int numberOfTimesCalledWithinTest, String errorType) {
        if (databaseName != null) {
            Mockito.verify(mockTracer, Mockito.times(numberOfTimesCalledWithinTest)).setAttribute(TracerProvider.DB_INSTANCE,
                databaseName, context);
        }
        Mockito.verify(mockTracer, Mockito.times(numberOfTimesCalledWithinTest)).setAttribute(TracerProvider.DB_TYPE,
            TracerProvider.DB_TYPE_VALUE, context);
        Mockito.verify(mockTracer, Mockito.times(numberOfTimesCalledWithinTest)).setAttribute(TracerProvider.DB_URL,
            TestConfigurations.HOST,
            context);
        Mockito.verify(mockTracer, Mockito.times(1)).setAttribute(TracerProvider.DB_STATEMENT, methodName, context);
        if (errorType == null) {
            Mockito.verify(mockTracer, Mockito.times(0)).setAttribute(Mockito.eq(TracerProvider.ERROR_MSG)
                , ArgumentMatchers.any(), Mockito.eq(context));
            Mockito.verify(mockTracer, Mockito.times(0)).setAttribute(Mockito.eq(TracerProvider.ERROR_TYPE)
                , ArgumentMatchers.any(), Mockito.eq(context));
        } else {
            Mockito.verify(mockTracer, Mockito.times(1)).setAttribute(Mockito.eq(TracerProvider.ERROR_TYPE)
                , Mockito.eq(errorType), Mockito.eq(context));
            Mockito.verify(mockTracer, Mockito.times(1)).setAttribute(Mockito.eq(TracerProvider.ERROR_MSG)
                , ArgumentMatchers.any(), Mockito.eq(context));
        }
    }

    private class TracerProviderCapture implements Answer<Context> {
        private Context result = null;

        public Context getResult() {
            return result;
        }

        @Override
        public Context answer(InvocationOnMock invocationOnMock) throws Throwable {
            result = (Context) invocationOnMock.callRealMethod();
            return result;
        }
    }
}
