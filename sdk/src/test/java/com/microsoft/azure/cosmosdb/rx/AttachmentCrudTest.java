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

import java.util.UUID;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Attachment;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import rx.Observable;

public class AttachmentCrudTest extends TestSuiteBase {

    public final static String DATABASE_ID = getDatabaseId(AttachmentCrudTest.class);

    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private Document createdDocument;

    private AsyncDocumentClient.Builder clientBuilder;
    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public AttachmentCrudTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createAttachment() throws Exception {

        // create an Attachment
        String uuid = UUID.randomUUID().toString();
        Attachment attachment = getAttachmentDefinition(uuid, "application/text");
        
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(createdDocument.getId()));
        Observable<ResourceResponse<Attachment>> createObservable = client.createAttachment(getDocumentLink(), attachment, options);

        // validate attachment creation
        ResourceResponseValidator<Attachment> validator = new ResourceResponseValidator.Builder<Attachment>()
                .withId(attachment.getId())
                .withContentType("application/text")
                .notNullEtag()
                .build();
        validateSuccess(createObservable, validator);
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAttachment() throws Exception {
        
        // create an Attachment
        String uuid = UUID.randomUUID().toString();
        Attachment attachment = getAttachmentDefinition(uuid, "application/text");
        
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(createdDocument.getId()));
        Attachment readBackAttachment = client.createAttachment(getDocumentLink(), attachment, options).toBlocking().single().getResource();

        // read attachment
        Observable<ResourceResponse<Attachment>> readObservable = client.readAttachment(readBackAttachment.getSelfLink(), options);

        // validate attachment read
        ResourceResponseValidator<Attachment> validator = new ResourceResponseValidator.Builder<Attachment>()
                .withId(attachment.getId())
                .withContentType("application/text")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteAttachment() throws Exception {
        // create an Attachment
        String uuid = UUID.randomUUID().toString();
        Attachment attachment = getAttachmentDefinition(uuid, "application/text");
        
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(createdDocument.getId()));
        Attachment readBackAttachment = client.createAttachment(getDocumentLink(), attachment, options).toBlocking().single().getResource();

        // delete attachment
        Observable<ResourceResponse<Attachment>> deleteObservable = client.deleteAttachment(readBackAttachment.getSelfLink(), options);

        // validate attachment delete
        ResourceResponseValidator<Attachment> validator = new ResourceResponseValidator.Builder<Attachment>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);

        //TODO validate after deletion the resource is actually deleted (not found)
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void upsertAttachment() throws Exception {
        
        // create an Attachment
        String uuid = UUID.randomUUID().toString();
        Attachment attachment = getAttachmentDefinition(uuid, "application/text");
        
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(createdDocument.getId()));
        Attachment readBackAttachment = client.upsertAttachment(getDocumentLink(), attachment, options).toBlocking().single().getResource();

        // read attachment
        Observable<ResourceResponse<Attachment>> readObservable = client.readAttachment(readBackAttachment.getSelfLink(), options);

        // validate attachment read
        ResourceResponseValidator<Attachment> validator = new ResourceResponseValidator.Builder<Attachment>()
                .withId(attachment.getId())
                .withContentType("application/text")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);
        
        //update attachment
        readBackAttachment.setContentType("application/json");

        Observable<ResourceResponse<Attachment>> updateObservable = client.upsertAttachment(getDocumentLink(), readBackAttachment, options);

        // validate attachment update
        ResourceResponseValidator<Attachment> validatorForUpdate = new ResourceResponseValidator.Builder<Attachment>()
                .withId(readBackAttachment.getId())
                .withContentType("application/json")
                .notNullEtag()
                .build();
        validateSuccess(updateObservable, validatorForUpdate);   
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceAttachment() throws Exception {
        
        // create an Attachment
        String uuid = UUID.randomUUID().toString();
        Attachment attachment = getAttachmentDefinition(uuid, "application/text");
        
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(createdDocument.getId()));
        Attachment readBackAttachment = client.createAttachment(getDocumentLink(), attachment, options).toBlocking().single().getResource();

        // read attachment
        Observable<ResourceResponse<Attachment>> readObservable = client.readAttachment(readBackAttachment.getSelfLink(), options);

        // validate attachment read
        ResourceResponseValidator<Attachment> validator = new ResourceResponseValidator.Builder<Attachment>()
                .withId(attachment.getId())
                .withContentType("application/text")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);
        
        //update attachment
        readBackAttachment.setContentType("application/json");

        Observable<ResourceResponse<Attachment>> updateObservable = client.replaceAttachment(readBackAttachment, options);

        // validate attachment update
        ResourceResponseValidator<Attachment> validatorForUpdate = new ResourceResponseValidator.Builder<Attachment>()
                .withId(readBackAttachment.getId())
                .withContentType("application/json")
                .notNullEtag()
                .build();
        validateSuccess(updateObservable, validatorForUpdate);   
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();;      
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);
        createdCollection = createCollection(client, createdDatabase.getId(), getCollectionDefinition());
        createdDocument = createDocument(client, createdDatabase.getId(), createdCollection.getId(), getDocumentDefinition());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase.getId());
        safeClose(client);
    }
    
    private String getDocumentLink() {
        return createdDocument.getSelfLink();
    }
    
    private static Document getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , uuid, uuid));
        return doc;
    }
    
    private static Attachment getAttachmentDefinition(String uuid, String type) {
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
