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
package com.microsoft.azure.cosmos;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.internal.BaseAuthorizationTokenProvider;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.OperationType;
import com.microsoft.azure.cosmosdb.internal.Paths;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.Utils;
import com.microsoft.azure.cosmosdb.rx.FeedResponseListValidator;
import com.microsoft.azure.cosmosdb.rx.TestConfigurations;
import com.microsoft.azure.cosmosdb.rx.TestSuiteBase;
import com.microsoft.azure.cosmosdb.rx.internal.Configs;
import com.microsoft.azure.cosmosdb.rx.internal.HttpClientFactory;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.client.RxClient;
import io.reactivex.netty.protocol.http.client.CompositeHttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rx.Observable;

public class CosmosPartitionKeyTests extends TestSuiteBase {

    private final static String NON_PARTITIONED_CONTAINER_ID = "NonPartitionContainer" + UUID.randomUUID().toString();
    private final static String NON_PARTITIONED_CONTAINER_DOCUEMNT_ID = "NonPartitionContainer_Document" + UUID.randomUUID().toString();

    private CosmosClient client;
    private CosmosDatabase createdDatabase;
    private CosmosClientBuilder clientBuilder;

    @Factory(dataProvider = "clientBuilders")
    public CosmosPartitionKeyTests(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws URISyntaxException, IOException {
        client = clientBuilder.build();
        createdDatabase = getSharedCosmosDatabase(client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }

    private void createContainerWithoutPk() throws URISyntaxException, IOException {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        HttpClientFactory factory = new HttpClientFactory(new Configs())
                .withMaxIdleConnectionTimeoutInMillis(connectionPolicy.getIdleConnectionTimeoutInMillis())
                .withPoolSize(connectionPolicy.getMaxPoolSize())
                .withHttpProxy(connectionPolicy.getProxy())
                .withRequestTimeoutInMillis(connectionPolicy.getRequestTimeoutInMillis());

        CompositeHttpClient<ByteBuf, ByteBuf> httpClient = factory.toHttpClientBuilder().build();
        
        // Create a non partitioned collection using the rest API and older version
        String resourceId = Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.getId();
        String path = Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.getId() + "/" + Paths.COLLECTIONS_PATH_SEGMENT + "/";
        DocumentCollection collection = new DocumentCollection();
        collection.setId(NON_PARTITIONED_CONTAINER_ID);
        
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(HttpConstants.HttpHeaders.X_DATE, Utils.nowAsRFC1123());
        headers.put(HttpConstants.HttpHeaders.VERSION, "2018-09-17");
        BaseAuthorizationTokenProvider base = new BaseAuthorizationTokenProvider(TestConfigurations.MASTER_KEY);
        String authorization = base.generateKeyAuthorizationSignature(HttpConstants.HttpMethods.POST, resourceId, Paths.COLLECTIONS_PATH_SEGMENT, headers);
        headers.put(HttpConstants.HttpHeaders.AUTHORIZATION, URLEncoder.encode(authorization, "UTF-8"));
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Create,
                ResourceType.DocumentCollection, path, collection, headers, new RequestOptions());

        String[] baseUrlSplit = TestConfigurations.HOST.split(":");
        String resourceUri = baseUrlSplit[0] + ":" + baseUrlSplit[1] + ":" + baseUrlSplit[2].split("/")[
            0] + "//" + Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.getId() + "/" + Paths.COLLECTIONS_PATH_SEGMENT + "/";
        URI uri = new URI(resourceUri);

        HttpClientRequest<ByteBuf> httpRequest = HttpClientRequest.create(HttpMethod.POST, uri.toString());

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpRequest.withHeader(entry.getKey(), entry.getValue());
        }

        httpRequest.withContent(request.getContent());

        RxClient.ServerInfo serverInfo = new RxClient.ServerInfo(uri.getHost(), uri.getPort());

        InputStream responseStream = httpClient.submit(serverInfo, httpRequest).flatMap(clientResponse -> {
            return toInputStream(clientResponse.getContent());
        })
        .toBlocking().single();
        String createdContainerAsString = IOUtils.readLines(responseStream, "UTF-8").get(0);
        assertThat(createdContainerAsString).contains("\"id\":\"" + NON_PARTITIONED_CONTAINER_ID + "\"");
        
        // Create a document in the non partitioned collection using the rest API and older version
        resourceId = Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.getId() + "/" + Paths.COLLECTIONS_PATH_SEGMENT + "/" + collection.getId();
        path = Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.getId() + "/" + Paths.COLLECTIONS_PATH_SEGMENT 
                + "/" + collection.getId() + "/" + Paths.DOCUMENTS_PATH_SEGMENT + "/";
        Document document = new Document();
        document.setId(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID);

        authorization = base.generateKeyAuthorizationSignature(HttpConstants.HttpMethods.POST, resourceId, Paths.DOCUMENTS_PATH_SEGMENT, headers);
        headers.put(HttpConstants.HttpHeaders.AUTHORIZATION, URLEncoder.encode(authorization, "UTF-8"));
        request = RxDocumentServiceRequest.create(OperationType.Create, ResourceType.Document, path,
                document, headers, new RequestOptions());

        resourceUri = baseUrlSplit[0] + ":" + baseUrlSplit[1] + ":" + baseUrlSplit[2].split("/")[0] + "//" + Paths.DATABASES_PATH_SEGMENT + "/"
                + createdDatabase.getId() + "/" + Paths.COLLECTIONS_PATH_SEGMENT + "/" + collection.getId() + "/" + Paths.DOCUMENTS_PATH_SEGMENT + "/";
        uri = new URI(resourceUri);

        httpRequest = HttpClientRequest.create(HttpMethod.POST, uri.toString());

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpRequest.withHeader(entry.getKey(), entry.getValue());
        }

