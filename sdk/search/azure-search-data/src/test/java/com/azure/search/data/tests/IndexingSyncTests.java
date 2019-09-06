// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.SearchIndexClient;
import org.joda.time.DateTime;
import org.junit.Assert;

public class IndexingSyncTests extends IndexingTestBase {
    private SearchIndexClient client;
    private int year = 2000,
        month = 1,
        day = 1,
        hour = 0,
        minute = 0,
        second = 0;

    @Override
    public void countingDocsOfNewIndexGivesZero() {
        Long actual = client.countDocuments();
        Long expected = 0L;

        Assert.assertEquals(expected, actual);
    }

    @Override
    public void dynamicDocumentDateTimesRoundTripAsUtc() {
        DateTime utcDateTime = new DateTime(year, month, day, hour, minute, second);

        //client.

    }

    @Override
    public void staticallyTypedDateTimesRoundTripAsUtc() {

    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildClient();
    }
}
