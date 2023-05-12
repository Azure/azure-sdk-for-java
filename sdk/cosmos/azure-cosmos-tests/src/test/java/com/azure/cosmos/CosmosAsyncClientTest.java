// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.guava27.Strings;
import org.testng.ITest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

public abstract class CosmosAsyncClientTest implements ITest {

    public static final String ROUTING_GATEWAY_EMULATOR_PORT = ":8081";
    public static final String COMPUTE_GATEWAY_EMULATOR_PORT = ":8903";
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
        return this.clientBuilder.getConnectionPolicy();
    }

    @Override
    public final String getTestName() {
        return this.testName;
    }

    @BeforeMethod(alwaysRun = true)
    public final void setTestName(Method method, Object[] row) {
        String testClassAndMethodName = Strings.lenientFormat("%s::%s",
                method.getDeclaringClass().getSimpleName(),
                method.getName());

        this.clientBuilder.buildConnectionPolicy();
        if (this.clientBuilder.getConnectionPolicy() != null && this.clientBuilder.configs() != null) {
            String connectionMode = this.clientBuilder.getConnectionPolicy().getConnectionMode() == ConnectionMode.DIRECT
                    ? "Direct " + this.clientBuilder.configs().getProtocol()
                    : this.clientBuilder.getEndpoint().contains(COMPUTE_GATEWAY_EMULATOR_PORT) ? "ComputeGW" : "Gateway";

            String template = clientBuilder.isContentResponseOnWriteEnabled() ?
                "%s[%s with %s consistency]" :
                "%s[%s with %s consistency ContentOnWriteDisabled]";

            this.testName = Strings.lenientFormat(template,
                    testClassAndMethodName,
                    connectionMode,
                    clientBuilder.getConsistencyLevel());
        } else {
            this.testName = testClassAndMethodName;
        }

        String suffix = this.resolveTestNameSuffix(row);
        if (suffix != null && !suffix.isEmpty()) {
            this.testName += "(" + suffix + ")";
        }
    }

    @AfterMethod(alwaysRun = true)
    public final void unsetTestName() {
        this.testName = null;
    }

    public String resolveTestNameSuffix(Object[] row) {
        return "";
    }
}
