// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.internal.BaseAuthorizationTokenProvider;
import com.azure.cosmos.internal.Configs;
import com.azure.cosmos.internal.Document;
import com.azure.cosmos.internal.DocumentCollection;
import com.azure.cosmos.internal.FeedResponseListValidator;
import com.azure.cosmos.internal.HttpConstants;
import com.azure.cosmos.internal.OperationType;
import com.azure.cosmos.internal.Paths;
import com.azure.cosmos.internal.RequestOptions;
import com.azure.cosmos.internal.ResourceType;
import com.azure.cosmos.internal.RxDocumentServiceRequest;
import com.azure.cosmos.internal.TestConfigurations;
import com.azure.cosmos.internal.Utils;
import com.azure.cosmos.internal.http.HttpClient;
import com.azure.cosmos.internal.http.HttpClientConfig;
import com.azure.cosmos.internal.http.HttpHeaders;
import com.azure.cosmos.internal.http.HttpRequest;
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

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws URISyntaxException, IOException {
        assertThat(this.client).isNull();
        client = clientBuilder().buildAsyncClient();
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
        assertThat(body).contains("\"getId\":\"" + NON_PARTITIONED_CONTAINER_ID + "\"");

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

    //FIXME test is flaky
    @Ignore
    @Test(groups = { "simple" })
    public void testNonPartitionedCollectionOperations() throws Exception {
        createContainerWithoutPk();
        CosmosAsyncContainer createdContainer = createdDatabase.getContainer(NON_PARTITIONED_CONTAINER_ID);

        Mono<CosmosAsyncItemResponse> readMono = createdContainer.getItem(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID, PartitionKey.None).read();
        CosmosResponseValidator<CosmosAsyncItemResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
                .withId(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID).build();
        validateSuccess(readMono, validator);

        String createdItemId = UUID.randomUUID().toString();
        Mono<CosmosAsyncItemResponse> createMono = createdContainer.createItem(new CosmosItemProperties("{'id':'" + createdItemId + "'}"));
        validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
                .withId(createdItemId).build();
        validateSuccess(createMono, validator);

        readMono = createdContainer.getItem(createdItemId, PartitionKey.None).read();
        validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
                .withId(createdItemId).build();
        validateSuccess(readMono, validator);

        CosmosAsyncItem itemToReplace = createdContainer.getItem(createdItemId, PartitionKey.None).read().block().getItem();
        CosmosItemProperties itemSettingsToReplace = itemToReplace.read().block().getProperties();
        String replacedItemId = UUID.randomUUID().toString();
        itemSettingsToReplace.setId(replacedItemId);
        Mono<CosmosAsyncItemResponse> replaceMono = itemToReplace.replace(itemSettingsToReplace);
        validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
                .withId(replacedItemId).build();
        validateSuccess(replaceMono, validator);

        String upsertedItemId = UUID.randomUUID().toString();

        Mono<CosmosAsyncItemResponse> upsertMono = createdContainer.upsertItem(new CosmosItemProperties("{'id':'" + upsertedItemId + "'}"));
        validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
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
        CosmosAsyncStoredProcedure createdSproc = createdContainer.getScripts().createStoredProcedure(sproc).block().getStoredProcedure();

        // Partiton Key value same as what is specified in the stored procedure body
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(PartitionKey.None);
        int result = Integer.parseInt(createdSproc.execute(null, new CosmosStoredProcedureRequestOptions()).block().getResponseAsString());
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

        Mono<CosmosAsyncItemResponse> deleteMono = createdContainer.getItem(upsertedItemId, PartitionKey.None).delete();
        validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
                .nullResource().build();
        validateSuccess(deleteMono, validator);

        deleteMono = createdContainer.getItem(replacedItemId, PartitionKey.None).delete();
        validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
                .nullResource().build();
        validateSuccess(deleteMono, validator);

        deleteMono = createdContainer.getItem(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID, PartitionKey.None).delete();
        validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
                .nullResource().build();
        validateSuccess(deleteMono, validator);

        deleteMono = createdContainer.getItem(documentCreatedBySprocId, PartitionKey.None).delete();
        validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
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
        CosmosAsyncContainer createdContainer = createdDatabase.createContainer(containerSettings).block().getContainer();
        CosmosItemProperties cosmosItemProperties = new CosmosItemProperties();
        cosmosItemProperties.setId(IdOfDocumentWithNoPk);
        CosmosAsyncItem createdItem = createdContainer.createItem(cosmosItemProperties).block().getItem();
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setPartitionKey(PartitionKey.None);
        Mono<CosmosAsyncItemResponse> readMono = createdItem.read(options);
        CosmosResponseValidator<CosmosAsyncItemResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncItemResponse>()
                .withId(IdOfDocumentWithNoPk).build();
        validateSuccess(readMono, validator);
    }

}
