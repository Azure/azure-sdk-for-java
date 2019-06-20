package com.azure.storage;

import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.test.TestBase;
import java.util.function.BiFunction;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import static org.junit.Assert.fail;

public class StorageTestBase extends TestBase {
    private final ServiceLogger logger = new ServiceLogger(StorageTestBase.class);
    private final String azureStorageConnectionString = ConfigurationManager.getConfiguration().get("AZURE_STORAGE_CONNECTION_STRING");
    public final String azureStorageFileEndpoint = ConfigurationManager.getConfiguration().get("AZURE_STORAGE_FILE_ENDPOINT");

    @Rule
    public TestName testName = new TestName();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Override
    public String testName() {
        return testName.getMethodName();
    }

    public <T> T setupClient(BiFunction<String, String, T> clientBuilder) {
        if (ImplUtils.isNullOrEmpty(azureStorageConnectionString) || ImplUtils.isNullOrEmpty(azureStorageFileEndpoint)) {
            logger.asWarning().log("Connection string and endpoint must be set to build the testing client");
            fail();
            return null;
        }
        return clientBuilder.apply(azureStorageConnectionString, azureStorageFileEndpoint);
    }

    public String generateName(String prefix) {
        return testResourceNamer.randomName(prefix, 16);
    }
}
