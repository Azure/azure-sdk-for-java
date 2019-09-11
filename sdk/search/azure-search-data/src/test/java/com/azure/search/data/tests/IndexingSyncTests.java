// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.core.exception.HttpResponseException;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.customization.Document;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import com.azure.search.data.generated.models.IndexBatch;
import com.azure.search.data.models.Book;
import com.azure.search.service.models.Index;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class IndexingSyncTests extends IndexingTestBase {
    private SearchIndexClient client;
    private static final String BOOKS_INDEX_NAME = "books";
    private static final String BOOKS_INDEX_JSON = "BooksIndexData.json";
    private static final String PUBLISH_DATE_FIELD = "PublishDate";
    private static final String ISBN_FIELD = "ISBN";
    private static final String ISBN1 = "1";
    private static final String ISBN2 = "2";
    private static final String DATE_UTC = "2010-06-27T00:00:00Z";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Override
    public void countingDocsOfNewIndexGivesZero() {
        Long actual = client.countDocuments();
        Long expected = 0L;

        Assert.assertEquals(expected, actual);
    }

    @Override
    public void indexWithInvalidDocumentThrowsException() {
        thrown.expect(HttpResponseException.class);
        thrown.expectMessage("The request is invalid. Details: actions : 0: Document key cannot be missing or empty.");

        List<IndexAction> indexActions = new LinkedList<>();
        addDocumentToIndexActions(indexActions, IndexActionType.UPLOAD, new Document());
        client.index(new IndexBatch().actions(indexActions));
    }

    public void dynamicDocumentDateTimesRoundTripAsUtc() throws Exception {
        // Book 1's publish date is in UTC format, and book 2's is unspecified.
        List<HashMap<String, Object>> books = Arrays.asList(
            new HashMap<String, Object>() {{
                put(ISBN_FIELD, ISBN1);
                put(PUBLISH_DATE_FIELD, DATE_UTC);
            }},
            new HashMap<String, Object>()
            {{
                put(ISBN_FIELD, ISBN2);
                put(PUBLISH_DATE_FIELD, "2010-06-27T00:00:00-00:00");
            }}
        );

        // Create 'books' index
        Reader indexData = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(BOOKS_INDEX_JSON));
        Index index = new ObjectMapper().readValue(indexData, Index.class);
        if (!interceptorManager.isPlaybackMode()) {
            searchServiceClient.indexes().create(index);
        }

        // Upload and retrieve book documents
        uploadDocuments(client, BOOKS_INDEX_NAME, books);
        Document actualBook1 = client.getDocument(ISBN1);
        Document actualBook2 = client.getDocument(ISBN2);

        // Verify
        Assert.assertEquals(DATE_UTC, actualBook1.get(PUBLISH_DATE_FIELD));
        Assert.assertEquals(DATE_UTC, actualBook2.get(PUBLISH_DATE_FIELD));
    }

    @Override
    public void staticallyTypedDateTimesRoundTripAsUtc() throws Exception {
        // Book 1's publish date is in UTC format, and book 2's is unspecified.
        DateFormat dateFormatUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DateFormat dateFormatUnspecifiedTimezone = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Book> books = Arrays.asList(
            new Book()
                .ISBN(ISBN1)
                .publishDate(dateFormatUtc.parse(DATE_UTC)),
            new Book()
                .ISBN(ISBN2)
                .publishDate(dateFormatUnspecifiedTimezone.parse("2010-06-27 00:00:00"))
        );

        // Create 'books' index
        Reader indexData = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(BOOKS_INDEX_JSON));
        Index index = new ObjectMapper().readValue(indexData, Index.class);
        if (!interceptorManager.isPlaybackMode()) {
            searchServiceClient.indexes().create(index);
        }

        // Upload and retrieve book documents
        uploadDocuments(client, BOOKS_INDEX_NAME, books);
        Document actualBook1 = client.getDocument(ISBN1);
        Document actualBook2 = client.getDocument(ISBN2);

        // Verify
        Assert.assertEquals(books.get(0).publishDate(), actualBook1.as(Book.class).publishDate());
        Assert.assertEquals(books.get(1).publishDate(), actualBook2.as(Book.class).publishDate());
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildClient();
    }
}
