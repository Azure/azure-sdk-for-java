// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.guava27.Strings;
import org.testng.ITest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

public abstract class KafkaCosmosAsyncClientTest implements ITest {
    private final CosmosClientBuilder clientBuilder;
    private String testName;

    public KafkaCosmosAsyncClientTest() {
        this(new CosmosClientBuilder());
    }

    public KafkaCosmosAsyncClientTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    public final CosmosClientBuilder getClientBuilder() {
        return this.clientBuilder;
    }

    public final ConnectionPolicy getConnectionPolicy() {
        return CosmosBridgeInternal.getConnectionPolicy(this.clientBuilder);
    }

    public final Configs extractConfigs() {
        return BridgeInternal.extractConfigs(this.clientBuilder);
    }

    public final ConsistencyLevel getConsistencyLevel() {
        return CosmosBridgeInternal.getConsistencyLevel(this.clientBuilder);
    }

    @Override
    public final String getTestName() {
        return this.testName;
    }

    @BeforeMethod(alwaysRun = true)
    public final void setTestName(Method method, Object[] row) {
        this.testName = Strings.lenientFormat("%s::%s",
            method.getDeclaringClass().getSimpleName(),
            method.getName());
    }

    @AfterMethod(alwaysRun = true)
    public final void unsetTestName() {
        this.testName = null;
    }

}
