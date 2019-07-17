// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.google.common.base.Strings;
import org.testng.ITest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

public abstract class CosmosClientTest implements ITest {

    private final CosmosClientBuilder clientBuilder;
    private String testName;

    public CosmosClientTest() {
        this(new CosmosClientBuilder());
    }

    public CosmosClientTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    public final CosmosClientBuilder clientBuilder() {
        return this.clientBuilder;
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

        if (this.clientBuilder.connectionPolicy() != null && this.clientBuilder.configs() != null) {
            String connectionMode = this.clientBuilder.connectionPolicy().connectionMode() == ConnectionMode.DIRECT
                    ? "Direct " + this.clientBuilder.configs().getProtocol()
                    : "Gateway";

            this.testName = Strings.lenientFormat("%s[%s with %s consistency]",
                    testClassAndMethodName,
                    connectionMode,
                    clientBuilder.consistencyLevel());
        } else {
            this.testName = testClassAndMethodName;
        }
    }

    @AfterMethod(alwaysRun = true)
    public final void unsetTestName() {
        this.testName = null;
    }
}
