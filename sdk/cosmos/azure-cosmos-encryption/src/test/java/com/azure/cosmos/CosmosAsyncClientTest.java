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
    public final void setTestName(Method method) {
        String testClassAndMethodName = Strings.lenientFormat("%s::%s",
            method.getDeclaringClass().getSimpleName(),
            method.getName());

        if (this.clientBuilder.getConnectionPolicy() != null && this.clientBuilder.configs() != null) {
            String connectionMode = this.clientBuilder.getConnectionPolicy().getConnectionMode() == ConnectionMode.DIRECT
                ? "Direct " + this.clientBuilder.configs().getProtocol()
                : "Gateway";

            this.testName = Strings.lenientFormat("%s[%s with %s consistency]",
                testClassAndMethodName,
                connectionMode,
                clientBuilder.getConsistencyLevel());
        } else {
            this.testName = testClassAndMethodName;
        }
    }

    @AfterMethod(alwaysRun = true)
    public final void unsetTestName() {
        this.testName = null;
    }
}
