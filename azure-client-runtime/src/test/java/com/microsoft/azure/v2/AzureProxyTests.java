package com.microsoft.azure.v2;

import com.microsoft.azure.v2.http.MockAzureHttpClient;
import com.microsoft.rest.v2.protocol.SerializerAdapter;
import com.microsoft.rest.v2.serializer.JacksonAdapter;
import com.microsoft.rest.v2.InvalidReturnTypeException;
import com.microsoft.rest.v2.annotations.DELETE;
import com.microsoft.rest.v2.annotations.ExpectedResponses;
import com.microsoft.rest.v2.annotations.GET;
import com.microsoft.rest.v2.annotations.Host;
import com.microsoft.rest.v2.annotations.PUT;
import com.microsoft.rest.v2.annotations.PathParam;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.functions.Action1;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class AzureProxyTests {
    private long delayInMillisecondsBackup;

    @Before
    public void beforeTest() {
        delayInMillisecondsBackup = AzureProxy.defaultDelayInMilliseconds();
        AzureProxy.setDefaultDelayInMilliseconds(0);
    }

    @After
    public void afterTest() {
        AzureProxy.setDefaultDelayInMilliseconds(delayInMillisecondsBackup);
    }

    @Host("https://mock.azure.com")
    private interface MockResourceService {
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}")
        @ExpectedResponses({200})
        MockResource get(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}")
        @ExpectedResponses({200})
        Single<MockResource> getAsync(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}")
        @ExpectedResponses({200})
        MockResource create(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location")
        @ExpectedResponses({200})
        MockResource createWithLocation(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        MockResource createWithLocationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsRemaining);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Azure-AsyncOperation")
        @ExpectedResponses({200})
        MockResource createWithAzureAsyncOperation(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Azure-AsyncOperation&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        MockResource createWithAzureAsyncOperationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsRemaining);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=ProvisioningState")
        @ExpectedResponses({200})
        MockResource createWithProvisioningState(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=ProvisioningState&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        MockResource createWithProvisioningStateAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsRemaining);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}")
        @ExpectedResponses({200})
        Single<MockResource> createAsync(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location")
        @ExpectedResponses({200})
        Single<MockResource> createAsyncWithLocation(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Single<MockResource> createAsyncWithLocationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Azure-AsyncOperation")
        @ExpectedResponses({200})
        Single<MockResource> createAsyncWithAzureAsyncOperation(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Azure-AsyncOperation&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Single<MockResource> createAsyncWithAzureAsyncOperationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=ProvisioningState")
        @ExpectedResponses({200})
        Single<MockResource> createAsyncWithProvisioningState(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=ProvisioningState&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Single<MockResource> createAsyncWithProvisioningStateAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Observable<MockResource> beginCreateAsyncWithBadReturnType(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining=1&InitialResponseStatusCode=294")
        @ExpectedResponses({200})
        Observable<OperationStatus<MockResource>> beginCreateAsyncWithLocationAndPollsAndUnexpectedStatusCode(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Observable<OperationStatus<MockResource>> beginCreateAsyncWithLocationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Azure-AsyncOperation&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Observable<OperationStatus<MockResource>> beginCreateAsyncWithAzureAsyncOperationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=ProvisioningState&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Observable<OperationStatus<MockResource>> beginCreateAsyncWithProvisioningStateAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @DELETE("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}")
        @ExpectedResponses({200})
        void delete(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @DELETE("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location")
        @ExpectedResponses({200})
        void deleteWithLocation(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @DELETE("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        void deleteWithLocationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @DELETE("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}")
        @ExpectedResponses({200})
        Completable deleteAsync(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @DELETE("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location")
        @ExpectedResponses({200})
        Completable deleteAsyncWithLocation(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @DELETE("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Completable deleteAsyncWithLocationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @DELETE("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Observable<OperationStatus<Void>> beginDeleteAsyncWithLocationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);
    }

    @Test
    public void get() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .get("1", "mine", "a");
        assertNotNull(resource);
        assertEquals("a", resource.name);

        assertEquals(1, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void getAsync() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .getAsync("1", "mine", "b")
                .toBlocking().value();
        assertNotNull(resource);
        assertEquals("b", resource.name);

        assertEquals(1, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void create() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .create("1", "mine", "c");
        assertNotNull(resource);
        assertEquals("c", resource.name);

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void createWithLocation() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createWithLocation("1", "mine", "c");
        assertNotNull(resource);
        assertEquals("c", resource.name);

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(1, httpClient.pollRequests());
    }

    @Test
    public void createWithLocationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createWithLocationAndPolls("1", "mine", "c", 2);
        assertNotNull(resource);
        assertEquals("c", resource.name);

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(2, httpClient.pollRequests());
    }

    @Test
    public void createWithAzureAsyncOperation() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createWithAzureAsyncOperation("1", "mine", "c");
        assertNotNull(resource);
        assertEquals("c", resource.name);

        assertEquals(1, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(1, httpClient.pollRequests());
    }

    @Test
    public void createWithAzureAsyncOperationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createWithAzureAsyncOperationAndPolls("1", "mine", "c", 2);
        assertNotNull(resource);
        assertEquals("c", resource.name);

        assertEquals(1, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(2, httpClient.pollRequests());
    }

    @Test
    public void createWithProvisioningState() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createWithProvisioningState("1", "mine", "c");
        assertNotNull(resource);
        assertEquals("c", resource.name);

        assertEquals(1, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void createWithProvisioningStateAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createWithProvisioningStateAndPolls("1", "mine", "c", 3);
        assertNotNull(resource);
        assertEquals("c", resource.name);

        assertEquals(3, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void createAsync() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createAsync("1", "mine", "c")
                .toBlocking().value();
        assertNotNull(resource);
        assertEquals("c", resource.name);

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void createAsyncWithLocation() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createAsyncWithLocation("1", "mine", "c")
                .toBlocking().value();
        assertNotNull(resource);
        assertEquals("c", resource.name);

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(1, httpClient.pollRequests());
    }

    @Test
    public void createAsyncWithLocationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createAsyncWithLocationAndPolls("1", "mine", "c", 3)
                .toBlocking().value();
        assertNotNull(resource);
        assertEquals("c", resource.name);

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(3, httpClient.pollRequests());
    }

    @Test
    public void createAsyncWithAzureAsyncOperation() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createAsyncWithAzureAsyncOperation("1", "mine", "c")
                .toBlocking().value();
        assertNotNull(resource);
        assertEquals("c", resource.name);

        assertEquals(1, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(1, httpClient.pollRequests());
    }

    @Test
    public void createAsyncWithAzureAsyncOperationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createAsyncWithAzureAsyncOperationAndPolls("1", "mine", "c", 3)
                .toBlocking().value();
        assertNotNull(resource);
        assertEquals("c", resource.name);

        assertEquals(1, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(3, httpClient.pollRequests());
    }

    @Test
    public void createAsyncWithAzureAsyncOperationAndPollsWithDelay() throws InterruptedException {
        final long delayInMilliseconds = 100;
        AzureProxy.setDefaultDelayInMilliseconds(delayInMilliseconds);

        final MockAzureHttpClient httpClient = new MockAzureHttpClient();
        final int pollsUntilResource = 3;
        createMockService(MockResourceService.class, httpClient)
                .createAsyncWithAzureAsyncOperationAndPolls("1", "mine", "c", pollsUntilResource)
                .subscribe();

        Thread.sleep((long)(delayInMilliseconds * 0.75));

        for (int i = 0; i < pollsUntilResource; ++i) {
            assertEquals(0, httpClient.getRequests());
            assertEquals(1, httpClient.createRequests());
            assertEquals(0, httpClient.deleteRequests());
            assertEquals(i, httpClient.pollRequests());

            Thread.sleep(delayInMilliseconds);
        }

        assertEquals(1, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(pollsUntilResource, httpClient.pollRequests());
    }

    @Test
    public void createAsyncWithProvisioningState() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createAsyncWithProvisioningState("1", "mine", "c")
                .toBlocking().value();
        assertNotNull(resource);
        assertEquals("c", resource.name);

        assertEquals(1, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void createAsyncWithProvisioningStateAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createAsyncWithProvisioningStateAndPolls("1", "mine", "c", 5)
                .toBlocking().value();
        assertNotNull(resource);
        assertEquals("c", resource.name);

        assertEquals(5, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void beginCreateAsyncWithBadReturnType() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResourceService service = createMockService(MockResourceService.class, httpClient);
        try {
            service.beginCreateAsyncWithBadReturnType("1", "mine", "c", 2);
            fail("Expected exception.");
        }
        catch (InvalidReturnTypeException e) {
            assertContains(e.getMessage(), "AzureProxyTests$MockResourceService.beginCreateAsyncWithBadReturnType()");
            assertContains(e.getMessage(), "rx.Observable<com.microsoft.azure.v2.MockResource>");
        }

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void beginCreateAsyncWithLocationAndPollsAndUnexpectedStatusCode() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        createMockService(MockResourceService.class, httpClient)
                .beginCreateAsyncWithLocationAndPollsAndUnexpectedStatusCode("1", "mine", "c")
                .subscribe(
                        new Action1<OperationStatus<MockResource>>() {
                           @Override
                           public void call(OperationStatus<MockResource> mockResourceOperationStatus) {
                                fail();
                           }
                       },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                assertEquals(CloudException.class, throwable.getClass());
                                assertContains(throwable.getMessage(), "Could not determine a long running operation polling strategy");
                            }
                        });

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void beginCreateAsyncWithLocationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final AtomicInteger inProgressCount = new AtomicInteger();
        final Value<MockResource> resource = new Value<>();

        createMockService(MockResourceService.class, httpClient)
                .beginCreateAsyncWithLocationAndPolls("1", "mine", "c", 3)
                .subscribe(new Action1<OperationStatus<MockResource>>() {
                    @Override
                    public void call(OperationStatus<MockResource> operationStatus) {
                        if (!operationStatus.isDone()) {
                            inProgressCount.incrementAndGet();
                        }
                        else {
                            resource.set(operationStatus.result());
                        }
                    }
                });

        assertEquals(2, inProgressCount.get());
        assertNotNull(resource.get());
        assertEquals("c", resource.get().name);

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(3, httpClient.pollRequests());
    }

    @Test
    public void beginCreateAsyncWithAzureAsyncOperationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final AtomicInteger inProgressCount = new AtomicInteger();
        final Value<MockResource> resource = new Value<>();

        createMockService(MockResourceService.class, httpClient)
                .beginCreateAsyncWithAzureAsyncOperationAndPolls("1", "mine", "c", 3)
                .subscribe(new Action1<OperationStatus<MockResource>>() {
                    @Override
                    public void call(OperationStatus<MockResource> operationStatus) {
                        if (!operationStatus.isDone()) {
                            inProgressCount.incrementAndGet();
                        }
                        else {
                            resource.set(operationStatus.result());
                        }
                    }
                });

        assertEquals(3, inProgressCount.get());
        assertNotNull(resource.get());
        assertEquals("c", resource.get().name);

        assertEquals(1, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(3, httpClient.pollRequests());
    }

    @Test
    public void beginCreateAsyncWithProvisioningStateAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final AtomicInteger inProgressCount = new AtomicInteger();
        final Value<MockResource> resource = new Value<>();

        createMockService(MockResourceService.class, httpClient)
                .beginCreateAsyncWithProvisioningStateAndPolls("1", "mine", "c", 4)
                .subscribe(new Action1<OperationStatus<MockResource>>() {
                    @Override
                    public void call(OperationStatus<MockResource> operationStatus) {
                        if (!operationStatus.isDone()) {
                            inProgressCount.incrementAndGet();
                        }
                        else {
                            resource.set(operationStatus.result());
                        }
                    }
                });

        assertEquals(3, inProgressCount.get());
        assertNotNull(resource.get());
        assertEquals("c", resource.get().name);

        assertEquals(4, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void beginCreateAsyncWithLocationAndPollsWhenPollsUntilResourceIs0() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final AtomicInteger inProgressCount = new AtomicInteger();
        final Value<MockResource> resource = new Value<>();

        createMockService(MockResourceService.class, httpClient)
                .beginCreateAsyncWithLocationAndPolls("1", "mine", "c", 0)
                .subscribe(new Action1<OperationStatus<MockResource>>() {
                    @Override
                    public void call(OperationStatus<MockResource> operationStatus) {
                        if (!operationStatus.isDone()) {
                            inProgressCount.incrementAndGet();
                        }
                        else {
                            resource.set(operationStatus.result());
                        }
                    }
                });

        assertEquals(0, inProgressCount.get());
        assertNotNull(resource.get());
        assertEquals("c", resource.get().name);

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void delete() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        createMockService(MockResourceService.class, httpClient)
                .delete("1", "mine", "c");

        assertEquals(0, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(1, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void deleteWithLocation() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        createMockService(MockResourceService.class, httpClient)
                .deleteWithLocation("1", "mine", "c");

        assertEquals(0, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(1, httpClient.deleteRequests());
        assertEquals(1, httpClient.pollRequests());
    }

    @Test
    public void deleteWithLocationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        createMockService(MockResourceService.class, httpClient)
                .deleteWithLocationAndPolls("1", "mine", "c", 4);

        assertEquals(0, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(1, httpClient.deleteRequests());
        assertEquals(4, httpClient.pollRequests());
    }

    @Test
    public void deleteAsync() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        createMockService(MockResourceService.class, httpClient)
                .deleteAsync("1", "mine", "c")
                .await();

        assertEquals(0, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(1, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void deleteAsyncWithLocation() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        createMockService(MockResourceService.class, httpClient)
                .deleteAsyncWithLocation("1", "mine", "c")
                .await();

        assertEquals(0, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(1, httpClient.deleteRequests());
        assertEquals(1, httpClient.pollRequests());
    }

    @Test
    public void deleteAsyncWithLocationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        createMockService(MockResourceService.class, httpClient)
                .deleteAsyncWithLocationAndPolls("1", "mine", "c", 10)
                .await();

        assertEquals(0, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(1, httpClient.deleteRequests());
        assertEquals(10, httpClient.pollRequests());
    }

    @Test
    public void beginDeleteAsyncWithLocationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final AtomicInteger inProgressCount = new AtomicInteger();
        final Value<Boolean> completed = new Value<>();

        createMockService(MockResourceService.class, httpClient)
                .beginDeleteAsyncWithLocationAndPolls("1", "mine", "c", 3)
                .subscribe(new Action1<OperationStatus<Void>>() {
                    @Override
                    public void call(OperationStatus<Void> operationStatus) {
                        if (!operationStatus.isDone()) {
                            inProgressCount.incrementAndGet();
                        }
                        else {
                            completed.set(true);
                        }
                    }
                });

        assertEquals(2, inProgressCount.get());
        assertNotNull(completed.get());
        assertTrue(completed.get());

        assertEquals(0, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(1, httpClient.deleteRequests());
        assertEquals(3, httpClient.pollRequests());
    }

    @Test
    public void beginDeleteAsyncWithLocationAndPollsWhenPollsUntilResourceIs0() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final AtomicInteger inProgressCount = new AtomicInteger();
        final Value<Boolean> completed = new Value<>();

        createMockService(MockResourceService.class, httpClient)
                .beginDeleteAsyncWithLocationAndPolls("1", "mine", "c", 0)
                .subscribe(new Action1<OperationStatus<Void>>() {
                    @Override
                    public void call(OperationStatus<Void> operationStatus) {
                        if (!operationStatus.isDone()) {
                            inProgressCount.incrementAndGet();
                        }
                        else {
                            completed.set(true);
                        }
                    }
                });

        assertEquals(0, inProgressCount.get());
        assertNotNull(completed.get());
        assertTrue(completed.get());

        assertEquals(0, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(1, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    private static <T> T createMockService(Class<T> serviceClass, MockAzureHttpClient httpClient) {
        return AzureProxy.create(serviceClass, (AzureEnvironment) null, httpClient, serializer);
    }

    private static void assertContains(String value, String expectedSubstring) {
        assertTrue("Expected \"" + value + "\" to contain \"" + expectedSubstring + "\".", value.contains(expectedSubstring));
    }

    private static final SerializerAdapter<?> serializer = new JacksonAdapter();
}
