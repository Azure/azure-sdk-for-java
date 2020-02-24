// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.*;
import com.azure.data.cosmos.internal.http.HttpClient;
import com.azure.data.cosmos.internal.http.HttpClientConfig;
import com.azure.data.cosmos.internal.http.HttpHeaders;
import com.azure.data.cosmos.internal.http.HttpRequest;
import com.azure.data.cosmos.rx.TestSuiteBase;
import io.netty.handler.codec.http.HttpMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.annotations.Ignore;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public final class CosmosPartitionKeyTests extends TestSuiteBase {

    private final static String NON_PARTITIONED_CONTAINER_ID = "NonPartitionContainer" + UUID.randomUUID().toString();
    private final static String NON_PARTITIONED_CONTAINER_DOCUEMNT_ID = "NonPartitionContainer_Document" + UUID.randomUUID().toString();

    private CosmosClient client;
    private CosmosDatabase createdDatabase;

    @Factory(dataProvider = "clientBuilders")
    public CosmosPartitionKeyTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws URISyntaxException, IOException {
        assertThat(this.client).isNull();
        client = clientBuilder().build();
        createdDatabase = getSharedCosmosDatabase(client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    private void createContainerWithoutPk() throws URISyntaxException, IOException {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        HttpClientConfig httpClientConfig = new HttpClientConfig(new Configs())
                .withMaxIdleConnectionTimeoutInMillis(connectionPolicy.idleConnectionTimeoutInMillis())
                .withPoolSize(connectionPolicy.maxPoolSize())
                .withHttpProxy(connectionPolicy.proxy())
                .withRequestTimeoutInMillis(connectionPolicy.requestTimeoutInMillis());

        HttpClient httpClient = HttpClient.createFixed(httpClientConfig);

        // CREATE a non partitioned collection using the rest API and older version
        String resourceId = Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.id();
        String path = Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.id() + "/" + Paths.COLLECTIONS_PATH_SEGMENT + "/";
        DocumentCollection collection = new DocumentCollection();
        collection.id(NON_PARTITIONED_CONTAINER_ID);

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(HttpConstants.HttpHeaders.X_DATE, Utils.nowAsRFC1123());
        headers.put(HttpConstants.HttpHeaders.VERSION, "2018-09-17");
        BaseAuthorizationTokenProvider base = new BaseAuthorizationTokenProvider(new CosmosKeyCredential(TestConfigurations.MASTER_KEY));
        String authorization = base.generateKeyAuthorizationSignature(HttpConstants.HttpMethods.POST, resourceId, Paths.COLLECTIONS_PATH_SEGMENT, headers);
        headers.put(HttpConstants.HttpHeaders.AUTHORIZATION, URLEncoder.encode(authorization, "UTF-8"));
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Create,
                ResourceType.DocumentCollection, path, collection, headers, new RequestOptions());

        String[] baseUrlSplit = TestConfigurations.HOST.split(":");
        String resourceUri = baseUrlSplit[0] + ":" + baseUrlSplit[1] + ":" + baseUrlSplit[2].split("/")[
            0] + "//" + Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.id() + "/" + Paths.COLLECTIONS_PATH_SEGMENT + "/";
        URI uri = new URI(resourceUri);

        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, uri, uri.getPort(), new HttpHeaders(headers));
        httpRequest.withBody(request.getContent());
        String body = httpClient.send(httpRequest).block().bodyAsString().block();
        assertThat(body).contains("\"id\":\"" + NON_PARTITIONED_CONTAINER_ID + "\"");

        // CREATE a document in the non partitioned collection using the rest API and older version
        resourceId = Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.id() + "/" + Paths.COLLECTIONS_PATH_SEGMENT + "/" + collection.id();
        path = Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.id() + "/" + Paths.COLLECTIONS_PATH_SEGMENT
                + "/" + collection.id() + "/" + Paths.DOCUMENTS_PATH_SEGMENT + "/";
        Document document = new Document();
        document.id(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID);

        authorization = base.generateKeyAuthorizationSignature(HttpConstants.HttpMethods.POST, resourceId, Paths.DOCUMENTS_PATH_SEGMENT, headers);
        headers.put(HttpConstants.HttpHeaders.AUTHORIZATION, URLEncoder.encode(authorization, "UTF-8"));
        request = RxDocumentServiceRequest.create(OperationType.Create, ResourceType.Document, path,
                document, headers, new RequestOptions());

        resourceUri = baseUrlSplit[0] + ":" + baseUrlSplit[1] + ":" + baseUrlSplit[2].split("/")[0] + "//" + Paths.DATABASES_PATH_SEGMENT + "/"
                + createdDatabase.id() + "/" + Paths.COLLECTIONS_PATH_SEGMENT + "/" + collection.id() + "/" + Paths.DOCUMENTS_PATH_SEGMENT + "/";
        uri = new URI(resourceUri);

        httpRequest = new HttpRequest(HttpMethod.POST, uri, uri.getPort(), new HttpHeaders(headers));
        httpRequest.withBody(request.getContent());

        body = httpClient.send(httpRequest).block().bodyAsString().block();
        assertThat(body).contains("\"id\":\"" + NON_PARTITIONED_CONTAINER_DOCUEMNT_ID + "\"");
    }

    //FIXME test is flaky
    @Ignore
    @Test(groups = { "simple" })
    public void testNonPartitionedCollectionOperations() throws Exception {
        createContainerWithoutPk();
        CosmosContainer createdContainer = createdDatabase.getContainer(NON_PARTITIONED_CONTAINER_ID);

        Mono<CosmosItemResponse> readMono = createdContainer.getItem(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID, PartitionKey.None).read();
        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID).build();
        validateSuccess(readMono, validator);

        String createdItemId = UUID.randomUUID().toString();
        Mono<CosmosItemResponse> createMono = createdContainer.createItem(new CosmosItemProperties("{'id':'" + createdItemId + "'}"));
        validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(createdItemId).build();
        validateSuccess(createMono, validator);

        readMono = createdContainer.getItem(createdItemId, PartitionKey.None).read();
        validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(createdItemId).build();
        validateSuccess(readMono, validator);

        CosmosItem itemToReplace = createdContainer.getItem(createdItemId, PartitionKey.None).read().block().item();
        CosmosItemProperties itemSettingsToReplace = itemToReplace.read().block().properties();
        String replacedItemId = UUID.randomUUID().toString();
        itemSettingsToReplace.id(replacedItemId);
        Mono<CosmosItemResponse> replaceMono = itemToReplace.replace(itemSettingsToReplace);
        validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(replacedItemId).build();
        validateSuccess(replaceMono, validator);

        String upsertedItemId = UUID.randomUUID().toString();

        Mono<CosmosItemResponse> upsertMono = createdContainer.upsertItem(new CosmosItemProperties("{'id':'" + upsertedItemId + "'}"));
        validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(upsertedItemId).build();
        validateSuccess(upsertMono, validator);

        // one document was created during setup, one with create (which was replaced) and one with upsert
        FeedOptions feedOptions = new FeedOptions();
        feedOptions.partitionKey(PartitionKey.None);
        ArrayList<String> expectedIds = new ArrayList<String>();
        expectedIds.add(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID);
        expectedIds.add(replacedItemId);
        expectedIds.add(upsertedItemId);
        Flux<FeedResponse<CosmosItemProperties>> queryFlux = createdContainer.queryItems("SELECT * from c", feedOptions);
        FeedResponseListValidator<CosmosItemProperties> queryValidator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
                .totalSize(3)
                .numberOfPages(1)
                .containsExactlyIds(expectedIds)
                .build();
        validateQuerySuccess(queryFlux, queryValidator);

        queryFlux = createdContainer.readAllItems(feedOptions);
        queryValidator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
                .totalSize(3)
                .numberOfPages(1)
                .containsExactlyIds(expectedIds)
                .build();
        validateQuerySuccess(queryFlux, queryValidator);

        String documentCreatedBySprocId = "testDoc";
        CosmosStoredProcedureProperties sproc = new CosmosStoredProcedureProperties(
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
        CosmosStoredProcedure createdSproc = createdContainer.getScripts().createStoredProcedure(sproc).block().storedProcedure();

        // Partiton Key value same as what is specified in the stored procedure body
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(PartitionKey.None);
        int result = Integer.parseInt(createdSproc.execute(null, new CosmosStoredProcedureRequestOptions()).block().responseAsString());
        assertThat(result).isEqualTo(1);

        // 3 previous items + 1 created from the sproc
        expectedIds.add(documentCreatedBySprocId);
        queryFlux = createdContainer.readAllItems(feedOptions);
        queryValidator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
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

        queryFlux = createdContainer.readAllItems(feedOptions);
        queryValidator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
                .totalSize(0)
                .numberOfPages(1)
                .build();
        validateQuerySuccess(queryFlux, queryValidator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT*100)
    public void testMultiPartitionCollectionReadDocumentWithNoPk() throws InterruptedException {
        String partitionedCollectionId = "PartitionedCollection" + UUID.randomUUID().toString();
        String IdOfDocumentWithNoPk = UUID.randomUUID().toString();
        CosmosContainerProperties containerSettings = new CosmosContainerProperties(partitionedCollectionId, "/mypk");
        CosmosContainer createdContainer = createdDatabase.createContainer(containerSettings).block().container();
        CosmosItemProperties cosmosItemProperties = new CosmosItemProperties();
        cosmosItemProperties.id(IdOfDocumentWithNoPk);
        CosmosItem createdItem = createdContainer.createItem(cosmosItemProperties).block().item();
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.partitionKey(PartitionKey.None);
        Mono<CosmosItemResponse> readMono = createdItem.read(options);
        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(IdOfDocumentWithNoPk).build();
        validateSuccess(readMono, validator);
    }

}
