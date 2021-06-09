package com.azure.data.tables.perf;

import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.perf.core.TableTestBase;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class ListTableEntitiesTest extends TableTestBase<PerfStressOptions> {
    private final String partitionKey;

    public ListTableEntitiesTest(PerfStressOptions options) {
        super(options);

        partitionKey = "listentitiestest-pk-" + UUID.randomUUID();
    }

    public Mono<Void> globalSetupAsync() {
        return tableAsyncClient.createTable()
            .then(super.globalSetupAsync()
                .then(Flux.range(0, options.getCount())
                    .map(i -> new TableEntity(partitionKey, "listentitiestest-rk" + UUID.randomUUID()))
                    .flatMap(tableAsyncClient::createEntity)
                    .then()));
    }

    @Override
    public void run() {
        tableClient.listEntities().forEach(b -> {
        });
    }

    @Override
    public Mono<Void> runAsync() {
        return tableAsyncClient.listEntities()
            .then();
    }
}
