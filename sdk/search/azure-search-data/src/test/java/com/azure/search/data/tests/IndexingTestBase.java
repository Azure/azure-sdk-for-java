// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.common.jsonwrapper.JsonWrapper;
import com.azure.search.data.common.jsonwrapper.api.Config;
import com.azure.search.data.common.jsonwrapper.api.JsonApi;
import com.azure.search.data.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.data.env.SearchIndexClientTestBase;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class IndexingTestBase extends SearchIndexClientTestBase {
    protected static final String INDEX_NAME = "hotels";
    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    protected static final String HOTEL_ID_FIELD = "HotelId";
    protected static final String LAST_RENOVATION_DATE_FIELD = "LastRenovationDate";
    protected static final String HOTEL_ID = "1";
    protected static final String LAST_RENOVATION_DATE = "2010-06-27T00:00:00Z";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        initializeClient();
    }

    protected <T> void uploadDocuments(T uploadDoc) throws Exception {
        JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        jsonApi.configure(Config.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonApi.configureTimezone();
        Map<String, Object> document = jsonApi.convertObjectToType(uploadDoc, Map.class);
        List<IndexAction> indexActions = new LinkedList<>();
        indexActions.add(new IndexAction()
            .actionType(IndexActionType.UPLOAD)
            .additionalProperties(document));

        indexDocuments(indexActions);

        // Wait 2 secs to allow index request to finish
        Thread.sleep(2000);
    }

    @Test
    public abstract void countingDocsOfNewIndexGivesZero();

    @Test
    public abstract void dynamicDocumentDateTimesRoundTripAsUtc() throws ParseException, Exception;

    @Test
    public abstract void staticallyTypedDateTimesRoundTripAsUtc() throws ParseException, Exception;

    protected abstract void indexDocuments(List<IndexAction> indexActions);

    protected abstract void initializeClient();
}
