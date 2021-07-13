// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.spark;

import com.azure.analytics.synapse.spark.models.SparkSession;
import com.azure.analytics.synapse.spark.models.SparkSessionCollection;
import com.azure.analytics.synapse.spark.models.SparkSessionOptions;
import com.azure.analytics.synapse.spark.models.SparkStatement;
import com.azure.analytics.synapse.spark.models.SparkStatementCollection;
import com.azure.analytics.synapse.spark.models.SparkStatementLanguageType;
import com.azure.analytics.synapse.spark.models.SparkStatementOptions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SparkSessionAsyncClientTest extends SparkClientTestBase {

    private SparkSessionAsyncClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new SparkClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .sparkPoolName(getSparkPoolName())
            .buildSparkSessionAsyncClient());
    }

    @Test
    public void getSparkSession() {
        client.getSparkSessions()
            .map(SparkSessionCollection::getSessions)
            .flatMapMany(Flux::fromIterable)
            .doOnNext(expected -> StepVerifier.create(client.getSparkSession(expected.getId()))
                .assertNext(actual -> assertSparkSessionEquals(expected, actual))
                .verifyComplete());
    }

    @Test
    public void crudSparkSession() {
        // arrange
        String sessionName = testResourceNamer.randomName("spark-session-", 20);
        SparkSessionOptions options = new SparkSessionOptions()
            .setName(sessionName)
            .setDriverMemory("28g")
            .setDriverCores(4)
            .setExecutorMemory("28g")
            .setExecutorCores(4)
            .setExecutorCount(2);

        AtomicReference<SparkSession> testSession = new AtomicReference<>();

        try {
            // act
            StepVerifier.create(client.createSparkSession(options, true))
                .consumeNextWith(expected -> {
                    testSession.set(expected);

                    assertEquals(sessionName, expected.getName());
                    assertEquals(getSparkPoolName(), expected.getSparkPoolName());
                })
                .verifyComplete();

            // act
            StepVerifier.create(client.getSparkSession(testSession.get().getId(), true))
                .assertNext(actual -> assertSparkSessionEquals(testSession.get(), actual))
                .verifyComplete();
        } finally {
            // clean up
            if (testSession.get() != null) {
                client.cancelSparkSession(testSession.get().getId()).block();
            }
        }
    }

    @Test
    public void crudSparkStatement() throws Exception {
        // arrange
        String sessionName = testResourceNamer.randomName("spark-session-", 20);
        SparkSessionOptions sessionOptions = new SparkSessionOptions()
            .setName(sessionName)
            .setDriverMemory("28g")
            .setDriverCores(4)
            .setExecutorMemory("28g")
            .setExecutorCores(4)
            .setExecutorCount(2);

        AtomicReference<SparkSession> testSession = new AtomicReference<>();
        AtomicReference<SparkStatement> testStatement = new AtomicReference<>();

        try {
            // act
            StepVerifier.create(client.createSparkSession(sessionOptions, true))
                .consumeNextWith(session -> {
                    testSession.set(session);

                    // assert
                    assertEquals(sessionName, session.getName());
                    assertEquals(getSparkPoolName(), session.getSparkPoolName());
                })
                .verifyComplete();

            // arrange
            String code = "print('hello, Azure CLI')";

            SparkStatementOptions options = new SparkStatementOptions()
                .setKind(SparkStatementLanguageType.PYSPARK)
                .setCode(code);

            Mono<SparkStatement> createSparkStatement = Mono.defer(() -> {
                if (interceptorManager.isPlaybackMode()) {
                    return Mono.empty();
                } else {
                    return Mono.delay(Duration.ofSeconds(360));
                }
            })
            .then(Mono.defer(() -> client.resetSparkSessionTimeout(testSession.get().getId())))
            .then(Mono.defer(() -> client.createSparkStatement(testSession.get().getId(), options)));

            // act
            StepVerifier.create(createSparkStatement)
                .consumeNextWith(created -> {
                    testStatement.set(created);
                    // assert
                    assertEquals(code, created.getCode());
                })
                .verifyComplete();

            // arrange
            Mono<SparkStatement> getSparkStatement = client.getSparkStatement(testSession.get().getId(), testStatement.get().getId());

            // act
            StepVerifier.create(getSparkStatement)
                .assertNext(actual -> assertSparkStatementEquals(testStatement.get(), actual))
                .verifyComplete();

            // arrange
            Mono<SparkStatementCollection> getSparkStatements = client.getSparkStatements(testSession.get().getId());

            // act
            StepVerifier.create(getSparkStatements)
                .assertNext(collection -> {
                    assertEquals(1, collection.getTotal());
                    assertSparkStatementEquals(testStatement.get(), collection.getStatements().get(0));
                })
                .verifyComplete();
        } finally {
            // clean up
            if (testStatement.get() != null) {
                client.cancelSparkStatement(testStatement.get().getId(), testStatement.get().getId());
            }
            if (testSession.get() != null) {
                client.cancelSparkSession(testSession.get().getId());
            }
        }
    }
}
