// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import java.lang.reflect.Method;

@Listeners({TestNGLogListener.class, CosmosNettyLeakDetectorFactory.class})
public abstract class CosmosEncryptionAsyncClientTest implements ITest {
    protected static Logger logger = LoggerFactory.getLogger(CosmosEncryptionAsyncClientTest.class.getSimpleName());
    protected static final int SUITE_SETUP_TIMEOUT = 120000;
    private static final ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor cosmosClientBuilderAccessor =
        ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();

    private final CosmosClientBuilder clientBuilder;
    private String testName;

    public CosmosEncryptionAsyncClientTest() {
        this(new CosmosClientBuilder());
    }

    public CosmosEncryptionAsyncClientTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    public final CosmosClientBuilder getClientBuilder() {
        return this.clientBuilder;
    }

    @Override
    public final String getTestName() {
        return this.testName;
    }

    @BeforeMethod(alwaysRun = true)
    public final void setTestName(Method method, Object[] row) {
        String testClassAndMethodName = String.format("%s::%s",
            method.getDeclaringClass().getSimpleName(),
            method.getName());

        ConnectionPolicy connectionPolicy = cosmosClientBuilderAccessor.getConnectionPolicy(clientBuilder);
        Configs configs = cosmosClientBuilderAccessor.getConfigs(clientBuilder);
        if (connectionPolicy != null && configs != null) {
            String connectionMode = connectionPolicy.getConnectionMode() == ConnectionMode.DIRECT
                ? "Direct " + configs.getProtocol()
                : "Gateway";

            ConsistencyLevel consistencyLevel = cosmosClientBuilderAccessor.getConsistencyLevel(clientBuilder);
            this.testName = String.format("%s[%s with %s consistency]",
                testClassAndMethodName,
                connectionMode,
                consistencyLevel);
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
