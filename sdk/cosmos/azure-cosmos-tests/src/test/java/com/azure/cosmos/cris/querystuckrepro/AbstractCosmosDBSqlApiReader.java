// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.cris.querystuckrepro;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;

public abstract class AbstractCosmosDBSqlApiReader {
    protected boolean isPartitionedColl = true;

    protected DummyLogger logger = new DummyLogger();
    protected final CosmosDBSqlApiReaderDTO readerMd;
    public final Object collThroughput = null;

    protected final String filterQueryOverride = "n/a";

    protected DataFormat dataFormat = new DataFormat();

    protected final String query;

    protected final String partitionKeyStr;

    public int throughput = -1;

    public final CosmosAsyncContainer readContainer;

    protected AbstractCosmosDBSqlApiReader(CosmosDBSqlApiReaderDTO readerMd) {
        this.readerMd = readerMd;
        this.readContainer = readerMd.getReadContainer();
        this.query = readerMd.getQuery();
        this.partitionKeyStr = readerMd.getPartitionKeyStr();
    }

    public AzureDocumentDbUserContext getAzureDocumentDbUserContext(DataSession session) {
        return new AzureDocumentDbUserContext(session);
    }

    public abstract int read(DataSession dataSession, ReadAttributes readAttr) throws SDKException;
}
