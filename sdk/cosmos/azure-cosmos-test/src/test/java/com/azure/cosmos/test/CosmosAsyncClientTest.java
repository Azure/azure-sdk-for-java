// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.guava27.Strings;
import org.testng.ITest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

public abstract class CosmosAsyncClientTest implements ITest {

    protected static final String ROUTING_GATEWAY_EMULATOR_PORT = ":8081";
    protected static final String COMPUTE_GATEWAY_EMULATOR_PORT = ":8903";
    private final CosmosClientBuilder clientBuilder;
    private String testName;

    public CosmosAsyncClientTest() {
        this(new CosmosClientBuilder());
    }

    public CosmosAsyncClientTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    public final CosmosClientBuilder getClientBuilder() {
        return this.clientBuilder;
    }

    public final ConnectionPolicy getConnectionPolicy() {
        return ImplementationBridgeHelpers
            .CosmosClientBuilderHelper
            .getCosmosClientBuilderAccessor()
            .getConnectionPolicy(this.clientBuilder);
    }

    public final Configs getConfigs() {
        return ImplementationBridgeHelpers
            .CosmosClientBuilderHelper
            .getCosmosClientBuilderAccessor()
            .getConfigs(this.clientBuilder);
    }
    public final String getEndpoint() {
        return ImplementationBridgeHelpers
            .CosmosClientBuilderHelper
            .getCosmosClientBuilderAccessor()
            .getEndpoint(this.clientBuilder);
    }

    public final ConsistencyLevel getConsistencyLevel() {
        return ImplementationBridgeHelpers
            .CosmosClientBuilderHelper
            .getCosmosClientBuilderAccessor()
            .getConsistencyLevel(this.clientBuilder);
    }

    @Override
    public final String getTestName() {
        return this.testName;
    }

    @BeforeMethod(alwaysRun = true)
    public final void setTestName(Method method) {
        String testClassAndMethodName = Strings.lenientFormat("%s::%s",
            method.getDeclaringClass().getSimpleName(),
            method.getName());

        if (this.getConnectionPolicy() != null && this.getConfigs() != null) {
            String connectionMode = this.getConnectionPolicy().getConnectionMode() == ConnectionMode.DIRECT
                ? "Direct " + this.getConfigs().getProtocol()
                : this.getEndpoint().contains(COMPUTE_GATEWAY_EMULATOR_PORT) ? "ComputeGW" : "Gateway";

            this.testName = Strings.lenientFormat(
                "%s[%s with %s consistency]",
                testClassAndMethodName,
                connectionMode,
                this.getConsistencyLevel());
        } else {
            this.testName = testClassAndMethodName;
        }
    }

    @AfterMethod(alwaysRun = true)
    public final void unsetTestName() {
        this.testName = null;
    }
}

