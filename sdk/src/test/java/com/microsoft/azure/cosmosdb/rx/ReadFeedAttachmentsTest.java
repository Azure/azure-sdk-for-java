/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Attachment;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.MediaOptions;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;

import rx.Observable;
import rx.observers.TestSubscriber;

public class ReadFeedAttachmentsTest extends TestSuiteBase {
    public final static String DATABASE_ID = getDatabaseId(ReadFeedAttachmentsTest.class);

    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private Document createdDocument;

    private AsyncDocumentClient.Builder clientBuilder;
    private AsyncDocumentClient client;

    private PartitionKey pk;
    @Factory(dataProvider = "clientBuilders")
    public ReadFeedAttachmentsTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = 30000000)
    public void readExternalAttachments() throws Exception {
        createdDocument = createDocument(client, createdDatabase.getId(),
                createdCollection.getId(), getDocumentDefinition());

        List<Attachment> createdAttachments = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            createdAttachments.add(createAttachments(client));
        }
        
        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(2);
        options.setPartitionKey(pk);

        Observable<FeedResponse<Attachment>> feedObservable = client.readAttachments(getDocumentLink(), options);

        int expectedPageSize = (createdAttachments.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<Attachment> validator = new FeedResponseListValidator
                .Builder<Attachment>()
                .totalSize(createdAttachments.size())
                .exactlyContainsInAnyOrder(createdAttachments
                        .stream()
                        .map(d -> d.getResourceId())
                        .collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<Attachment>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readAndUpdateEmbededAttachments() throws Exception {
        createdDocument = createDocument(client, createdDatabase.getId(),
                createdCollection.getId(), getDocumentDefinition());

        FeedOptions feedOptions = new FeedOptions();
        feedOptions.setMaxItemCount(1);
        feedOptions.setPartitionKey(pk);
        String documentLink = "dbs/" + getDatabaseId() + "/colls/" + getCollectionId() + "/docs/" + getDocumentId();

        MediaOptions options = new MediaOptions();
        options.setContentType("application/octet-stream");

        RequestOptions reqOptions = new RequestOptions();
        reqOptions.setPartitionKey(pk);


        try(InputStream ipStream = getMedia1Stream()) {
            TestSubscriber<ResourceResponse<Attachment>> subscriber = new TestSubscriber<>();
            client.createAttachment(documentLink, ipStream, options, reqOptions)
                    .toBlocking()
                    .subscribe(subscriber);
            subscriber.assertNoErrors();
        }

        try(InputStream ipStream = getMedia1Stream()) {
            validateReadEmbededAttachment(documentLink, ipStream, feedOptions);
        }

        validateUpdateEmbededAttachment(documentLink, options, feedOptions);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws DocumentClientException {
        client = clientBuilder.build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);
        createdCollection = createCollection(client, createdDatabase.getId(),
                getCollectionDefinition());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase.getId());
        safeClose(client);
    }

    private void validateUpdateEmbededAttachment(String documentLink, MediaOptions mediaOptions, FeedOptions feedOptions) throws Exception {
        String mediaLink = client.readAttachments(documentLink, feedOptions)
                .map( response -> response.getResults().iterator().next().getMediaLink())
                .toBlocking()
                .first();

        try (InputStream ipStream = getMedia2Stream()) {
            client.updateMedia(mediaLink, ipStream, mediaOptions)
                    .toBlocking().first();
        }

        try (InputStream ipStream = getMedia2Stream()) {
            validateReadEmbededAttachment(documentLink, ipStream, feedOptions);
        }
    }

    private void validateReadEmbededAttachment(String documentLink, InputStream ipStream, FeedOptions feedOptions) {
        TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        client.readAttachments(documentLink, feedOptions)
                .map( response -> response.getResults().iterator().next().getMediaLink())
                .flatMap(mediaLink -> client.readMedia(mediaLink))
                .map(mediaResponse -> {

                    try(InputStream responseMediaStream = mediaResponse.getMedia()) {
                        return IOUtils.contentEquals(ipStream, responseMediaStream);
                    } catch (IOException e) {
                        return false;
                    }
                })
                .filter(x -> x) // Filter only right extractions back
                .toBlocking()
                .subscribe(subscriber);

        subscriber.assertNoErrors();
        subscriber.assertCompleted();
        subscriber.assertValueCount(1);
    }

    private InputStream getMedia1Stream()
    {
        return this.getClass().getResourceAsStream("/cosmosdb-1.png");
    }

    private InputStream getMedia2Stream()
    {
        return this.getClass().getResourceAsStream("/Microsoft.jpg");
    }

    private String getCollectionId() {
        return createdCollection.getId();
    }

    private String getDatabaseId() {
        return createdDatabase.getId();
    }
    
    private String getDocumentId() {
        return createdDocument.getId();
    }

    public Attachment createAttachments(AsyncDocumentClient client) throws DocumentClientException {
        Attachment attachment = getAttachmentDefinition();
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(pk);
        return client.createAttachment(getDocumentLink(), attachment, options).toBlocking().single().getResource();
    }

    public String getDocumentLink() {
        return "dbs/" + getDatabaseId() + "/colls/" + getCollectionId() + "/docs/" + getDocumentId();
    }

    private Document getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        pk = new PartitionKey(uuid);
        Document doc = new Document(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , uuid, uuid));
        return doc;
    }
    
    private static Attachment getAttachmentDefinition() {
        String uuid = UUID.randomUUID().toString();
        String type = "application/text";
        return new Attachment(String.format(
                    "{" +
                    "  'id': '%s'," +
                    "  'media': 'http://xstore.'," +
                    "  'MediaType': 'Book'," +
                    "  'Author': 'My Book Author'," +
                    "  'Title': 'My Book Title'," +
                    "  'contentType': '%s'" +
                    "}", uuid, type));
    }
}
