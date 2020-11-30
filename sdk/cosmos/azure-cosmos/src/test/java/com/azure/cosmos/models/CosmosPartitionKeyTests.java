// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncStoredProcedure;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.BaseAuthorizationTokenProvider;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.RequestVerb;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.rx.CosmosItemResponseValidator;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedFlux;
import io.netty.handler.codec.http.HttpMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
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
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(client);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    private void createContainerWithoutPk() throws URISyntaxException, IOException {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        HttpClientConfig httpClientConfig = new HttpClientConfig(new Configs())
                .withMaxIdleConnectionTimeout(connectionPolicy.getIdleHttpConnectionTimeout())
                .withPoolSize(connectionPolicy.getMaxConnectionPoolSize())
                .withProxy(connectionPolicy.getProxy())
                .withRequestTimeout(connectionPolicy.getRequestTimeout());

        HttpClient httpClient = HttpClient.createFixed(httpClientConfig);

        // CREATE a non partitioned collection using the rest API and older getVersion
        String resourceId = Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.getId();
        String path = Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.getId() + "/" + Paths.COLLECTIONS_PATH_SEGMENT + "/";
        DocumentCollection collection = new DocumentCollection();
        collection.setId(NON_PARTITIONED_CONTAINER_ID);

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(HttpConstants.HttpHeaders.X_DATE, Utils.nowAsRFC1123());
        headers.put(HttpConstants.HttpHeaders.VERSION, "2018-09-17");
        BaseAuthorizationTokenProvider base = new BaseAuthorizationTokenProvider(new AzureKeyCredential(TestConfigurations.MASTER_KEY));
        String authorization = base.generateKeyAuthorizationSignature(RequestVerb.POST, resourceId, Paths.COLLECTIONS_PATH_SEGMENT, headers);
        headers.put(HttpConstants.HttpHeaders.AUTHORIZATION, URLEncoder.encode(authorization, "UTF-8"));
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create,
                ResourceType.DocumentCollection, path, collection, headers, new RequestOptions());

        String[] baseUrlSplit = TestConfigurations.HOST.split(":");
        String resourceUri = baseUrlSplit[0] + ":" + baseUrlSplit[1] + ":" + baseUrlSplit[2].split("/")[
            0] + "//" + Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.getId() + "/" + Paths.COLLECTIONS_PATH_SEGMENT + "/";
        URI uri = new URI(resourceUri);

        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, uri, uri.getPort(), new HttpHeaders(headers));
        httpRequest.withBody(request.getContentAsByteArrayFlux());
        String body = httpClient.send(httpRequest).block().bodyAsString().block();
        assertThat(body).contains("\"id\":\"" + NON_PARTITIONED_CONTAINER_ID + "\"");

        // CREATE a document in the non partitioned collection using the rest API and older getVersion
        resourceId = Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.getId() + "/" + Paths.COLLECTIONS_PATH_SEGMENT + "/" + collection.getId();
        path = Paths.DATABASES_PATH_SEGMENT + "/" + createdDatabase.getId() + "/" + Paths.COLLECTIONS_PATH_SEGMENT
                + "/" + collection.getId() + "/" + Paths.DOCUMENTS_PATH_SEGMENT + "/";
        Document document = new Document();
        document.setId(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID);

        authorization = base.generateKeyAuthorizationSignature(RequestVerb.POST, resourceId, Paths.DOCUMENTS_PATH_SEGMENT, headers);
        headers.put(HttpConstants.HttpHeaders.AUTHORIZATION, URLEncoder.encode(authorization, "UTF-8"));
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Document, path,
                document, headers, new RequestOptions());

        resourceUri = baseUrlSplit[0] + ":" + baseUrlSplit[1] + ":" + baseUrlSplit[2].split("/")[0] + "//" + Paths.DATABASES_PATH_SEGMENT + "/"
                + createdDatabase.getId() + "/" + Paths.COLLECTIONS_PATH_SEGMENT + "/" + collection.getId() + "/" + Paths.DOCUMENTS_PATH_SEGMENT + "/";
        uri = new URI(resourceUri);

        httpRequest = new HttpRequest(HttpMethod.POST, uri, uri.getPort(), new HttpHeaders(headers));
        httpRequest.withBody(request.getContentAsByteArrayFlux());

        body = httpClient.send(httpRequest).block().bodyAsString().block();
        assertThat(body).contains("\"id\":\"" + NON_PARTITIONED_CONTAINER_DOCUEMNT_ID + "\"");
    }

    @Test(groups = { "emulator" })
    public void nonPartitionedCollectionOperations() throws Exception {
        createContainerWithoutPk();
        CosmosAsyncContainer createdContainer = createdDatabase.getContainer(NON_PARTITIONED_CONTAINER_ID);

        Mono<CosmosItemResponse<InternalObjectNode>> readMono = createdContainer.readItem(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID,
                                                                                                 PartitionKey.NONE, InternalObjectNode.class);

        CosmosItemResponseValidator validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .withId(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID)
                .build();
        this.validateItemSuccess(readMono, validator);

        String createdItemId = UUID.randomUUID().toString();
        Mono<CosmosItemResponse<InternalObjectNode>> createMono =
            createdContainer.createItem(new InternalObjectNode("{'id':'" + createdItemId + "'}"));
        validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .withId(createdItemId)
                .build();
        this.validateItemSuccess(createMono, validator);

        readMono = createdContainer.readItem(createdItemId, PartitionKey.NONE, InternalObjectNode.class);
        validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .withId(createdItemId)
                .build();
        this.validateItemSuccess(readMono, validator);

        CosmosItemResponse<InternalObjectNode> response = createdContainer.readItem(createdItemId, PartitionKey.NONE,
                                                                               InternalObjectNode.class)
                                                                          .block();
        InternalObjectNode itemSettingsToReplace = ModelBridgeInternal.getInternalObjectNode(response);
        String replacedItemId = UUID.randomUUID().toString();
        itemSettingsToReplace.setId(replacedItemId);
        Mono<CosmosItemResponse<InternalObjectNode>> replaceMono = createdContainer.replaceItem(itemSettingsToReplace,
                                                                                 createdItemId,
                                                                                 PartitionKey.NONE);
        validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .withId(replacedItemId)
                .build();
        this.validateItemSuccess(replaceMono, validator);

        String upsertedItemId = UUID.randomUUID().toString();

        Mono<CosmosItemResponse<InternalObjectNode>> upsertMono = createdContainer.upsertItem(new InternalObjectNode("{'id':'" + upsertedItemId + "'}"));

        validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .withId(upsertedItemId)
                .build();
        this.validateItemSuccess(upsertMono, validator);

        // one document was created during setup, one with create (which was replaced) and one with upsert
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        cosmosQueryRequestOptions.setPartitionKey(PartitionKey.NONE);
        ArrayList<String> expectedIds = new ArrayList<String>();
        expectedIds.add(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID);
        expectedIds.add(replacedItemId);
        expectedIds.add(upsertedItemId);
        CosmosPagedFlux<InternalObjectNode> queryFlux = createdContainer.queryItems("SELECT * from c", cosmosQueryRequestOptions, InternalObjectNode.class);
        FeedResponseListValidator<InternalObjectNode> queryValidator = new FeedResponseListValidator.Builder<InternalObjectNode>()
                .totalSize(3)
                .numberOfPages(1)
                .containsExactlyIds(expectedIds)
                .build();
        validateQuerySuccess(queryFlux.byPage(), queryValidator);

        queryFlux = createdContainer.queryItems("SELECT * FROM r", cosmosQueryRequestOptions, InternalObjectNode.class);
        queryValidator = new FeedResponseListValidator.Builder<InternalObjectNode>()
                .totalSize(3)
                .numberOfPages(1)
                .containsExactlyIds(expectedIds)
                .build();
        validateQuerySuccess(queryFlux.byPage(), queryValidator);

        String documentCreatedBySprocId = "testDoc";
        CosmosStoredProcedureProperties sproc = ModelBridgeInternal.createCosmosStoredProcedureProperties(
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
        CosmosStoredProcedureResponse createdSproc = createdContainer.getScripts().createStoredProcedure(sproc).block();
        CosmosAsyncStoredProcedure storedProcedure =
            createdContainer.getScripts().getStoredProcedure(createdSproc.getProperties().getId());

        // Partiton Key value same as what is specified in the stored procedure body
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(PartitionKey.NONE);
        CosmosStoredProcedureRequestOptions cosmosStoredProcedureRequestOptions = new CosmosStoredProcedureRequestOptions();
        cosmosStoredProcedureRequestOptions.setPartitionKey(PartitionKey.NONE);
        int result = Integer.parseInt(storedProcedure.execute(null, cosmosStoredProcedureRequestOptions).block().getResponseAsString());
        assertThat(result).isEqualTo(1);

        // 3 previous items + 1 created from the sproc
        expectedIds.add(documentCreatedBySprocId);
        queryFlux = createdContainer.queryItems("SELECT * FROM r", cosmosQueryRequestOptions, InternalObjectNode.class);
        queryValidator = new FeedResponseListValidator.Builder<InternalObjectNode>()
                .totalSize(4)
                .numberOfPages(1)
                .containsExactlyIds(expectedIds)
                .build();
        validateQuerySuccess(queryFlux.byPage(), queryValidator);
        CosmosItemResponseValidator deleteResponseValidator;
        Mono<CosmosItemResponse<Object>> deleteMono =
            createdContainer.deleteItem(upsertedItemId, PartitionKey.NONE);
        deleteResponseValidator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .nullResource()
                .build();
        this.validateItemSuccess(deleteMono, deleteResponseValidator);


        deleteMono = createdContainer.deleteItem(replacedItemId, PartitionKey.NONE);
        deleteResponseValidator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .nullResource()
                .build();
        this.validateItemSuccess(deleteMono, deleteResponseValidator);

        deleteMono = createdContainer.deleteItem(NON_PARTITIONED_CONTAINER_DOCUEMNT_ID, PartitionKey.NONE);
        deleteResponseValidator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .nullResource()
                .build();
        this.validateItemSuccess(deleteMono, deleteResponseValidator);

        deleteMono = createdContainer.deleteItem(documentCreatedBySprocId, PartitionKey.NONE);
        deleteResponseValidator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .nullResource()
                .build();
        this.validateItemSuccess(deleteMono, deleteResponseValidator);

        queryFlux = createdContainer.queryItems("SELECT * FROM r", cosmosQueryRequestOptions, InternalObjectNode.class);
        queryValidator = new FeedResponseListValidator.Builder<InternalObjectNode>()
                .totalSize(0)
                .numberOfPages(1)
                .build();
        validateQuerySuccess(queryFlux.byPage(), queryValidator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT*100)
    public void multiPartitionCollectionReadDocumentWithNoPk() throws InterruptedException {
        String partitionedCollectionId = "PartitionedCollection" + UUID.randomUUID().toString();
        String IdOfDocumentWithNoPk = UUID.randomUUID().toString();
        CosmosContainerProperties containerSettings = new CosmosContainerProperties(partitionedCollectionId, "/mypk");
        createdDatabase.createContainer(containerSettings).block();
        CosmosAsyncContainer createdContainer = createdDatabase.getContainer(containerSettings.getId());
        InternalObjectNode internalObjectNode = new InternalObjectNode();
        internalObjectNode.setId(IdOfDocumentWithNoPk);
        createdContainer.createItem(internalObjectNode).block();
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        ModelBridgeInternal.setPartitionKey(options, PartitionKey.NONE);
        Mono<CosmosItemResponse<InternalObjectNode>> readMono = createdContainer.readItem(internalObjectNode.getId(),
                                                                           PartitionKey.NONE, options,
                                                                           InternalObjectNode.class);

        CosmosItemResponseValidator validator =
            new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                .withId(IdOfDocumentWithNoPk)
                .build();
        this.validateItemSuccess(readMono, validator);
    }

}
