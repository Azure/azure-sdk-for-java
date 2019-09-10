// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.env.SearchIndexClientTestBase;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

public abstract class IndexingTestBase extends SearchIndexClientTestBase {
    protected static final String INDEX_NAME = "hotels";
    protected static final DateFormat DATE_FORMAT_UTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    protected static final DateFormat DATE_FORMAT_UNSPECIFIED_TIMEZONE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected static final String HOTEL_ID_FIELD = "HotelId";
    protected static final String LAST_RENOVATION_DATE_FIELD = "LastRenovationDate";
    protected static final String HOTEL_ID1 = "1";
    protected static final String HOTEL_ID2 = "2";
    protected static final String DATE_UTC = "2010-06-27T00:00:00Z";
    protected static final String DATE_UNSPECIFIED_TIMEZONE = "2010-06-27 00:00:00";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        initializeClient();
    }

    @Test
    public abstract void countingDocsOfNewIndexGivesZero();

    @Test
    public abstract void indexWithInvalidDocumentThrowsException();

    @Test
    public abstract void dynamicDocumentDateTimesRoundTripAsUtc() throws Exception;

    @Test
    public abstract void staticallyTypedDateTimesRoundTripAsUtc() throws Exception;

    protected abstract void initializeClient();

    protected void addDocumentToIndexActions(List<IndexAction> indexActions, IndexActionType indexActionType, HashMap<String, Object> document) {
        indexActions.add(new IndexAction()
            .actionType(indexActionType)
            .additionalProperties(document));
    }
}
