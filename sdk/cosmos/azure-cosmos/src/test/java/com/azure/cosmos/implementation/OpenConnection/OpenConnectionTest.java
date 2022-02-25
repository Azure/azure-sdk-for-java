package com.azure.cosmos.implementation.OpenConnection;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.PartitionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class OpenConnectionTest {
    private final static Logger logger = LoggerFactory.getLogger(OpenConnectionTest.class);

    @Test
    public void openConnectionTest() {

        System.setProperty("COSMOS.ALWAYS_OPEN_CONNECTIONS", "true");

        DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
        directConnectionConfig.setIdleConnectionTimeout(Duration.ofSeconds(5));

        String endpoint = "";
        String key = "";
        CosmosAsyncClient client = new CosmosClientBuilder()
            .endpoint(endpoint)
            .key(key)
            .directMode(directConnectionConfig)
            .buildAsyncClient();

        CosmosAsyncDatabase database = client.getDatabase("TestDatabase");
        CosmosAsyncContainer container = database.getContainer("TestContainer");

        container.readItem("c637db9b-8d63-4fcd-91d3-ab6c9e61c83b", new PartitionKey("pk1"), TestItem.class).block();


        AtomicLong totalTime = new AtomicLong(0L);
        AtomicInteger totalSuccessfulRequest = new AtomicInteger(0);
        Instant startTime  = Instant.now();

        List<Integer> concurrency = Arrays.asList(1, 2, 3);
        for (int k = 0; k < concurrency.size(); k++) {
            int totalRequest = 10 * concurrency.get(k);

            for (int i = 0; i < 10; i++) {
                Flux.range(1, totalRequest/10)
                    .flatMap(index -> {
                        return container.readItem("c637db9b-8d63-4fcd-91d3-ab6c9e61c83b", new PartitionKey("pk1"), TestItem.class);
                    })
                    .flatMap(itemResponse -> {
                        totalTime.accumulateAndGet(itemResponse.getDuration().toMillis(), (oldValue, newValue) -> oldValue + newValue);
                        int total = totalSuccessfulRequest.incrementAndGet();

                        logger.info("Duration {} , diagnostics: {}", itemResponse.getDuration().toMillis(), itemResponse.getDiagnostics());
                        if (total == totalRequest) {
                            logger.info("Total time {}, total Request {}, Avg time {}", Duration.between(startTime, Instant.now()).toMillis(), total, totalTime.get()*1.0/totalRequest);
                        }
                        return Mono.empty();
                    })
                    .blockLast();
            }
        }



        client.close();
    }
}
