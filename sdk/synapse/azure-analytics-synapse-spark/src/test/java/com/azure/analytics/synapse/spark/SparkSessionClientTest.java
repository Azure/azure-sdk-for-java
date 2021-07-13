// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.spark;

import com.azure.analytics.synapse.spark.models.SparkSession;
import com.azure.analytics.synapse.spark.models.SparkSessionOptions;
import com.azure.analytics.synapse.spark.models.SparkStatement;
import com.azure.analytics.synapse.spark.models.SparkStatementCollection;
import com.azure.analytics.synapse.spark.models.SparkStatementLanguageType;
import com.azure.analytics.synapse.spark.models.SparkStatementOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SparkSessionClientTest extends SparkClientTestBase {

    private SparkSessionClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new SparkClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .sparkPoolName(getSparkPoolName())
            .buildSparkSessionClient());
    }

    @Test
    public void getSparkSession() {
        for (SparkSession expectedSparkSession : client.getSparkSessions().getSessions()) {
            // act
            SparkSession actualSparkSession = client.getSparkSession(expectedSparkSession.getId());
            // assert
            assertSparkSessionEquals(expectedSparkSession, actualSparkSession);
        }
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

        SparkSession expected = null;

        try {
            // act
            expected = client.createSparkSession(options, true);

            // assert
            assertNotNull(expected);
            assertEquals(sessionName, expected.getName());
            assertEquals(getSparkPoolName(), expected.getSparkPoolName());

            // act
            SparkSession actual = client.getSparkSession(expected.getId(), true);

            // assert
            assertSparkSessionEquals(expected, actual);
        } finally {
            // clean up
            if (expected != null) {
                client.cancelSparkSession(expected.getId());
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

        SparkSession session = null;
        SparkStatement expected = null;

        try {
            // act
            session = client.createSparkSession(sessionOptions, true);

            if (!interceptorManager.isPlaybackMode()) {
                Thread.sleep(360000);
            }

            client.resetSparkSessionTimeout(session.getId());

            String code = "print('hello, Azure CLI')";

            SparkStatementOptions options = new SparkStatementOptions()
                .setKind(SparkStatementLanguageType.PYSPARK)
                .setCode(code);

            // act
            expected = client.createSparkStatement(session.getId(), options);

            // assert
            assertNotNull(expected);
            assertEquals(code, expected.getCode());

            // act
            SparkStatement actual = client.getSparkStatement(session.getId(), expected.getId());

            // assert
            assertSparkStatementEquals(expected, actual);

            // act
            SparkStatementCollection list = client.getSparkStatements(session.getId());

            // assert
            assertEquals(1, list.getTotal());
            assertSparkStatementEquals(expected, list.getStatements().get(0));
        } finally {
            // clean up
            if (expected != null) {
                client.cancelSparkStatement(session.getId(), expected.getId());
            }
            if (session != null) {
                client.cancelSparkSession(session.getId());
            }
        }
    }
}
