package com.microsoft.azure.documentdb.rx.internal;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.Error;
import com.microsoft.azure.documentdb.ResourceResponse;
import com.microsoft.azure.documentdb.internal.DocumentServiceResponse;
import com.microsoft.azure.documentdb.internal.HttpConstants;
import com.microsoft.azure.documentdb.rx.AsyncDocumentClient;
import com.microsoft.azure.documentdb.rx.FailureValidator;
import com.microsoft.azure.documentdb.rx.ResourceResponseValidator;
import com.microsoft.azure.documentdb.rx.TestConfigurations;
import com.microsoft.azure.documentdb.rx.TestSuiteBase;
import com.microsoft.azure.documentdb.rx.Utils;

import rx.Observable;

public class RetryCreateDocumentTest extends TestSuiteBase {
    private final static String DATABASE_ID = getDatabaseId(RetryCreateDocumentTest.class);

    private final static int TIMEOUT = 7000;
    {
        subscriberValidationTimeout = TIMEOUT;
    }
    
    private AsyncDocumentClient client;

    private Database database;
    private DocumentCollection collection;
    private RxGatewayStoreModel gateway;
    private RxGatewayStoreModel spyGateway;

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void retryDocumentCreate() throws Exception {
        // create a document to ensure collection is cached
        client.createDocument(collection.getSelfLink(),  getDocumentDefinition(), null, false).toBlocking().single();

        Document docDefinition = getDocumentDefinition();

        Observable<ResourceResponse<Document>> createObservable = client
                .createDocument(collection.getSelfLink(), docDefinition, null, false);
        AtomicInteger count = new AtomicInteger();

        doAnswer(new Answer< Observable<DocumentServiceResponse>>() {
            @Override
            public Observable<DocumentServiceResponse> answer(InvocationOnMock invocation) throws Throwable {
                RxDocumentServiceRequest req = (RxDocumentServiceRequest) invocation.getArguments()[0];
                int currentAttempt = count.getAndIncrement();
                if (currentAttempt == 0) {
                    Map<String, String> header = ImmutableMap.of(
                            HttpConstants.HttpHeaders.SUB_STATUS,
                            Integer.toString(HttpConstants.SubStatusCodes.PARTITION_KEY_MISMATCH));          

                    return Observable.error(new DocumentClientException(HttpConstants.StatusCodes.BADREQUEST, new Error() , header));
                } else {
                    return gateway.doCreate(req);
                }
            }
        }).when(this.spyGateway).doCreate(anyObject());

        // validate
        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withId(docDefinition.getId()).build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocument_noRetryOnNonRetriableFailure() throws Exception {
        // create a document to ensure collection is cached
        client.createDocument(collection.getSelfLink(),  getDocumentDefinition(), null, false).toBlocking().single();

        Document docDefinition = getDocumentDefinition();

        Observable<ResourceResponse<Document>> createObservable = client
                .createDocument(collection.getSelfLink(), docDefinition, null, false);
        AtomicInteger count = new AtomicInteger();

        doAnswer(new Answer< Observable<DocumentServiceResponse>>() {
            @Override
            public Observable<DocumentServiceResponse> answer(InvocationOnMock invocation) throws Throwable {
                RxDocumentServiceRequest req = (RxDocumentServiceRequest) invocation.getArguments()[0];
                int currentAttempt = count.getAndIncrement();
                if (currentAttempt == 0) {
                    Map<String, String> header = ImmutableMap.of(
                            HttpConstants.HttpHeaders.SUB_STATUS,
                            Integer.toString(2));          

                    return Observable.error(new DocumentClientException(1, new Error() , header));
                } else {
                    return gateway.doCreate(req);
                }
            }
        }).when(this.spyGateway).doCreate(anyObject());

        // validate

        FailureValidator validator = new FailureValidator.Builder().statusCode(1).subStatusCode(2).build();
        validateFailure(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocument_failImmediatelyOnNonRetriable() throws Exception {
        // create a document to ensure collection is cached
        client.createDocument(collection.getSelfLink(),  getDocumentDefinition(), null, false).toBlocking().single();

        Document docDefinition = getDocumentDefinition();

        Observable<ResourceResponse<Document>> createObservable = client
                .createDocument(collection.getSelfLink(), docDefinition, null, false);
        AtomicInteger count = new AtomicInteger();

        doAnswer(new Answer< Observable<DocumentServiceResponse>>() {
            @Override
            public Observable<DocumentServiceResponse> answer(InvocationOnMock invocation) throws Throwable {
                RxDocumentServiceRequest req = (RxDocumentServiceRequest) invocation.getArguments()[0];
                int currentAttempt = count.getAndIncrement();
                if (currentAttempt == 0) {
                    Map<String, String> header = ImmutableMap.of(
                            HttpConstants.HttpHeaders.SUB_STATUS,
                            Integer.toString(2));          

                    return Observable.error(new DocumentClientException(1, new Error() , header));
                } else {
                    return gateway.doCreate(req);
                }
            }
        }).when(this.spyGateway).doCreate(anyObject());

        // validate

        FailureValidator validator = new FailureValidator.Builder().statusCode(1).subStatusCode(2).build();
        validateFailure(createObservable.timeout(100, TimeUnit.MILLISECONDS), validator);
    }

    private void registerSpyProxy() {

        RxDocumentClientImpl clientImpl = (RxDocumentClientImpl) client;
        try {
            Field f = RxDocumentClientImpl.class.getDeclaredField("gatewayProxy");
            f.setAccessible(true);
            this.gateway = (RxGatewayStoreModel) f.get(clientImpl);
            this.spyGateway = Mockito.spy(gateway);
            f.set(clientImpl, this.spyGateway);
        } catch (Exception e) {
            fail("failed to register spy proxy due to " + e.getMessage());
        }
    }

    @BeforeMethod
    public void beforeMethod() {
        Mockito.reset(this.spyGateway);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        // set up the client        
        client = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKey(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(ConnectionPolicy.GetDefault())
                .withConsistencyLevel(ConsistencyLevel.Session)
                .build();
        registerSpyProxy();

        Database databaseDefinition = new Database();
        databaseDefinition.setId(DATABASE_ID);

        try {
            client.deleteDatabase(Utils.getDatabaseLink(databaseDefinition, true), null).toBlocking().single();
        } catch (Exception e) {
            // ignore failure if it doesn't exist
        }

        database = client.createDatabase(databaseDefinition, null).toBlocking().single().getResource();
        collection = client.createCollection(database.getSelfLink(), getCollectionDefinition(), null).toBlocking().single().getResource();
    }

    private Document getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , uuid, uuid));
        return doc;
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT)
    public void afterClass() {
        client.deleteDatabase(database.getSelfLink(), null).toBlocking().single();
        client.close();
    }
}
