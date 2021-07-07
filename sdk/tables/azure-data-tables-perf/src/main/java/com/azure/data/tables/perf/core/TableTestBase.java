// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.perf.core;

import com.azure.core.test.utils.ResourceNamer;
import com.azure.data.tables.TableAsyncClient;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.azure.perf.test.core.PerfStressOptions;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public abstract class TableTestBase<TOptions extends PerfStressOptions> extends ServiceTest<TOptions> {
    private static final String STRING_TYPE_PROPERTY_NAME = "StringTypeProperty";
    private static final String STRING_TYPE_PROPERTY_VALUE = "StringTypeProperty";
    private static final String DATETIME_TYPE_PROPERTY_NAME = "DatetimeTypeProperty";
    private static final OffsetDateTime DATETIME_TYPE_PROPERTY_VALUE = OffsetDateTime.of(1970, 10, 4, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final String UUID_TYPE_PROPERTY_NAME = "GuidTypeProperty";
    private static final UUID UUID_TYPE_PROPERTY_VALUE = UUID.fromString("c9da6455-213d-42c9-9a79-3e9149a57833");
    private static final String BINARY_TYPE_PROPERTY_NAME = "BinaryTypeProperty";
    private static final byte[] BINARY_TYPE_PROPERTY_VALUE = "BinaryTypeProperty".getBytes();
    private static final String LONG_TYPE_PROPERTY_NAME = "Int64TypeProperty";
    private static final long LONG_TYPE_PROPERTY_VALUE = 2^32 + 1;
    private static final String DOUBLE_TYPE_PROPERTY_NAME = "DoubleTypeProperty";
    private static final double DOUBLE_TYPE_PROPERTY_VALUE = 200.23d;
    private static final String INT_TYPE_PROPERTY_NAME = "IntTypeProperty";
    private static final int INT_TYPE_PROPERTY_VALUE = 5;

    private static final String STRING_TYPE_PROPERTY1_NAME = "StringTypeProperty1";
    private static final String STRING_TYPE_PROPERTY1_VALUE = "StringTypeProperty";
    private static final String STRING_TYPE_PROPERTY2_NAME = "StringTypeProperty2";
    private static final String STRING_TYPE_PROPERTY2_VALUE = "1970-10-04T00:00:00+00:00";
    private static final String STRING_TYPE_PROPERTY3_NAME = "StringTypeProperty3";
    private static final String STRING_TYPE_PROPERTY3_VALUE = "c9da6455-213d-42c9-9a79-3e9149a57833";
    private static final String STRING_TYPE_PROPERTY4_NAME = "StringTypeProperty4";
    private static final String STRING_TYPE_PROPERTY4_VALUE = "BinaryTypeProperty";
    private static final String STRING_TYPE_PROPERTY5_NAME = "StringTypeProperty5";
    private static final String STRING_TYPE_PROPERTY5_VALUE = Long.toString(2^32 + 1);
    private static final String STRING_TYPE_PROPERTY6_NAME = "StringTypeProperty6";
    private static final String STRING_TYPE_PROPERTY6_VALUE = "200.23";
    private static final String STRING_TYPE_PROPERTY7_NAME = "StringTypeProperty7";
    private static final String STRING_TYPE_PROPERTY7_VALUE = "5";

    protected final TableClient tableClient;
    protected final TableAsyncClient tableAsyncClient;

    /**
     * Creates an instance of the performance test.
     *
     * @param options The options configured for the test.
     */
    public TableTestBase(TOptions options) {
        super(options);

        String tableName = new ResourceNamer("PerfTest").randomName("table", 20);

        tableClient = tableServiceClient.getTableClient(tableName);
        tableAsyncClient = tableServiceAsyncClient.getTableClient(tableName);
    }

    protected TableEntity generateEntityWithAllTypes(String partitionKey, String rowKey) {
        return new TableEntity(partitionKey, rowKey)
            .addProperty(STRING_TYPE_PROPERTY_NAME, STRING_TYPE_PROPERTY_VALUE)
            .addProperty(DATETIME_TYPE_PROPERTY_NAME, DATETIME_TYPE_PROPERTY_VALUE)
            .addProperty(UUID_TYPE_PROPERTY_NAME, UUID_TYPE_PROPERTY_VALUE)
            .addProperty(BINARY_TYPE_PROPERTY_NAME, BINARY_TYPE_PROPERTY_VALUE)
            .addProperty(LONG_TYPE_PROPERTY_NAME, LONG_TYPE_PROPERTY_VALUE)
            .addProperty(DOUBLE_TYPE_PROPERTY_NAME, DOUBLE_TYPE_PROPERTY_VALUE)
            .addProperty(INT_TYPE_PROPERTY_NAME, INT_TYPE_PROPERTY_VALUE);
    }

    protected TableEntity generateEntityWithStringsOnly(String partitionKey, String rowKey) {
        return new TableEntity(partitionKey, rowKey)
            .addProperty(STRING_TYPE_PROPERTY1_NAME, STRING_TYPE_PROPERTY1_VALUE)
            .addProperty(STRING_TYPE_PROPERTY2_NAME, STRING_TYPE_PROPERTY2_VALUE)
            .addProperty(STRING_TYPE_PROPERTY3_NAME, STRING_TYPE_PROPERTY3_VALUE)
            .addProperty(STRING_TYPE_PROPERTY4_NAME, STRING_TYPE_PROPERTY4_VALUE)
            .addProperty(STRING_TYPE_PROPERTY5_NAME, STRING_TYPE_PROPERTY5_VALUE)
            .addProperty(STRING_TYPE_PROPERTY6_NAME, STRING_TYPE_PROPERTY6_VALUE)
            .addProperty(STRING_TYPE_PROPERTY7_NAME, STRING_TYPE_PROPERTY7_VALUE);
    }
}