        httpRequest.withContent(request.getContent());

        serverInfo = new RxClient.ServerInfo(uri.getHost(), uri.getPort());

        responseStream = httpClient.submit(serverInfo, httpRequest).flatMap(clientResponse -> {
            return toInputStream(clientResponse.getContent());
        }).toBlocking().single();
        String createdItemAsString = IOUtils.readLines(responseStream, "UTF-8").get(0);
        assertThat(createdItemAsString).contains("\"id\":\"" + NON_PARTITIONED_CONTAINER_DOCUEMNT_ID + "\"");
    }

    @Test(groups = { "simple" }, timeOut = 10 * TIMEOUT)
    public void testNonPartitionedCollectionOperations() throws Exception {
        createContainerWithoutPk();
        CosmosContainer createdContainer = createdDatabase.getContainer(NON_PARTITIONED_CONTAINER_ID);

        Mono<CosmosItemResponse> readMono = createdContainer.getItem(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID, PartitionKey.None).read();
        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID).build();
        validateSuccess(readMono, validator);

        String createdItemId = UUID.randomUUID().toString();
        Mono<CosmosItemResponse> createMono = createdContainer.createItem(new CosmosItemSettings("{'id':'" + createdItemId + "'}"));
        validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(createdItemId).build();
        validateSuccess(createMono, validator);

        readMono = createdContainer.getItem(createdItemId, PartitionKey.None).read();
        validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(createdItemId).build();
        validateSuccess(readMono, validator);

        CosmosItem itemToReplace = createdContainer.getItem(createdItemId, PartitionKey.None).read().block().getCosmosItem();
        CosmosItemSettings itemSettingsToReplace = itemToReplace.read().block().getCosmosItemSettings();
        String replacedItemId = UUID.randomUUID().toString();
        itemSettingsToReplace.setId(replacedItemId);
        Mono<CosmosItemResponse> replaceMono = itemToReplace.replace(itemSettingsToReplace);
        validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(replacedItemId).build();
        validateSuccess(replaceMono, validator);

        String upsertedItemId = UUID.randomUUID().toString();

        Mono<CosmosItemResponse> upsertMono = createdContainer.upsertItem(new CosmosItemSettings("{'id':'" + upsertedItemId + "'}"));
        validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(upsertedItemId).build();
        validateSuccess(upsertMono, validator);

        // one document was created during setup, one with create (which was replaced) and one with upsert
        FeedOptions feedOptions = new FeedOptions();
        feedOptions.setPartitionKey(PartitionKey.None);
        ArrayList<String> expectedIds = new ArrayList<String>();
        expectedIds.add(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID);
        expectedIds.add(replacedItemId);
        expectedIds.add(upsertedItemId);
        Flux<FeedResponse<CosmosItemSettings>> queryFlux = createdContainer.queryItems("SELECT * from c", feedOptions);
        FeedResponseListValidator<CosmosItemSettings> queryValidator = new FeedResponseListValidator.Builder<CosmosItemSettings>()
                .totalSize(3)
                .numberOfPages(1)
                .containsExactlyIds(expectedIds)
                .build();
        validateQuerySuccess(queryFlux, queryValidator);

        queryFlux = createdContainer.listItems(feedOptions);
        queryValidator = new FeedResponseListValidator.Builder<CosmosItemSettings>()
                .totalSize(3)
                .numberOfPages(1)
                .containsExactlyIds(expectedIds)
                .build();
        validateQuerySuccess(queryFlux, queryValidator);

        String documentCreatedBySprocId = "testDoc";
        CosmosStoredProcedureSettings sproc = new CosmosStoredProcedureSettings(
                "{" +
                        "  'id': '" +UUID.randomUUID().toString() + "'," +
                        "  'body':'" +
                        "   function() {" +
                        "   var client = getContext().getCollection();" +
                        "   var doc = client.createDocument(client.getSelfLink(), { \\'id\\': \\'" + documentCreatedBySprocId + "\\'}, {}, function(err, docCreated, options) { " +
                        "   if(err) throw new Error(\\'Error while creating document: \\' + err.message);" +
                        "   else {" +
                        "   getContext().getResponse().setBody(1);" +
                        "      }" +
                        "    });" +
                        "}'" +
                "}");
        CosmosStoredProcedure createdSproc = createdContainer.createStoredProcedure(sproc).block().getStoredProcedure();

        // Partiton Key value same as what is specified in the stored procedure body
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(PartitionKey.None);
        int result = Integer.parseInt(createdSproc.execute(null, options).block().getResponseAsString());
        assertThat(result).isEqualTo(1);

        // 3 previous items + 1 created from the sproc
        expectedIds.add(documentCreatedBySprocId);
        queryFlux = createdContainer.listItems(feedOptions);
        queryValidator = new FeedResponseListValidator.Builder<CosmosItemSettings>()
                .totalSize(4)
                .numberOfPages(1)
                .containsExactlyIds(expectedIds)
                .build();
        validateQuerySuccess(queryFlux, queryValidator);

        Mono<CosmosItemResponse> deleteMono = createdContainer.getItem(upsertedItemId, PartitionKey.None).delete();
        validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .nullResource().build();
        validateSuccess(deleteMono, validator);

        deleteMono = createdContainer.getItem(replacedItemId, PartitionKey.None).delete();
        validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .nullResource().build();
        validateSuccess(deleteMono, validator);

        deleteMono = createdContainer.getItem(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID, PartitionKey.None).delete();
        validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .nullResource().build();
        validateSuccess(deleteMono, validator);

        deleteMono = createdContainer.getItem(documentCreatedBySprocId, PartitionKey.None).delete();
        validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .nullResource().build();
        validateSuccess(deleteMono, validator);

        queryFlux = createdContainer.listItems(feedOptions);
        queryValidator = new FeedResponseListValidator.Builder<CosmosItemSettings>()
                .totalSize(0)
                .numberOfPages(1)
                .build();
        validateQuerySuccess(queryFlux, queryValidator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT*100)
    public void testMultiPartitionCollectionReadDocumentWithNoPk() throws InterruptedException {
        String partitionedCollectionId = "PartitionedCollection" + UUID.randomUUID().toString();
        String IdOfDocumentWithNoPk = UUID.randomUUID().toString();
        CosmosContainerSettings containerSettings = new CosmosContainerSettings(partitionedCollectionId, "/mypk");
        CosmosContainer createdContainer = createdDatabase.createContainer(containerSettings).block().getContainer();
        CosmosItemSettings cosmosItemSettings = new CosmosItemSettings();
        cosmosItemSettings.setId(IdOfDocumentWithNoPk);
        CosmosItem createdItem = createdContainer.createItem(cosmosItemSettings).block().getCosmosItem();
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setPartitionKey(PartitionKey.None);
        Mono<CosmosItemResponse> readMono = createdItem.read(options);
        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(IdOfDocumentWithNoPk).build();
        validateSuccess(readMono, validator);
    }

    private Observable<InputStream> toInputStream(Observable<ByteBuf> contentObservable) {
        return contentObservable.reduce(new ByteArrayOutputStream(), (out, bb) -> {
            try {
                bb.readBytes(out, bb.readableBytes());
                return out;
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }).map(out -> {
            return new ByteArrayInputStream(out.toByteArray());
        });
    }

}
