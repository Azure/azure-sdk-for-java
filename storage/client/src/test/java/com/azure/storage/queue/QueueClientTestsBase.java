package com.azure.storage.queue;

import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.test.TestBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.Objects;
import java.util.function.Function;

public abstract class QueueClientTestsBase extends TestBase {
    private static final String AZURE_STORAGE_CONNECTION_STRING = "AZURE_STORAGE_CONNECTION_STRING";
    private static String connectionString;

    private final ServiceLogger logger = new ServiceLogger(QueueClientBuilderBase.class);

    @Rule
    public TestName testName = new TestName();

    @Override
    public String testName() {
        return testName.getMethodName();
    }

    <T> T clientSetup(Function<String, T> clientBuilder) {
        if (ImplUtils.isNullOrEmpty(connectionString)) {
            connectionString = interceptorManager.isPlaybackMode()
                ? "DefaultEndpointsProtocol=http;AccountName=playbackAccount;AccountKey=EJadf4qp7wsKngrEU8/chQiIJBAbXb/ouwuDt+m9Ksie+KHnFEM/5ROEK/OeAvxYuZ6OsOGag/bSltOiZJc2Mg==;EndpointSuffix=core.windows.net"
                : ConfigurationManager.getConfiguration().get(AZURE_STORAGE_CONNECTION_STRING);
        }

        Objects.requireNonNull(connectionString, "AZURE_STORAGE_CONNECTION_STRING expected to be set.");

        return Objects.requireNonNull(clientBuilder.apply(connectionString));
    }

    @Override
    protected void beforeTest() {
    }

    @Override
    protected void afterTest() {
    }

    @Test
    public abstract void createQueue();

    @Test
    public abstract void createQueueAlreadyExists();

    @Test
    public abstract void deleteQueue();

    @Test
    public abstract void deleteQueueDoesNotExist();

    @Test
    public abstract void getProperties();

    @Test
    public abstract void setMetadata();

    @Test
    public abstract void getAccessPolicy();

    @Test
    public abstract void setAccessPolicy();
}
