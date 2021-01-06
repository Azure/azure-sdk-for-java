package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.benchmark.Configuration;
import com.google.common.base.Preconditions;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CtlWorkload {
    private final static Logger LOGGER = LoggerFactory.getLogger(CtlWorkload.class);

    private final Configuration _configuration;
    private final CosmosAsyncClient _client;
    private final ResourceManager _resourceManager;
    private final Semaphore _concurrencyControlSemaphore;

    public CtlWorkload(final Configuration configuration) {
        Preconditions.checkNotNull(configuration, "The Workload configuration defining the parameters can not be null");

        _configuration = configuration;
        _client = AsyncClientFactory.buildAsyncClient(configuration);
        _resourceManager = new ResourceManager(configuration, _client);
        _concurrencyControlSemaphore = new Semaphore(configuration.getConcurrency());
    }

    public void setup() throws CosmosException {
        _resourceManager.initializeDatabase();
        _resourceManager.createContainers();
    }

    public void run() {
    }

    public void shutdown() {
        _resourceManager.deleteResources();
    }
}
