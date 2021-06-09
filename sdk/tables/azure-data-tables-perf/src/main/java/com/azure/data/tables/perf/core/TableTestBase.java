package com.azure.data.tables.perf.core;

import com.azure.data.tables.TableAsyncClient;
import com.azure.data.tables.TableClient;
import com.azure.perf.test.core.PerfStressOptions;

import java.util.UUID;

public abstract class TableTestBase<TOptions extends PerfStressOptions> extends ServiceTest<TOptions> {
    protected final TableClient tableClient;
    protected final TableAsyncClient tableAsyncClient;

    /**
     * Creates an instance of the performance test.
     *
     * @param options The options configured for the test.
     */
    public TableTestBase(TOptions options) {
        super(options);

        String tableName = "createtabletest-" + UUID.randomUUID();

        tableClient = tableServiceClient.getTableClient(tableName);
        tableAsyncClient = tableServiceAsyncClient.getTableClient(tableName);
    }
}
