/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmos;

import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.RetryOptions;
import com.microsoft.azure.cosmosdb.rx.FailureValidator;
import com.microsoft.azure.cosmosdb.rx.TestConfigurations;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentClientImpl;
import io.reactivex.subscribers.TestSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosTestSuiteBase {

    protected static final int TIMEOUT = 8000;
    protected static final int SETUP_TIMEOUT = 30000;
    protected static final int SHUTDOWN_TIMEOUT = 12000;
    protected static final int SUITE_SETUP_TIMEOUT = 120000;
    private static final Logger logger = LoggerFactory.getLogger(RxDocumentClientImpl.class);
    
    protected int subscriberValidationTimeout = TIMEOUT;

    public static String getDatabaseId(Class<?> klass) {
        return String.format("java.rx.%s", klass.getName());
    }

    public <T extends CosmosResponse> void validateSuccess(Mono<T> single,
                                                     CosmosResponseValidator<T> validator) throws InterruptedException {
        validateSuccess(single.flux(), validator, subscriberValidationTimeout);
    }

    public static <T extends CosmosResponse> void validateSuccess(Flux<T> flowable,
                                                            CosmosResponseValidator<T> validator, long timeout) throws InterruptedException {

        TestSubscriber<T> testSubscriber = new TestSubscriber<>();

        flowable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
        validator.validate( testSubscriber.values().get(0));
    }

    public <T extends Resource, U extends CosmosResponse> void validateFailure(Mono<U> mono,
                                                     FailureValidator validator) throws InterruptedException {
        validateFailure(mono.flux(), validator, subscriberValidationTimeout);
    }

    public static <T extends Resource, U extends CosmosResponse> void validateFailure(Flux<U> flowable,
                                                            FailureValidator validator, long timeout) throws InterruptedException {

        TestSubscriber<CosmosResponse> testSubscriber = new TestSubscriber<>();

        flowable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
        assertThat(testSubscriber.errors()).hasSize(1);
        validator.validate((Throwable) testSubscriber.getEvents().get(1).get(0));
    }

    @DataProvider
    public static Object[][] clientBuilders() {
        return new Object[][] { { createGatewayRxCosmosClient() } };
    }

    static protected CosmosClientBuilder createGatewayRxCosmosClient() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.Gateway);
        RetryOptions options = new RetryOptions();
        options.setMaxRetryWaitTimeInSeconds(SUITE_SETUP_TIMEOUT);
        connectionPolicy.setRetryOptions(options);

        return CosmosClient.builder().connectionPolicy(connectionPolicy)
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .consistencyLevel(ConsistencyLevel.Session);
    }

    static protected CosmosDatabase safeCreateDatabase(CosmosClient client, CosmosDatabaseSettings databaseSettings) {
        safeDeleteDatabase(client, databaseSettings.getId());
        return createDatabase(client, databaseSettings);
    }

    static protected CosmosDatabase createDatabase(CosmosClient client, String databaseId) {
        Mono<CosmosDatabaseResponse> databaseSingle = client.createDatabase(databaseId);
        return databaseSingle.block().getDatabase();
    }

    static protected CosmosContainer createContainerInDB(CosmosClient client, String containerID, String databaseId) {
        CosmosDatabase cosmosDatabaseProxy = client.getDatabase(databaseId);
        Mono<CosmosContainerResponse> containerSingle = cosmosDatabaseProxy.createContainer(containerID, "/mypk");
        return containerSingle.block().getContainer();
    }

    static private CosmosDatabase createDatabase(CosmosClient client, CosmosDatabaseSettings databaseSettings) {
        Mono<CosmosDatabaseResponse> databaseSingle = client.createDatabase(databaseSettings,
                                                                         new CosmosDatabaseRequestOptions());
        return databaseSingle.block().getDatabase();
    }

    static protected void safeDeleteDatabase(CosmosClient client, String databaseId) {
        CosmosDatabase database = client.getDatabase(databaseId);
        if (client != null) {
            try {
                database.delete().block();
            } catch (Exception e) {
            }
        }
    }

    static protected void safeDeleteContainer(CosmosClient client, String containerId, String databaseId) {
        CosmosContainer container = client.getDatabase(databaseId).getContainer(containerId);
        if (client != null) {
            try {
                container.delete().block();
            } catch (Exception e) {
            }
        }
    }

    static protected CosmosContainerSettings getContainerSettings() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerSettings settings = new CosmosContainerSettings(UUID.randomUUID().toString(), partitionKeyDef);
        return settings;
    }

    static protected void safeClose(CosmosClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                logger.error("Error: ", e);
            }
        }
    }
}
