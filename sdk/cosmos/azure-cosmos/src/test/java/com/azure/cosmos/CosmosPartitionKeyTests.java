// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.BaseAuthorizationTokenProvider;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.rx.TestSuiteBase;
import io.netty.handler.codec.http.HttpMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
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

    private CosmosAsyncClient client;
    private CosmosAsyncDatabase createdDatabase;

    @Factory(dataProvider = "clientBuilders")
    public CosmosPartitionKeyTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_CosmosPartitionKeyTests() throws URISyntaxException, IOException {
        assertThat(this.client).isNull();
        client = clientBuilder().buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(client);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    private void createContainerWithoutPk() throws URISyntaxException, IOException {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        HttpClientConfig httpClientConfig = new HttpClientConfig(new Configs())
                .withMaxIdleConnectionTimeoutInMillis(connectionPolicy.getIdleConnectionTimeoutInMillis())
                .withPoolSize(connectionPolicy.getMaxPoolSize())
                .withHttpProxy(connectionPolicy.getProxy())
                .withRequestTimeoutInMillis(connectionPolicy.getRequestTimeoutInMillis());

        HttpClient httpClient = HttpClient.createFixed(httpClientConfig);

        // CREATE a non partitioned collection using the rest API and older getVersion
        String resourceId = Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.getId();
        String path = Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.getId() + "/" + Paths.COLLECTIONS_PATH_SEGMENT + "/";
        DocumentCollection collection = new DocumentCollection();
        collection.setId(NON_PARTITIONED_CONTAINER_ID);

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
            0] + "//" + Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.getId() + "/" + Paths.COLLECTIONS_PATH_SEGMENT + "/";
        URI uri = new URI(resourceUri);

        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, uri, uri.getPort(), new HttpHeaders(headers));
        httpRequest.withBody(request.getContent());
        String body = httpClient.send(httpRequest).block().bodyAsString().block();
        assertThat(body).contains("\"id\":\"" + NON_PARTITIONED_CONTAINER_ID + "\"");

        // CREATE a document in the non partitioned collection using the rest API and older getVersion
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

        httpRequest = new HttpRequest(HttpMethod.POST, uri, uri.getPort(), new HttpHeaders(headers));
        httpRequest.withBody(request.getContent());

        body = httpClient.send(httpRequest).block().bodyAsString().block();
        assertThat(body).contains("\"id\":\"" + NON_PARTITIONED_CONTAINER_DOCUEMNT_ID + "\"");
    }

    @Test(groups = { "emulator" })
    public void nonPartitionedCollectionOperations() throws Exception {
        createContainerWithoutPk();
        CosmosAsyncContainer createdContainer = createdDatabase.getContainer(NON_PARTITIONED_CONTAINER_ID);

        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> readMono = createdContainer.readItem(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID,
                                                                    PartitionKey.NONE, CosmosItemProperties.class);
        CosmosResponseValidator<CosmosAsyncItemResponse<CosmosItemProperties>> validator =
            new CosmosResponseValidator.Builder<CosmosAsyncItemResponse<CosmosItemProperties>>()
                .withId(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID).build();
        validateSuccess(readMono, validator);

        String createdItemId = UUID.randomUUID().toString();
        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> createMono = 
            createdContainer.createItem(new CosmosItemProperties("{'id':'" + createdItemId + "'}"));
        validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse<CosmosItemProperties>>()
                .withId(createdItemId).build();
        validateSuccess(createMono, validator);

        readMono = createdContainer.readItem(createdItemId, PartitionKey.NONE, CosmosItemProperties.class);
        validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse<CosmosItemProperties>>()
                .withId(createdItemId).build();
        validateSuccess(readMono, validator);
        
        CosmosItemProperties itemSettingsToReplace = createdContainer.readItem(createdItemId, PartitionKey.NONE,
                                                                               CosmosItemProperties.class)
                                                         .block()
                                                         .getProperties();
        String replacedItemId = UUID.randomUUID().toString();
        itemSettingsToReplace.setId(replacedItemId);
        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> replaceMono = createdContainer.replaceItem(itemSettingsToReplace,
                                                                                 createdItemId,
                                                                                 PartitionKey.NONE);
        validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse<CosmosItemProperties>>()
                .withId(replacedItemId).build();
        validateSuccess(replaceMono, validator);

        String upsertedItemId = UUID.randomUUID().toString();

        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> upsertMono = createdContainer.upsertItem(new CosmosItemProperties("{'id':'" + upsertedItemId + "'}"));
        validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse<CosmosItemProperties>>()
                .withId(upsertedItemId).build();
        validateSuccess(upsertMono, validator);

        // one document was created during setup, one with create (which was replaced) and one with upsert
        FeedOptions feedOptions = new FeedOptions();
        feedOptions.partitionKey(PartitionKey.NONE);
        ArrayList<String> expectedIds = new ArrayList<String>();
        expectedIds.add(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID);
        expectedIds.add(replacedItemId);
        expectedIds.add(upsertedItemId);
        Flux<FeedResponse<CosmosItemProperties>> queryFlux = createdContainer.queryItems("SELECT * from c", feedOptions, CosmosItemProperties.class);
        FeedResponseListValidator<CosmosItemProperties> queryValidator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
                .totalSize(3)
                .numberOfPages(1)
                .containsExactlyIds(expectedIds)
                .build();
        validateQuerySuccess(queryFlux, queryValidator);

        queryFlux = createdContainer.readAllItems(feedOptions, CosmosItemProperties.class);
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
        CosmosAsyncStoredProcedure createdSproc = createdContainer.getScripts().createStoredProcedure(sproc).block().getStoredProcedure();

        // Partiton Key value same as what is specified in the stored procedure body
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(PartitionKey.NONE);
        CosmosStoredProcedureRequestOptions cosmosStoredProcedureRequestOptions = new CosmosStoredProcedureRequestOptions();
        cosmosStoredProcedureRequestOptions.setPartitionKey(PartitionKey.NONE);
        int result = Integer.parseInt(createdSproc.execute(null, cosmosStoredProcedureRequestOptions).block().getResponseAsString());
        assertThat(result).isEqualTo(1);

        // 3 previous items + 1 created from the sproc
        expectedIds.add(documentCreatedBySprocId);
        queryFlux = createdContainer.readAllItems(feedOptions, CosmosItemProperties.class);
        queryValidator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
                .totalSize(4)
                .numberOfPages(1)
                .containsExactlyIds(expectedIds)
                .build();
        validateQuerySuccess(queryFlux, queryValidator);
        CosmosResponseValidator<CosmosAsyncItemResponse> deleteResponseValidator;
        Mono<CosmosAsyncItemResponse> deleteMono =
            createdContainer.deleteItem(upsertedItemId, PartitionKey.NONE);
        deleteResponseValidator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
                .nullResource().build();
        validateSuccess(deleteMono, deleteResponseValidator);

        deleteMono = createdContainer.deleteItem(replacedItemId, PartitionKey.NONE);
        deleteResponseValidator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
                .nullResource().build();
        validateSuccess(deleteMono, deleteResponseValidator);

        deleteMono = createdContainer.deleteItem(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID, PartitionKey.NONE);
        deleteResponseValidator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
                .nullResource().build();
        validateSuccess(deleteMono, deleteResponseValidator);

        deleteMono = createdContainer.deleteItem(documentCreatedBySprocId, PartitionKey.NONE);
        deleteResponseValidator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
                .nullResource().build();
        validateSuccess(deleteMono, deleteResponseValidator);

        queryFlux = createdContainer.readAllItems(feedOptions, CosmosItemProperties.class);
        queryValidator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
                .totalSize(0)
                .numberOfPages(1)
                .build();
        validateQuerySuccess(queryFlux, queryValidator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT*100)
    public void multiPartitionCollectionReadDocumentWithNoPk() throws InterruptedException {
        String partitionedCollectionId = "PartitionedCollection" + UUID.randomUUID().toString();
        String IdOfDocumentWithNoPk = UUID.randomUUID().toString();
        CosmosContainerProperties containerSettings = new CosmosContainerProperties(partitionedCollectionId, "/mypk");
        CosmosAsyncContainer createdContainer = createdDatabase.createContainer(containerSettings).block().getContainer();
        CosmosItemProperties cosmosItemProperties = new CosmosItemProperties();
        cosmosItemProperties.setId(IdOfDocumentWithNoPk);
        createdContainer.createItem(cosmosItemProperties).block();
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setPartitionKey(PartitionKey.NONE);
        Mono<CosmosAsyncItemResponse<CosmosItemProperties>> readMono = createdContainer.readItem(cosmosItemProperties.getId(),
                                                                           PartitionKey.NONE, options,
                                                                           CosmosItemProperties.class);
        CosmosResponseValidator<CosmosAsyncItemResponse<CosmosItemProperties>> validator =
            new CosmosResponseValidator.Builder<CosmosAsyncItemResponse<CosmosItemProperties>>()
                .withId(IdOfDocumentWithNoPk).build();
        validateSuccess(readMono, validator);
    }

}
